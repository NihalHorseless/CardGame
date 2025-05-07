package com.example.cardgame.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardgame.audio.MusicManager
import com.example.cardgame.audio.MusicTrack
import com.example.cardgame.audio.SoundManager
import com.example.cardgame.audio.SoundType
import com.example.cardgame.data.enum.FortificationType
import com.example.cardgame.data.enum.GameState
import com.example.cardgame.data.enum.InteractionMode
import com.example.cardgame.data.enum.TacticCardType
import com.example.cardgame.data.enum.TargetType
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.campaign.Campaign
import com.example.cardgame.data.model.campaign.CampaignLevel
import com.example.cardgame.data.model.campaign.Difficulty
import com.example.cardgame.data.model.campaign.SpecialRule
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.repository.CampaignRepository
import com.example.cardgame.data.repository.CardRepository
import com.example.cardgame.game.GameManager
import com.example.cardgame.game.PlayerContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class GameViewModel(
    private val cardRepository: CardRepository,
    private val campaignRepository: CampaignRepository,
    private val soundManager: SoundManager,
    private val musicManager: MusicManager
) : ViewModel() {

    private val _gameManager = GameManager()
    val gameManager: GameManager get() = _gameManager

    // Get contexts for the player and opponent
    private val _playerContext get() = _gameManager.getPlayerContextById(0)
    val playerContext: PlayerContext get() = _playerContext

    private val _opponentContext get() = _gameManager.getPlayerContextById(1)
    val opponentContext: PlayerContext get() = _opponentContext

    // Available decks
    private val _availableDecks = mutableStateOf<List<String>>(emptyList())
    val availableDecks: State<List<String>> = _availableDecks

    // Selected decks
    private val _selectedPlayerDeck = mutableStateOf<String?>(null)
    val selectedPlayerDeck: State<String?> = _selectedPlayerDeck

    private val _selectedOpponentDeck = mutableStateOf<String?>(null)
    val selectedOpponentDeck: State<String?> = _selectedOpponentDeck

    // Basic game state
    private val _playerHandState = mutableStateOf<List<Card>>(_gameManager.players[0].hand)
    val playerHandState: State<List<Card>> = _playerHandState

    private val _gameBoardState = mutableStateOf<Array<Array<UnitCard?>>>(
        Array(_gameManager.gameBoard.rows) {
            arrayOfNulls(_gameManager.gameBoard.columns)
        }
    )

    // Selected cell on the unified board
    private val _selectedCell = mutableStateOf<Pair<Int, Int>?>(null)
    val selectedCell: State<Pair<Int, Int>?> = _selectedCell

    // Valid move destinations for the selected unit
    private val _validMoveDestinations = mutableStateOf<List<Pair<Int, Int>>>(emptyList())
    val validMoveDestinations: State<List<Pair<Int, Int>>> = _validMoveDestinations

    // Valid attack targets for the selected unit
    private val _validAttackTargets = mutableStateOf<List<Pair<Int, Int>>>(emptyList())
    val validAttackTargets: State<List<Pair<Int, Int>>> = _validAttackTargets

    // Current interaction mode
    private val _interactionMode = mutableStateOf(InteractionMode.DEFAULT)
    val interactionMode: State<InteractionMode> = _interactionMode

    private val _targetingType = mutableStateOf<TacticCardType?>(null)
    val targetingType: State<TacticCardType?> = _targetingType

    private val _playerMana = mutableIntStateOf(0)
    val playerMana: State<Int> = _playerMana

    private val _playerMaxMana = mutableIntStateOf(10)
    val playerMaxMana: State<Int> = _playerMaxMana

    private val _playerHealth = mutableIntStateOf(30)
    val playerHealth: State<Int> = _playerHealth

    private val _opponentHealth = mutableIntStateOf(30)
    val opponentHealth: State<Int> = _opponentHealth

    private val _opponentName = mutableStateOf("Opponent")
    val opponentName: State<String> = _opponentName

    private val _opponentHandSize = mutableIntStateOf(0)
    val opponentHandSize: State<Int> = _opponentHandSize

    private val _isPlayerTurn = mutableStateOf(true)
    val isPlayerTurn: State<Boolean> = _isPlayerTurn

    private val _gameState = mutableStateOf(GameState.NOT_STARTED)
    val gameState: State<GameState> = _gameState

    private val _statusMessage = mutableStateOf("")
    val statusMessage: State<String> = _statusMessage

    // Animation states
    private val _isCardAnimationVisible = mutableStateOf(false)
    val isCardAnimationVisible: State<Boolean> = _isCardAnimationVisible

    private val _cardAnimationPosition = mutableStateOf(Pair(0f, 0f))
    val cardAnimationPosition: State<Pair<Float, Float>> = _cardAnimationPosition

    // Attack animation states
    private val _isSimpleAttackVisible = mutableStateOf(false)
    val isSimpleAttackVisible: State<Boolean> = _isSimpleAttackVisible

    private val _attackingUnitType = mutableStateOf(UnitType.INFANTRY)
    val attackingUnitType: State<UnitType> = _attackingUnitType

    private val _attackTargetPosition = mutableStateOf(Pair(0f, 0f))
    val attackTargetPosition: State<Pair<Float, Float>> = _attackTargetPosition


    // Win conditions
    private val _isGameOver = mutableStateOf(false)
    val isGameOver: State<Boolean> = _isGameOver

    private val _isPlayerWinner = mutableStateOf(false)
    val isPlayerWinner: State<Boolean> = _isPlayerWinner

    // Position tracking for animations
    private val cellPositions = mutableMapOf<Pair<Int, Int>, Pair<Float, Float>>()


    // Selected card index for deployment
    private val _selectedCardIndex = mutableStateOf<Int?>(null)
    val selectedCardIndex: State<Int?> = _selectedCardIndex

    // Valid deployment positions when a card is selected
    private val _validDeploymentPositions = mutableStateOf<List<Pair<Int, Int>>>(emptyList())
    val validDeploymentPositions: State<List<Pair<Int, Int>>> = _validDeploymentPositions

    // Flag to indicate if the last attack had a counter bonus
    private val _isCounterBonus = mutableStateOf(false)
    val isCounterBonus: State<Boolean> = _isCounterBonus

    // Current player ID (default to player 0)
    private val _currentPlayerId = mutableIntStateOf(0)
    val currentPlayerId: State<Int> = _currentPlayerId

    private val _isTacticEffectVisible = mutableStateOf(false)
    val isTacticEffectVisible: State<Boolean> = _isTacticEffectVisible

    private val _tacticEffectType = mutableStateOf(TacticCardType.SPECIAL)
    val tacticEffectType: State<TacticCardType> = _tacticEffectType

    private val _tacticEffectPosition = mutableStateOf(Pair(0f, 0f))
    val tacticEffectPosition: State<Pair<Float, Float>> = _tacticEffectPosition

    // Campaign-related states
    private val _currentCampaign = mutableStateOf<Campaign?>(null)
    val currentCampaign: State<Campaign?> = _currentCampaign

    private val _currentLevel = mutableStateOf<CampaignLevel?>(null)
    val currentLevel: State<CampaignLevel?> = _currentLevel

    private val _isInCampaign = mutableStateOf(false)
    val isInCampaign: State<Boolean> = _isInCampaign

    private val _currentObjective = mutableStateOf<String?>(null)
    val currentObjective: State<String?> = _currentObjective

    private val _objectiveCompleted = mutableStateOf(false)
    val objectiveCompleted: State<Boolean> = _objectiveCompleted

    init {
        // Load available decks when ViewModel is created
        loadAvailableDecks()
    }

    /**
     * Loads the list of available decks from the CardRepository
     */
    fun loadAvailableDecks() {
        viewModelScope.launch {
            val allDeckNames = cardRepository.getAllAvailableDecks()
            _availableDecks.value = allDeckNames
            loadAvailableDeckNames()
        }
    }
    private val _availableDeckNames = mutableStateOf<List<String>>(emptyList())
    val availableDeckNames: State<List<String>> = _availableDeckNames

    fun loadAvailableDeckNames() {
        viewModelScope.launch {
            val deckNames = cardRepository.getAllAvailableDeckNames()
            _availableDeckNames.value = deckNames
        }
    }

    fun loadPlayerDeck(deckName: String) {
        viewModelScope.launch {
            val deck = cardRepository.loadAnyDeck(deckName)
            if (deck != null) {
                _gameManager.players[0].setDeck(deck)
            } else {
                _statusMessage.value = "Failed to load deck: $deckName"
            }
        }
    }

    /**
     * Sets the selected deck for the player
     */
    fun setPlayerDeck(deckName: String) {
        _selectedPlayerDeck.value = deckName
        viewModelScope.launch {
            // First try to load from custom decks, then predefined decks
            val deck = cardRepository.loadAnyDeck(deckName)
            if (deck != null) {
                _gameManager.players[0].setDeck(deck)
            } else {
                _statusMessage.value = "Failed to load deck: $deckName"
            }
        }
    }

    /**
     * Sets the selected campaign for the player
     */
    fun setCurrentCampaign(selectedCampaign: Campaign) {
        _currentCampaign.value = selectedCampaign
    }

    /**
     * Sets the selected deck for the opponent
     */
    fun setOpponentDeck(deckName: String) {
        _selectedOpponentDeck.value = deckName
    }

    /**
     * Gets detailed information about a specific deck
     */
      fun getDeckInfo(deckName: String): Deck? {

        return cardRepository.loadPlayerDeck(deckName)
    }

    fun registerCellPosition(row: Int, col: Int, x: Float, y: Float) {
        cellPositions[Pair(row, col)] = Pair(x, y)
    }



    /**
     * Load all available campaigns
     */
    fun loadAvailableCampaigns(): List<Campaign> {
        return campaignRepository.getAllCampaigns()
    }

    /**
     * Start a campaign level
     */
    fun startCampaignLevel(campaignId: String, levelId: String) {
        _isInCampaign.value = true
        // Load PLayer Deck
        val playerDeckName = _selectedPlayerDeck.value
        if (playerDeckName == null) {
            _statusMessage.value = "Select a proper Deck name"
            return
        }
        viewModelScope.launch {
            val playerDeck = cardRepository.loadAnyDeck(playerDeckName)

            if (playerDeck == null) {
                _statusMessage.value = "Failed to load player deck: $playerDeckName"
                return@launch
            }
            playerDeck.shuffle()
            _gameManager.players[0].setDeck(playerDeck)

            // Load campaign
            val campaign = campaignRepository.getCampaign(campaignId)
            _currentCampaign.value = campaign

            if (campaign != null) {
                // Find the requested level
                val level = campaign.levels.find { it.id == levelId }
                _currentLevel.value = level

                if (level != null) {
                    // Set current objective if any
                    val customObjective =
                        level.specialRules.filterIsInstance<SpecialRule.CustomObjective>().firstOrNull()
                    _currentObjective.value = customObjective?.description

                    // Configure game for this level
                    configureGameForLevel(level)

                    // Start the game
                    _gameManager.startCampaignGame(level)
                    updateAllGameStates()
                }
            }
        }

    }

    /**
     * Configure the game with level-specific settings
     */
    private fun configureGameForLevel(level: CampaignLevel) {
        // Load the opponent's deck and name
        val opponentDeck = cardRepository.loadAIDeck(level.opponentDeckId)
        _opponentName.value = level.opponentName
        if (opponentDeck != null) {
            _gameManager.players[1].setDeck(opponentDeck)
        } else {
            // Fallback if deck not found
            _statusMessage.value = "Opponent deck not found, using default"
            Log.d("LevelConfig", opponentDeck.toString())
        }

        if (opponentDeck == null) {
            _statusMessage.value = "Failed to load opponent deck: $opponentDeck"
            return
        }
        opponentDeck.shuffle()

        Log.d("LevelConfig", opponentDeck.toString())

        // Set player and opponent health
        _gameManager.players[0].health = level.startingHealth ?: 30
        _gameManager.players[1].health = when (level.difficulty) {
            Difficulty.EASY -> 30
            Difficulty.MEDIUM -> 40
            Difficulty.HARD -> 50
            Difficulty.LEGENDARY -> 60
        }
        _opponentHealth.intValue = _gameManager.players[1].health
        Log.d("LevelConfig", "${_gameManager.players[1].health}  $opponentHealth")
        // Set starting mana
        _gameManager.players[0].currentMana = level.startingMana ?: 1
        _gameManager.players[1].currentMana = level.startingMana ?: 1

        // Apply special rules
        applySpecialRules(level.specialRules)
    }

    /**
     * Apply special rules to the game
     */
    private fun applySpecialRules(rules: List<SpecialRule>) {
        rules.forEach { rule ->
            when (rule) {
                is SpecialRule.StartingBoard -> {
                    rule.unitSetup.forEach { setup ->
                        if (setup.isPlayerUnit) {
                            // Player units
                            val card = cardRepository.getCardById(setup.unitId)
                            if (card is UnitCard) {
                                _gameManager.gameBoard.placeUnit(
                                    card.clone(),
                                    setup.row,
                                    setup.col,
                                    0
                                )
                            } else if (card is FortificationCard) {
                                _gameManager.gameBoard.placeFortification(
                                    card.clone(),
                                    setup.row,
                                    setup.col,
                                    0
                                )
                            }
                        } else {
                            // Enemy units
                            val card = cardRepository.getCardById(setup.unitId)
                            if (card is UnitCard) {
                                _gameManager.gameBoard.placeUnit(
                                    card.clone(),
                                    setup.row,
                                    setup.col,
                                    1
                                )
                            } else if (card is FortificationCard) {
                                _gameManager.gameBoard.placeFortification(
                                    card.clone(),
                                    setup.row,
                                    setup.col,
                                    1
                                )
                            }
                        }
                    }
                }

                is SpecialRule.AdditionalCards -> {
                    rule.cards.forEach { cardId ->
                        val card = cardRepository.getCardById(cardId)
                        if (card != null) {
                            _gameManager.players[0].hand.add(card)
                        }
                    }
                }

                is SpecialRule.ModifiedMana -> {
                    _gameManager.players[0].maxMana += rule.amount
                }

                is SpecialRule.CustomObjective -> {
                    // Custom objective will be checked during gameplay
                }
            }
        }
    }

    /**
     * Check for special victory conditions
     */
    private fun checkCampaignObjectives() {
        val currentLevel = _currentLevel.value ?: return
        val customObjective =
            currentLevel.specialRules.filterIsInstance<SpecialRule.CustomObjective>().firstOrNull()

        if (customObjective != null) {
            _objectiveCompleted.value = customObjective.checkCompletion(_gameManager)
        } else {
            // If no custom objective, just winning is enough
            _objectiveCompleted.value = true
        }
    }

    /**
     * Mark current level as completed
     */
    private fun completeCampaignLevel() {
        val campaign = _currentCampaign.value ?: return
        val level = _currentLevel.value ?: return

        if (_isPlayerWinner.value && _objectiveCompleted.value) {
            // Create updated campaign with this level marked as completed
            val updatedLevels = campaign.levels.map {
                if (it.id == level.id) it.copy(isCompleted = true) else it
            }

            val updatedCampaign = campaign.copy(levels = updatedLevels)
            _currentCampaign.value = updatedCampaign

            // Save progress
            campaignRepository.updateCampaign(updatedCampaign)

            // Update message
            _statusMessage.value = "Level completed!"
        }
    }

    /**
     * Exit the current campaign level and return to campaign screen
     */
    fun exitCampaignLevel() {
        _isInCampaign.value = false
        _currentLevel.value = null
        _currentObjective.value = null
        _objectiveCompleted.value = false

        // Reset the game state
        _gameManager.reset()
    }

    /**
     * Check if the current campaign is completed
     */
    fun isCampaignCompleted(): Boolean {
        val campaign = _currentCampaign.value ?: return false
        return campaign.levels.all { it.isCompleted }
    }

    /**
     * Get the next unlocked level in the current campaign
     */
    fun getNextUnlockedLevel(): CampaignLevel? {
        val campaign = _currentCampaign.value ?: return null

        // Find the first incomplete level, or the first level if none completed yet
        val nextLevel = campaign.levels.find { !it.isCompleted }

        // If all levels are completed, return the last one
        return nextLevel ?: campaign.levels.lastOrNull()
    }

    /**
     * Reset all campaign progress
     */
    fun resetCampaignProgress() {
        campaignRepository.resetAllProgress()
        _currentCampaign.value = null
        _currentLevel.value = null
    }


    fun startGame() {
        // Check if decks are selected
        val playerDeckName = _selectedPlayerDeck.value
        val opponentDeckName = _selectedOpponentDeck.value

        if (playerDeckName == null || opponentDeckName == null) {
            _statusMessage.value = "Please select decks for both players"
            return
        }

        // Reset game over state
        _isGameOver.value = false

        // Use viewModelScope to handle loading decks asynchronously
        viewModelScope.launch {
            // Load player deck - check custom decks first, then predefined decks
            val playerDeck = cardRepository.loadAnyDeck(playerDeckName)

            if (playerDeck == null) {
                _statusMessage.value = "Failed to load player deck: $playerDeckName"
                return@launch
            }

            // For opponent deck, still use predefined AI decks
            val opponentDeck = cardRepository.loadAnyDeck(opponentDeckName)

            if (opponentDeck == null) {
                _statusMessage.value = "Failed to load opponent deck: $opponentDeckName"
                return@launch
            }

            // Shuffle decks
            playerDeck.shuffle()
            opponentDeck.shuffle()

            // Set player decks
            _gameManager.players[0].setDeck(playerDeck)
            _gameManager.players[1].setDeck(opponentDeck)
Log.d("StartGame",playerDeck.toString())
            _opponentName.value = "Opponent"
            _isInCampaign.value = false

            // Start the game
            _gameManager.startGame()
            updateAllGameStates()

            _statusMessage.value = "Game started with decks: $playerDeckName vs $opponentDeckName"
        }
    }

    /**
     * Gets valid deployment positions for a player based on their ID.
     * Player 0 can deploy in the bottom two rows (3-4 in a 5x5 board)
     * Player 1 can deploy in the top two rows (0-1 in a 5x5 board)
     */
    fun getValidDeploymentPositions(playerId: Int): List<Pair<Int, Int>> {
        val validPositions = mutableListOf<Pair<Int, Int>>()

        // Define row ranges based on player ID
        val rowRange = if (playerId == 0) {
            (gameManager.gameBoard.rows - 2) until gameManager.gameBoard.rows // Bottom two rows for player 0
        } else {
            0 until 2 // Top two rows for player 1
        }

        // Add all empty cells in the valid rows
        for (row in rowRange) {
            for (col in 0 until gameManager.gameBoard.columns) {
                if (gameManager.gameBoard.getUnitAt(row, col) == null) {
                    validPositions.add(Pair(row, col))
                }
            }
        }

        return validPositions
    }

    /**
     * Handle card selection from hand
     */
    fun onCardSelected(cardIndex: Int) {
        if (!_isPlayerTurn.value) return

        val hand = _playerHandState.value
        if (cardIndex < 0 || cardIndex >= hand.size) return

        val card = hand[cardIndex]
        soundManager.playSound(SoundType.CARD_PICK)

        // If we're already in a targeting mode, cancel it
        if (_interactionMode.value == InteractionMode.CARD_TARGETING ||
            _interactionMode.value == InteractionMode.DEPLOY) {
            cancelDeployment()
            return
        }

        // Handle both unit and fortification cards - deploy mode
        when (card) {
            is UnitCard, is FortificationCard -> {
                // Check if player has enough mana
                if (_playerMana.intValue < card.manaCost) {
                    _statusMessage.value = "Not enough mana!"
                    return
                }

                // Set interaction mode to DEPLOY specifically for unit/fortification cards
                _interactionMode.value = InteractionMode.DEPLOY
                _selectedCardIndex.value = cardIndex

                // Get valid deployment positions
                _validDeploymentPositions.value = getValidDeploymentPositions(0) // 0 is player ID

                _statusMessage.value = "Select a position to deploy"
            }

            is TacticCard -> {
                handleTacticCardSelection(card, cardIndex)
            }

            else -> {
                // For non-unit cards, just play them directly
                playCard(cardIndex)
            }
        }
    }


    /**
     * Handle TacticCard selection from hand
     */
    private fun handleTacticCardSelection(card: TacticCard, cardIndex: Int) {
        // Check if player has enough mana
        if (_playerMana.intValue < card.manaCost) {
            _statusMessage.value = "Not enough mana!"
            return
        }
        // Deselect cell
        _selectedCell.value = null
        _validMoveDestinations.value = emptyList()


        // Different handling based on card's target type
        when (card.targetType) {
            TargetType.NONE -> {
                // Cards that don't need targets (like card draw) can be played immediately
                val success = card.play(_gameManager.players[0], _gameManager, null)
                if (success) {
                    _statusMessage.value = "${card.name} played successfully"
                    updateAllGameStates()
                    // Deselect Card
                    _selectedCardIndex.value = null
                } else {
                    _statusMessage.value = "Failed to play ${card.name}"
                }
            }

            else -> {
                // Cards that need targets - switch to CARD_TARGETING mode (not DEPLOY)
                _selectedCardIndex.value = cardIndex
                _interactionMode.value = InteractionMode.CARD_TARGETING

                _targetingType.value = card.cardType

                // Highlight valid targets based on card target type
                _validDeploymentPositions.value = when (card.targetType) {
                    TargetType.FRIENDLY -> getFriendlyTargets()
                    TargetType.ENEMY -> getEnemyTargets()
                    TargetType.BOARD -> getFriendlyTargets() + getEnemyTargets()
                    TargetType.ANY -> getFriendlyTargets() + getEnemyTargets()
                    else -> emptyList()
                }

                _statusMessage.value = "Select a target for ${card.name}"
            }
        }
    }

    /**
     * Handle a click on a cell when in targeting mode
     */
    private fun handleTacticCardTargeting(row: Int, col: Int) {
        val cardIndex = _selectedCardIndex.value ?: return
        val card = _playerHandState.value[cardIndex] as? TacticCard ?: return

        // Check if this is a valid target position
        if (Pair(row, col) !in _validDeploymentPositions.value) {
            _statusMessage.value = "Invalid target"
            return
        }

        // Convert 2D board position to linear position for the effect function
        val linearPosition = row * _gameManager.gameBoard.columns + col

        // Play the card with the target
        val success = card.play(_gameManager.players[0], _gameManager, linearPosition)


        if (success) {
            // Show animation effect
            playTacticCardSound(card.cardType)
            val targetPos = cellPositions[Pair(row, col)]
            if (targetPos != null) {
                _tacticEffectPosition.value = targetPos
                _tacticEffectType.value = card.cardType
                _isTacticEffectVisible.value = true
            }

            // Reset selection state
            resetSelectionStates()

            // Update game state
            updateAllGameStates()

            // Deselect Card
            _selectedCardIndex.value = null
        } else {
            _statusMessage.value = "Failed to play ${card.name}"
        }
    }

    /**
     * Helper methods to find valid targets
     */
    private fun getFriendlyTargets(): List<Pair<Int, Int>> {
        val targets = mutableListOf<Pair<Int, Int>>()

        // Get all positions with friendly units
        for (row in 0 until _gameManager.gameBoard.rows) {
            for (col in 0 until _gameManager.gameBoard.columns) {
                // Check for units
                val unit = _gameManager.gameBoard.getUnitAt(row, col)
                if (unit != null && _gameManager.gameBoard.getUnitOwner(unit) == 0) {
                    targets.add(Pair(row, col))
                    continue
                }

                // Check for fortifications
                val fort = _gameManager.gameBoard.getFortificationAt(row, col)
                if (fort != null && _gameManager.gameBoard.getFortificationOwner(fort) == 0) {
                    targets.add(Pair(row, col))
                }
            }
        }

        return targets
    }

    private fun getEnemyTargets(): List<Pair<Int, Int>> {
        // Similar to getFriendlyTargets but for enemy units (owner == 1)
        // Implementation similar to above
        val targets = mutableListOf<Pair<Int, Int>>()

        for (row in 0 until _gameManager.gameBoard.rows) {
            for (col in 0 until _gameManager.gameBoard.columns) {
                // Check for enemy units
                val unit = _gameManager.gameBoard.getUnitAt(row, col)
                if (unit != null && _gameManager.gameBoard.getUnitOwner(unit) == 1) {
                    targets.add(Pair(row, col))
                    continue
                }

                // Check for enemy fortifications
                val fort = _gameManager.gameBoard.getFortificationAt(row, col)
                if (fort != null && _gameManager.gameBoard.getFortificationOwner(fort) == 1) {
                    targets.add(Pair(row, col))
                }
            }
        }

        return targets
    }

    private fun getBoardTargets(): List<Pair<Int, Int>> {
        // Return all board positions
        val targets = mutableListOf<Pair<Int, Int>>()

        for (row in 0 until _gameManager.gameBoard.rows) {
            for (col in 0 until _gameManager.gameBoard.columns) {
                targets.add(Pair(row, col))
            }
        }

        return targets
    }

    private fun deployCardAtPosition(row: Int, col: Int) {
        val cardIndex = _selectedCardIndex.value ?: return

        if (cardIndex < 0 || cardIndex >= _playerHandState.value.size) return

        // Check if position is valid
        if (!_validDeploymentPositions.value.contains(Pair(row, col))) {
            _statusMessage.value = "Cannot deploy here"
            return
        }

        // Play the card at this position
        playCard(cardIndex, row, col)

        // Reset deployment state
        _selectedCardIndex.value = null
        _validDeploymentPositions.value = emptyList()
        _interactionMode.value = InteractionMode.DEFAULT
    }

    fun onTacticEffectComplete() {
        _isTacticEffectVisible.value = false
    }

    private fun cancelDeployment() {
        _selectedCardIndex.value = null
        _validDeploymentPositions.value = emptyList()
        _interactionMode.value = InteractionMode.DEFAULT
        _statusMessage.value = "Deployment cancelled"
    }

    /**
     * Updates the board state representation from the GameManager
     */
    private fun updateBoardState() {
        val boardRows = _gameManager.gameBoard.rows
        val boardCols = _gameManager.gameBoard.columns

        val boardState = Array(boardRows) { row ->
            Array(boardCols) { col ->
                _gameManager.gameBoard.getUnitAt(row, col)
            }
        }

        _gameBoardState.value = boardState
    }

    /**
     * Updates all game state from the GameManager
     */
    private fun updateAllGameStates() {
        val currentPlayer = _gameManager.turnManager.currentPlayer ?: return

        // Update hand
        _playerHandState.value = _gameManager.players[0].hand

        // Update board
        updateBoardState()
        Log.d("LevelConfigD", _opponentHealth.value.toString())
        // Update player stats
        _playerMana.intValue = _gameManager.players[0].currentMana
        _playerMaxMana.intValue = _gameManager.players[0].maxMana
        _playerHealth.intValue = _gameManager.players[0].health
        _opponentHealth.intValue = _gameManager.players[1].health
        Log.d("LevelConfigD", _opponentHealth.value.toString())

        // Update opponent stats
        _opponentHandSize.intValue = _gameManager.players[1].hand.size

        // Update turn state
        _isPlayerTurn.value = currentPlayer.id == 0
        _gameState.value = _gameManager.gameState

        // Reset selection state
        resetSelectionStates()

        // Check for game over
        checkGameOver()
    }

    /**
     * Handles clicking on a cell in the unified board with direct interaction style
     */
    fun onCellClick(row: Int, col: Int) {
        if (!_isPlayerTurn.value) return
        when (_interactionMode.value) {
            InteractionMode.DEPLOY -> {
                // Handle unit/fortification deployment
                if (_validDeploymentPositions.value.contains(Pair(row, col))) {
                    deployCardAtPosition(row, col)
                } else {
                    _statusMessage.value = "Cannot deploy at this position"
                }
                return
            }

            InteractionMode.CARD_TARGETING -> {
                // Handle tactic card targeting
                if (_validDeploymentPositions.value.contains(Pair(row, col))) {
                    handleTacticCardTargeting(row, col)
                } else {
                    _statusMessage.value = "Invalid target"
                }
                return
            }

            else -> {
                // Continue with the existing unit selection and movement logic
                // (No changes needed to the existing code here)
            }
        }

        val clickedUnit = _gameManager.gameBoard.getUnitAt(row, col)
        val unitOwner = clickedUnit?.let { _gameManager.gameBoard.getUnitOwner(it) }

        // Check for fortifications
        val clickedFortification = _gameManager.gameBoard.getFortificationAt(row, col)
        val fortificationOwner =
            clickedFortification?.let { _gameManager.gameBoard.getFortificationOwner(it) }

        // If there's a fortification at this cell, handle it
        if (clickedFortification != null) {
            // If fortification belongs to player
            if (fortificationOwner == 0) {
                handleFortificationSelection(row, col, clickedFortification, fortificationOwner)
            }
            // If player has a unit selected and clicks on enemy fortification to attack
            else if (_selectedCell.value != null && Pair(row, col) in _validAttackTargets.value) {
                val (selectedRow, selectedCol) = _selectedCell.value!!
                val selectedUnit = _gameManager.gameBoard.getUnitAt(selectedRow, selectedCol)

                if (selectedUnit != null) {
                    // First check if this is a fortification target
                    val targetFort = _gameManager.gameBoard.getFortificationAt(row, col)
                    if (targetFort != null) {
                        executeAttackAgainstFortification(selectedRow, selectedCol, row, col)
                    } else {
                        // This is a regular unit attack
                        executeAttack(selectedRow, selectedCol, row, col, playerContext)
                    }
                }
            }
            return
        }

        // If a fortification is selected and player clicks on a valid attack target
        if (_selectedCell.value != null) {
            val (selectedRow, selectedCol) = _selectedCell.value!!
            val selectedFortification =
                _gameManager.gameBoard.getFortificationAt(selectedRow, selectedCol)

            if (selectedFortification != null &&
                selectedFortification.fortType == FortificationType.TOWER &&
                Pair(row, col) in _validAttackTargets.value
            ) {
                executeFortificationAttack(selectedRow, selectedCol, row, col)
                return
            }
        }

        // Original unit selection and movement logic
        if (_selectedCell.value == null && clickedUnit != null && unitOwner == 0) {
            // Select the unit and immediately show movement and attack options
            _selectedCell.value = Pair(row, col)
            playUnitTapSound(clickedUnit.unitType)

            // Show valid attack targets if unit can attack
            if (clickedUnit.canAttackThisTurn) {
                _validAttackTargets.value =
                    playerContext.getValidAttackTargets(row, col, _gameManager)
            } else {
                _validAttackTargets.value = emptyList()
            }

            // Show valid movement destinations if unit can move
            if (playerContext.canUnitMove(row, col, _gameManager)) {
                _validMoveDestinations.value =
                    playerContext.getValidMoveDestinations(row, col, _gameManager)
                _statusMessage.value = "Select a destination to move to, or a target to attack."
            } else {
                _validMoveDestinations.value = emptyList()

                if (clickedUnit.canAttackThisTurn) {
                    _statusMessage.value = "Select a target to attack."
                } else {
                    _statusMessage.value = "This unit has already acted this turn."
                }
            }
        }
        // If a unit is selected and player clicks on a valid move destination
        else if (_selectedCell.value != null && Pair(row, col) in _validMoveDestinations.value) {
            val (selectedRow, selectedCol) = _selectedCell.value!!
            executeMove(selectedRow, selectedCol, row, col, context = playerContext)
        }
        // If a unit is selected and player clicks on a valid attack target
        else if (_selectedCell.value != null && Pair(row, col) in _validAttackTargets.value) {
            val (selectedRow, selectedCol) = _selectedCell.value!!
            executeAttack(selectedRow, selectedCol, row, col, playerContext)
        }
        // If player clicks on another of their units, select that unit instead
        else if (clickedUnit != null && unitOwner == 0) {
            // Clear previous selection
            _selectedCell.value = null
            _validMoveDestinations.value = emptyList()
            _validAttackTargets.value = emptyList()

            // Select the new unit (call onCellClick recursively)
            onCellClick(row, col)
        }
        // If player clicks elsewhere, clear selection
        else {
            _selectedCell.value = null
            _validMoveDestinations.value = emptyList()
            _validAttackTargets.value = emptyList()
            _statusMessage.value = ""
        }
    }


    /**
     * Execute a movement action
     */
    private fun executeMove(
        fromRow: Int,
        fromCol: Int,
        toRow: Int,
        toCol: Int,
        context: PlayerContext
    ) {
        val unit = _gameManager.gameBoard.getUnitAt(fromRow, fromCol) ?: return


        // Execute the movement after animation
        viewModelScope.launch {
            delay(500) // Animation duration

            val moveResult = context.moveUnit(fromRow, fromCol, toRow, toCol, _gameManager)

            if (moveResult) {
                _statusMessage.value = "Unit moved successfully."
                playUnitMovementSound(unit.unitType)

                // Update the selected cell to the new position
                _selectedCell.value = Pair(toRow, toCol)

                // Clear movement destinations since unit has moved
                _validMoveDestinations.value = emptyList()

                // If the unit can still attack, show attack targets
                if (unit.canAttackThisTurn) {
                    _validAttackTargets.value =
                        playerContext.getValidAttackTargets(toRow, toCol, _gameManager)
                    _statusMessage.value = "Select a target to attack or click elsewhere to cancel."
                } else {
                    _validAttackTargets.value = emptyList()

                    // Auto-deselect if no further actions are possible
                    _selectedCell.value = null
                }

                updateAllGameStates()
            } else {
                _statusMessage.value = "Move failed."
                _selectedCell.value = null
                _validMoveDestinations.value = emptyList()
                _validAttackTargets.value = emptyList()
            }
            Log.d("AIMove", "Position : $moveResult Status: $_statusMessage ")

        }
    }

    /**
     * Update UI state when unit is selected
     */
    private fun updateUnitSelectionState(row: Int, col: Int) {
        val unit = _gameManager.gameBoard.getUnitAt(row, col) ?: return

        // Show valid movement destinations if unit can move
        if (playerContext.canUnitMove(row, col, _gameManager)) {
            _validMoveDestinations.value =
                playerContext.getValidMoveDestinations(row, col, _gameManager)
        } else {
            _validMoveDestinations.value = emptyList()
        }

        // Show valid attack targets if unit can attack
        if (unit.canAttackThisTurn) {
            _validAttackTargets.value = playerContext.getValidAttackTargets(row, col, _gameManager)
        } else {
            _validAttackTargets.value = emptyList()
        }
    }

    /**
     * Execute an attack between units with animations
     */
    private fun executeAttack(
        attackerRow: Int,
        attackerCol: Int,
        targetRow: Int,
        targetCol: Int,
        context: PlayerContext
    ) {
        // Get units for the attack
        val attackerUnit = _gameManager.gameBoard.getUnitAt(attackerRow, attackerCol) ?: return
        val targetUnit = _gameManager.gameBoard.getUnitAt(targetRow, targetCol) ?: return

        // Check if this attack has a counter bonus
        val hasCounterBonus = _gameManager.hasCounterBonus(attackerUnit, targetUnit)
        _isCounterBonus.value = hasCounterBonus

        // Get target position for animation
        val targetPos = cellPositions[Pair(targetRow, targetCol)]



        if (targetPos != null) {
            // Start attack animation
            _attackingUnitType.value = attackerUnit.unitType
            _attackTargetPosition.value = targetPos
            _isSimpleAttackVisible.value = true
            // Play Attack Sound
            playUnitAttackSound(attackerUnit.unitType)


            // Attack logic after animation
            viewModelScope.launch {
                delay(400)
                // Reset selection state
                resetSelectionStates()

                // Wait for attack animation
                delay(400)
                _isSimpleAttackVisible.value = false

                // Calculate the actual damage that will be dealt
                val damage = if (hasCounterBonus) {
                    // Apply damage multiplier for counter bonus
                    attackerUnit.attack * 2 // Simplified version - should match GameManager's calculation
                } else {
                    attackerUnit.attack
                }

                // Wait for damage
                delay(600)

                // Store original health before attack for animation
                val originalHealth = targetUnit.health

                // Perform the actual attack using contexts
                val attackResult = _gameManager.executeAttackWithContext(
                    context,
                    attackerRow,
                    attackerCol,
                    targetRow,
                    targetCol
                )

                if (attackResult) {

                    // Animate health decrease
                    val actualDamage = originalHealth - targetUnit.health

                    // Restore health temporarily for animation
                    val tempHealth = targetUnit.health
                    targetUnit.health = originalHealth

                    // Animate health decrease
                    animateHealthDecrease(targetUnit, actualDamage) {
                        // Restore the actual health once animation completes
                        targetUnit.health = tempHealth

                        Log.d("AnimateHealth", "ViewModel")
                        // Reset counter state
                        _isCounterBonus.value = false

                        updateAllGameStates()
                    }
                } else {
                    _statusMessage.value = "Cannot attack with this unit"
                    _interactionMode.value = InteractionMode.DEFAULT
                    _isCounterBonus.value = false
                }
            }
        } else {
            // Fallback if position tracking failed
            // (Similar logic as above but without animations)
            val attackResult = _gameManager.executeAttackWithContext(
                playerContext,
                attackerRow,
                attackerCol,
                targetRow,
                targetCol
            )

            if (attackResult) {

                // Reset states...
                resetSelectionStates()
                _isCounterBonus.value = false

                updateAllGameStates()
            } else {
                _statusMessage.value = "Cannot attack with this unit"
                _interactionMode.value = InteractionMode.DEFAULT
                _isCounterBonus.value = false
            }
        }
    }

    /**
     * Execute an attack against a fortification
     */
    private fun executeAttackAgainstFortification(
        attackerRow: Int,
        attackerCol: Int,
        targetRow: Int,
        targetCol: Int
    ) {
        // Get the attacking unit
        val attackerUnit = _gameManager.gameBoard.getUnitAt(attackerRow, attackerCol) ?: return
        val targetFortHealth =
            _gameManager.gameBoard.getFortificationAt(targetRow, targetCol)?.health
                ?: return

        val targetFort = _gameManager.gameBoard.getFortificationAt(targetRow, targetCol)
            ?: return


        // Check if this attack has a counter bonus
        val hasCounterBonus = _gameManager.hasFortificationCounterBonus(attackerUnit)
        _isCounterBonus.value = hasCounterBonus

        // Get target position for animation
        val targetPos = cellPositions[Pair(targetRow, targetCol)]

        if (targetPos != null) {
            // Start attack animation
            _attackingUnitType.value = attackerUnit.unitType
            _attackTargetPosition.value = targetPos
            _isSimpleAttackVisible.value = true
            playUnitAttackSound(attackerUnit.unitType)

            // Attack logic after animation
            viewModelScope.launch {
                // Wait for attack animation
                delay(800)
                _isSimpleAttackVisible.value = false

                // Calculate the actual damage that will be dealt
                val damage = if (hasCounterBonus) {
                    // Apply damage multiplier for counter bonus
                    attackerUnit.attack * 2 // Should match GameManager's calculation
                } else {
                    attackerUnit.attack
                }

                // Wait for damage
                delay(300)

                if (targetFortHealth <= damage)
                    soundManager.playSound(SoundType.FORTIFICATION_DESTROY)
                // Perform the actual attack
                val attackResult = _gameManager.executeUnitAttackFortification(
                    attackerUnit,
                    targetRow,
                    targetCol
                )

                if (attackResult) {

                    // Animate health decrease
                    val actualDamage = targetFortHealth - targetFort.health

                    // Restore health temporarily for animation
                    val tempHealth = targetFort.health
                    targetFort.health = targetFortHealth

                    // Animate health decrease
                    animateHealthDecrease(targetFort, actualDamage) {
                        // Restore the actual health once animation completes
                        targetFort.health = tempHealth

                        Log.d("AnimateHealth", "ViewModel")
                        // Reset  states
                        resetSelectionStates()

                        // Reset counter state
                        _isCounterBonus.value = false
                        Log.d("FortHealthVDamage", "Damage: $damage  Fort Health: $targetFortHealth")


                        updateAllGameStates()
                    }

                } else {
                    _statusMessage.value = "Cannot attack this fortification"
                    _interactionMode.value = InteractionMode.DEFAULT
                    _isCounterBonus.value = false
                }
            }
        } else {
            // Fallback if position tracking failed
            val attackResult = _gameManager.executeUnitAttackFortification(
                attackerUnit,
                targetRow,
                targetCol
            )

            if (attackResult) {
                // Reset states...
                resetSelectionStates()
                _isCounterBonus.value = false

                updateAllGameStates()
            } else {
                _statusMessage.value = "Cannot attack this fortification"
                _interactionMode.value = InteractionMode.DEFAULT
                _isCounterBonus.value = false
            }
        }
    }

    /**
     * Attack the opponent directly
     */
    fun attackOpponentDirectly() {
        if (!_isPlayerTurn.value || _selectedCell.value == null) return

        val (row, col) = _selectedCell.value!!

        if (playerContext.canAttackOpponentDirectly(row, col, _gameManager)) {
            // Get the unit to determine damage amount
            val attackingUnit = _gameManager.gameBoard.getUnitAt(row, col) ?: return
            val damageAmount = attackingUnit.attack

            // Start the health animation
            animatePlayerHealthDecrease(isPlayer = false, damageAmount = damageAmount) {
                // After animation completes, execute the actual attack
                val attackResult =
                    _gameManager.executeDirectAttackWithContext(playerContext, row, col)

                if (attackResult) {
                    _statusMessage.value = "Direct attack successful!"
                    _selectedCell.value = null
                    _validAttackTargets.value = emptyList()
                    _validMoveDestinations.value = emptyList()
                    updateAllGameStates()
                } else {
                    _statusMessage.value = "Cannot attack opponent directly"
                }
            }
        } else {
            _statusMessage.value = "This unit cannot reach the opponent"
        }
    }

    /**
     * Play a card from the player's hand to a specific position on the board
     */
    fun playCard(cardIndex: Int, targetRow: Int, targetCol: Int) {
        if (!_isPlayerTurn.value) return

        // Check if card can be played
        if (cardIndex >= playerContext.player.hand.size || cardIndex < 0) return
        if (playerContext.player.hand[cardIndex].manaCost > playerContext.player.currentMana) {
            _statusMessage.value = "Not enough mana"
            return
        }

        // Check if the position is valid
        if (!_gameManager.gameBoard.isPositionEmpty(targetRow, targetCol)) {
            _statusMessage.value = "This position is already occupied"
            return
        }

        // Check if the position is in the player's deployment zone
        if (!playerContext.isInDeploymentZone(targetRow, targetCol)) {
            _statusMessage.value = "You can only deploy in your zone"
            return
        }

        // Get animation target position
        val cellPos = cellPositions[Pair(targetRow, targetCol)]

        if (cellPos != null) {
            // Set up animation
            _cardAnimationPosition.value = cellPos
            _isCardAnimationVisible.value = true

            // Actually play the card after animation
            viewModelScope.launch {
                soundManager.playSound(SoundType.CARD_PLAY)
                delay(300) // Wait for animation
                _isCardAnimationVisible.value = false

                // Now actually play the card using the context
                val isCardPlayed = playerContext.playCard(
                    cardIndex,
                    _gameManager,
                    Pair(targetRow, targetCol)
                )

                if (isCardPlayed) {
                    _statusMessage.value = "Card played successfully"
                    updateAllGameStates()
                } else {
                    _statusMessage.value = "Cannot play this card"
                }
            }
        } else {
            // Fallback if position isn't registered - just play without animation
            val isCardPlayed = playerContext.playCard(
                cardIndex,
                _gameManager,
                Pair(targetRow, targetCol)
            )

            if (isCardPlayed) {
                _statusMessage.value = "Card played successfully"
                updateAllGameStates()
            } else {
                _statusMessage.value = "Cannot play this card"
            }
        }
    }

    /**
     * Convenience method to play a card without specifying a target
     * It will find the first available position in the player's deployment zone
     */
    fun playCard(cardIndex: Int) {
        if (!_isPlayerTurn.value) return

        // Check if card can be played
        if (cardIndex >= playerContext.player.hand.size || cardIndex < 0) return
        if (playerContext.player.hand[cardIndex].manaCost > playerContext.player.currentMana) {
            _statusMessage.value = "Not enough mana"
            return
        }

        // Find an empty position in the player's deployment zone
        val position = playerContext.getFirstEmptyPosition()

        if (position == null) {
            _statusMessage.value = "No space to deploy"
            return
        }

        // Play the card to this position
        playCard(cardIndex, position.first, position.second)
    }

    fun endTurn() {
        soundManager.playSound(SoundType.TURN_END)
        _selectedCell.value = null
        _validMoveDestinations.value = emptyList()
        _validAttackTargets.value = emptyList()
        _interactionMode.value = InteractionMode.DEFAULT


        _gameManager.turnManager.endTurn()
        updateAllGameStates()

        // AI turn
        if (!_isPlayerTurn.value) {
            simulateAITurn()
        }
    }

    private fun simulateAITurn() {
        viewModelScope.launch {
            // AI plays cards
            delay(500) // Think time

            // Try to play a card
            for (i in opponentContext.player.hand.indices) {
                val card = opponentContext.player.hand[i]
                if (card.manaCost <= opponentContext.player.currentMana) {
                    val position = opponentContext.getFirstEmptyPosition()
                    if (position != null) {
                        val isCardPlayed = opponentContext.playCard(i, _gameManager, position)
                        if (isCardPlayed) {
                            updateAllGameStates()
                            delay(500) // Animation delay
                            break // Play one card per turn for simplicity
                        }
                    }
                }
            }

            // AI moves units
            delay(500)

            // Get movable units
            val movableUnits = opponentContext.getMovableUnits(_gameManager)

            for (unit in movableUnits) {
                val position = _gameManager.gameBoard.getUnitPosition(unit) ?: continue
                val validMoves = _gameManager.getValidMoveDestinations(unit)

                if (validMoves.isNotEmpty()) {
                    // Simple AI strategy: move toward player's side
                    val bestMove =
                        validMoves.minByOrNull { it.first } // Move to lowest row (toward player)

                    if (bestMove != null) {

                        delay(500) // Animation duration

                        // Execute the move
                        executeMove(
                            position.first,
                            position.second,
                            bestMove.first,
                            bestMove.second,
                            context = opponentContext
                        )
                        Log.d("AIMove", "Position : ${position} Best Move: ${bestMove} ")
                        updateAllGameStates()
                        delay(300)
                    }
                }
            }

            // AI attacks
            delay(500)

            // Get all AI units
            val aiUnits = opponentContext.units

            // Try to attack with each unit
            for (aiUnit in aiUnits.filter { it.canAttackThisTurn }) {
                // Find the unit's position
                val aiUnitPos = _gameManager.gameBoard.getUnitPosition(aiUnit) ?: continue

                // Find a target (simple AI just attacks the first valid target it finds)
                val validTargets = opponentContext.getValidAttackTargets(
                    aiUnitPos.first,
                    aiUnitPos.second,
                    _gameManager
                )

                if (validTargets.isNotEmpty()) {
                    val target = validTargets.first()


                    // Execute attack
                    executeAttack(
                        aiUnitPos.first,
                        aiUnitPos.second,
                        target.first,
                        target.second,
                        opponentContext
                    )
                    updateAllGameStates()
                    delay(800)
                } else if (opponentContext.canAttackOpponentDirectly(
                        aiUnitPos.first,
                        aiUnitPos.second,
                        _gameManager
                    )
                ) {
                    // Direct attack on player
                    val damageAmount = aiUnit.attack
                    viewModelScope.launch {
                        // Animate player health decrease
                        animatePlayerHealthDecrease(isPlayer = true, damageAmount = damageAmount) {
                            // After animation, execute the actual attack
                            _gameManager.executeDirectAttack(aiUnit, 0)
                            updateAllGameStates()
                        }
                        delay(500)
                    }
                }
            }

            // End AI turn
            delay(500)
            _gameManager.turnManager.endTurn()
            updateAllGameStates()
        }
    }

    private fun handleFortificationSelection(
        row: Int,
        col: Int,
        fortification: FortificationCard,
        fortificationOwner: Int
    ) {
        // Only handle player's fortifications
        if (fortificationOwner != 0) return

        // Select the fortification
        _selectedCell.value = Pair(row, col)

        // Play fortification hit sound
        soundManager.playSound(SoundType.FORTIFICATION_TAP)

        // Reset move destinations (fortifications can't move)
        _validMoveDestinations.value = emptyList()

        // Show attack targets for towers
        if (fortification.fortType == FortificationType.TOWER && fortification.canAttackThisTurn) {
            val attackTargets = getValidAttackTargetsForFortification(fortification, row, col)
            _validAttackTargets.value = attackTargets

            if (attackTargets.isNotEmpty()) {
                _statusMessage.value = "Select a target to attack with your tower"
            } else {
                _statusMessage.value = "No valid targets in range"
            }
        } else {
            _validAttackTargets.value = emptyList()

            if (fortification.fortType == FortificationType.TOWER) {
                _statusMessage.value = "This tower has already attacked this turn"
            } else {
                _statusMessage.value = "Walls cannot attack"
            }
        }
    }

    /**
     * Get valid attack targets for a fortification
     */
    private fun getValidAttackTargetsForFortification(
        fortification: FortificationCard,
        row: Int,
        col: Int
    ): List<Pair<Int, Int>> {
        // Only towers can attack
        if (fortification.fortType != FortificationType.TOWER) return emptyList()

        // If tower can't attack this turn, return empty list
        if (!fortification.canAttackThisTurn) return emptyList()

        val attackTargets = mutableListOf<Pair<Int, Int>>()

        // Tower attack range is 2
        val range = 2

        // Check all cells within range
        for (targetRow in 0 until _gameManager.gameBoard.rows) {
            for (targetCol in 0 until _gameManager.gameBoard.columns) {
                // Calculate Manhattan distance
                val distance = abs(row - targetRow) + abs(col - targetCol)

                // Check if within range and not the same cell
                if (distance in 1..range) {
                    // Check if there's an enemy unit at this position
                    val targetUnit = _gameManager.gameBoard.getUnitAt(targetRow, targetCol)
                    if (targetUnit != null) {
                        val targetUnitOwner = _gameManager.gameBoard.getUnitOwner(targetUnit)

                        // Only include enemy units
                        if (targetUnitOwner != null && targetUnitOwner != 0) {
                            attackTargets.add(Pair(targetRow, targetCol))
                        }
                    }
                }
            }
        }

        return attackTargets
    }

    /**
     * Execute an attack from a fortification
     */
    private fun executeFortificationAttack(
        fortificationRow: Int,
        fortificationCol: Int,
        targetRow: Int,
        targetCol: Int
    ) {
        val fortification =
            _gameManager.gameBoard.getFortificationAt(fortificationRow, fortificationCol) ?: return

        // Only towers can attack
        if (fortification.fortType != FortificationType.TOWER) return

        // Get target position for animation
        val targetPos = cellPositions[Pair(targetRow, targetCol)]
        val targetUnit = _gameManager.gameBoard.getUnitAt(targetRow, targetCol) ?: return

        if (targetPos != null) {
            // Start attack animation (use a distinct attack type for towers)
            _attackingUnitType.value = UnitType.MISSILE // Use artillery animation for towers
            _attackTargetPosition.value = targetPos
            _isSimpleAttackVisible.value = true
            soundManager.playSound(SoundType.MISSILE_ATTACK)

            // Attack logic after animation
            viewModelScope.launch {
                // Wait for attack animation
                delay(800)
                _isSimpleAttackVisible.value = false

                // Wait for damage
                delay(300)

                // Store original health before attack for animation
                val originalHealth = targetUnit.health

                // Execute the attack
                val attackResult =
                    _gameManager.executeFortificationAttack(fortification, targetRow, targetCol)

                if (attackResult) {

                    // Animate health decrease
                    val actualDamage = originalHealth - targetUnit.health

                    // Restore health temporarily for animation
                    val tempHealth = targetUnit.health
                    targetUnit.health = originalHealth

                    // Animate health decrease
                    animateHealthDecrease(targetUnit, actualDamage) {
                        // Restore the actual health once animation completes
                        targetUnit.health = tempHealth

                        Log.d("AnimateHealth", "ViewModel")
                        // Reset states
                        _statusMessage.value = "Tower attack successful!"
                        _selectedCell.value = null
                        _validAttackTargets.value = emptyList()
                        _interactionMode.value = InteractionMode.DEFAULT

                        updateAllGameStates()
                    }

                } else {
                    _statusMessage.value = "Attack failed"
                    _interactionMode.value = InteractionMode.DEFAULT
                }
            }
        } else {
            // Fallback if position tracking failed
            val attackResult =
                _gameManager.executeFortificationAttack(fortification, targetRow, targetCol)

            if (attackResult) {
                _statusMessage.value = "Tower attack successful!"
                _selectedCell.value = null
                _validAttackTargets.value = emptyList()
                _interactionMode.value = InteractionMode.DEFAULT
                updateAllGameStates()
            } else {
                _statusMessage.value = "Attack failed"
                _interactionMode.value = InteractionMode.DEFAULT
            }
        }
    }

    // Property for tracking health change for units
    private val _visualHealthMap = mutableStateOf<Map<Card, Int>>(emptyMap())
    val visualHealthMap: State<Map<Card, Int>> = _visualHealthMap

    fun animateHealthDecrease(unit: Card, damageAmount: Int, completion: () -> Unit) {

        val startHealth = when(unit) {
            is FortificationCard -> unit.health
            is UnitCard -> unit.health
            else -> return
        }
        var remainingDamage = damageAmount

        // Initialize with current health
        _visualHealthMap.value = _visualHealthMap.value.toMutableMap().apply {
            put(unit, startHealth)
        }

        viewModelScope.launch {
            // Play a sound at the start of damage
            //   soundManager.playSound(SoundType.DAMAGE_TAP)

            // Animate health decrements one by one
            while (remainingDamage > 0) {
                val currentHealth = _visualHealthMap.value[unit] ?: startHealth
                // Update the visual health (but not the actual unit health yet)
                _visualHealthMap.value = _visualHealthMap.value.toMutableMap().apply {
                    put(unit, currentHealth - 1)
                }

                // Play tick sound for each health point lost
                if (remainingDamage % 2 == 0) { // Play every other tick to avoid sound spam
                    soundManager.playSound(SoundType.DAMAGE_TAP, volume = 0.3f)
                }

                // Small delay between ticks - faster for bigger damage amounts
                val tickDelay = when {
                    damageAmount > 5 -> 80L
                    else -> 120L
                }
                delay(tickDelay)

                remainingDamage--
            }

            // Wait a brief moment for visual clarity before completing
            delay(400)

            // Remove from visual map once animation is complete
            _visualHealthMap.value = _visualHealthMap.value.toMutableMap().apply {
                remove(unit)
            }

            // Call completion handler
            completion()
        }
    }

    // Properties for tracking damaged health of Players
    private val _playerVisualHealth = mutableStateOf<Int?>(null)
    val playerVisualHealth: State<Int?> = _playerVisualHealth

    private val _opponentVisualHealth = mutableStateOf<Int?>(null)
    val opponentVisualHealth: State<Int?> = _opponentVisualHealth

    // Animation method for player health decreases
    fun animatePlayerHealthDecrease(isPlayer: Boolean, damageAmount: Int, completion: () -> Unit) {
        val player = if (isPlayer) _gameManager.players[0] else _gameManager.players[1]
        val startHealth = player.health
        var remainingDamage = damageAmount

        // Set initial visual health
        if (isPlayer) {
            _playerVisualHealth.value = startHealth
        } else {
            _opponentVisualHealth.value = startHealth
        }

        viewModelScope.launch {
            // Play initial hit sound
            soundManager.playSound(SoundType.PLAYER_HIT)

            // Animate health decrements one by one
            while (remainingDamage > 0) {
                val currentVisualHealth = if (isPlayer)
                    _playerVisualHealth.value ?: startHealth
                else
                    _opponentVisualHealth.value ?: startHealth

                // Update the visual health
                if (isPlayer) {
                    _playerVisualHealth.value = currentVisualHealth - 1
                } else {
                    _opponentVisualHealth.value = currentVisualHealth - 1
                }

                // Play tick sound for each health point lost
                if (remainingDamage % 3 == 0) { // Less frequent sounds for player damage
                    soundManager.playSound(SoundType.PLAYER_HIT, volume = 0.2f)
                }

                // Adjust delay based on damage amount
                val tickDelay = when {
                    damageAmount > 10 -> 70L
                    damageAmount > 7 -> 90L
                    else -> 180L
                }
                delay(tickDelay)

                remainingDamage--
            }

            // Small delay after animation completes
            delay(300)

            // Clear visual health values
            if (isPlayer) {
                _playerVisualHealth.value = null
            } else {
                _opponentVisualHealth.value = null
            }

            // Call completion handler
            completion()
        }
    }
    // Add this method to your GameViewModel class
    fun attachBayonet(row: Int, col: Int) {
        // Get the unit at the specified position
        val unit = _gameManager.gameBoard.getUnitAt(row, col) ?: return

        // Check if it's a player's unit
        val unitOwner = _gameManager.gameBoard.getUnitOwner(unit)
        if (unitOwner != 0) return // Only player can attach bayonet for now

        // Check if it's a MUSKET unit
        if (unit.unitType != UnitType.MUSKET) {
            _statusMessage.value = "Only MUSKET units can use bayonets"
            return
        }

        // Check if the unit has already moved or attacked this turn
        if (!playerContext.canUnitMove(row, col, _gameManager)) {
            _statusMessage.value = "Unit has already acted this turn and cannot attach bayonet"
            return
        }

        // Attach the bayonet
        _gameManager.attachBayonetToUnit(unit)

        // Play a sound effect for the bayonet attachment
        soundManager.playSound(SoundType.BAYONET_SHEATHE)

        // Update the message
        _statusMessage.value = "Bayonet attached! Unit transformed to INFANTRY but cannot move or attack this turn"

        // Update game state to reflect changes
        updateAllGameStates()
    }

    private fun resetSelectionStates() {
        _selectedCell.value = null
        _validMoveDestinations.value = emptyList()
        _validAttackTargets.value = emptyList()
        _validDeploymentPositions.value = emptyList()
        _interactionMode.value = InteractionMode.DEFAULT
        _targetingType.value = null
    }

    private fun playUnitAttackSound(unitType: UnitType) {
        val attackSound = when (unitType) {
            UnitType.INFANTRY -> SoundType.INFANTRY_ATTACK
            UnitType.CAVALRY -> SoundType.CAVALRY_ATTACK
            UnitType.ARTILLERY -> SoundType.ARTILLERY_ATTACK
            UnitType.MISSILE -> SoundType.MISSILE_ATTACK
            UnitType.MUSKET -> SoundType.MUSKET_ATTACK
        }
        soundManager.playSound(
            attackSound,
            volume = if (unitType == UnitType.ARTILLERY) 0.5f else 1.0f
        )
    }

    private fun playUnitTapSound(unitType: UnitType) {
        val tapSound = when (unitType) {
            UnitType.CAVALRY -> SoundType.CAVALRY_UNIT_TAP
            else -> SoundType.FOOT_UNIT_TAP
        }
        soundManager.playSound(tapSound)
    }

    private fun playUnitMovementSound(unitType: UnitType) {
        val movementSound = when (unitType) {
            UnitType.CAVALRY -> SoundType.CAVALRY_UNIT_MOVE
            else -> SoundType.FOOT_UNIT_MOVE
        }
        soundManager.playSound(movementSound)
    }

    fun playMenuSoundOne() {
        soundManager.playSound(SoundType.MENU_TAP)
    }

    fun playMenuSoundTwo() {
        soundManager.playSound(SoundType.MENU_TAP_TWO)
    }

    fun playMenuScrollSound() {
        soundManager.playSound(SoundType.MENU_SCROLL)
    }
    fun playStartBattleSound() {
        viewModelScope.launch {
            soundManager.playSound(SoundType.LEVEL_START)
            delay(200)
        }
    }

    private fun playTacticCardSound(tacticCardType: TacticCardType) {
        val effectSound = when (tacticCardType) {
            TacticCardType.BUFF -> SoundType.SPELL_BUFF
            TacticCardType.SPECIAL -> SoundType.SPELL_SPECIAL
            TacticCardType.DEBUFF -> SoundType.SPELL_DEBUFF
            TacticCardType.DIRECT_DAMAGE -> SoundType.SPELL_DIRECT_DAMAGE
            TacticCardType.AREA_EFFECT -> SoundType.SPELL_AREA_EFFECT
        }
        soundManager.playSound(effectSound)
    }
    fun playScreenMusic(screen: String) {
        when (screen) {
            "main_menu" -> musicManager.playMusic(MusicTrack.MAIN_MENU,false)
            "level_selection" -> musicManager.playMusic(MusicTrack.LEVEL_SELECTION,false)
            "deck_editor" -> musicManager.playMusic(MusicTrack.DECK_EDITOR,true)
            else -> musicManager.stopMusic()
        }
    }
    fun stopMusic() {
        musicManager.stopMusic()
    }
    val isMusicMuted: State<Boolean> = musicManager.isMuted

    // Add this method to toggle mute
    fun toggleMusicMute() {
        musicManager.toggleMute()
    }

    override fun onCleared() {
        super.onCleared()
        musicManager.release()
    }


    fun checkGameOver() {
        // Original implementation to check if any player's health is 0
        val playerIsDead = _gameManager.players[0].health <= 0
        val opponentIsDead = _gameManager.players[1].health <= 0

        if (playerIsDead || opponentIsDead) {
            // Set game over state
            _isGameOver.value = true

            // Determine winner
            _isPlayerWinner.value = opponentIsDead && !playerIsDead

            // Play appropriate sound
            if (_isPlayerWinner.value) {
                soundManager.playSound(SoundType.VICTORY)
            } else {
                soundManager.playSound(SoundType.DEFEAT)
            }

            // If in campaign mode, check objectives and update progress
            if (_isInCampaign.value) {
                checkCampaignObjectives()
                completeCampaignLevel()
            }

            // Update game state in manager
            _gameManager.gameState = GameState.FINISHED
        }
    }

}