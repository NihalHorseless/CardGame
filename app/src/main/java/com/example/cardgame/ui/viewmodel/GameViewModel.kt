package com.example.cardgame.ui.viewmodel

import android.annotation.SuppressLint
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
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.repository.CampaignRepository
import com.example.cardgame.data.repository.CardRepository
import com.example.cardgame.game.Board
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
    private val opponentContext: PlayerContext get() = _opponentContext

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

    @SuppressLint("MutableCollectionMutableState")
    private val _currentDeckInfo = mutableStateOf<HashMap<String,Int>?>(null)
    val currentDeckInfo: State<HashMap<String,Int>?> = _currentDeckInfo

    private val _maxMana = mutableIntStateOf(10)
    val maxMana: State<Int> = _maxMana

    init {
        // Load available decks when ViewModel is created
        loadAvailableDecks()
        _gameManager.setEntityDestructionCallback { entity, position ->
            playDeathAnimation(entity, position)
        }
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
    fun loadDeckInfo(deckId: String) {
        viewModelScope.launch {
            _currentDeckInfo.value = cardRepository.getDeckInfo(deckId = deckId)
            Log.d("SelectedDeck",cardRepository.getDeckInfo(deckId = deckId).toString())
        }
        Log.d("SelectedDeck",currentDeckInfo.value.toString())
    }
    private val _availableDeckNames = mutableStateOf<List<String>>(emptyList())
    val availableDeckNames: State<List<String>> = _availableDeckNames

    private fun loadAvailableDeckNames() {
        viewModelScope.launch {
            val deckNames = cardRepository.getAllAvailableDeckNames()
            _availableDeckNames.value = deckNames
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
        _gameManager.players[0].health = level.startingHealth
        _gameManager.players[1].health = when (level.difficulty) {
            Difficulty.EASY -> 30
            Difficulty.MEDIUM -> 40
            Difficulty.HARD -> 50
            Difficulty.LEGENDARY -> 60
        }
        _opponentHealth.intValue = _gameManager.players[1].health
        Log.d("LevelConfig", "${_gameManager.players[1].health}  $opponentHealth")
        // Set starting mana
        _gameManager.players[0].currentMana = level.startingMana
        _gameManager.players[1].currentMana = level.startingMana

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

    fun setMaxMana(mana: Int) {
        _maxMana.intValue = mana
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

            // Apply max mana setting to both players
            _gameManager.players[0].maxMana = _maxMana.intValue
            _gameManager.players[1].maxMana = _maxMana.intValue

            _opponentName.value = "Mediocre Bot"
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
    private fun getValidDeploymentPositions(playerId: Int): List<Pair<Int, Int>> {
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
        Log.d("LevelConfigD", _opponentHealth.intValue.toString())
        // Update player stats
        _playerMana.intValue = _gameManager.players[0].currentMana
        _playerMaxMana.intValue = _gameManager.players[0].maxMana
        _playerHealth.intValue = _gameManager.players[0].health
        _opponentHealth.intValue = _gameManager.players[1].health
        Log.d("LevelConfigD", _opponentHealth.intValue.toString())

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
                viewModelScope.launch {
                    executeFortificationAttack(selectedRow, selectedCol, row, col)
                }

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

                // Check if this attack will kill the target
                val willKillTarget = targetUnit.health <= damage

                // Perform the actual attack using contexts
                val attackResult = _gameManager.executeAttackWithContext(
                    context,
                    attackerRow,
                    attackerCol,
                    targetRow,
                    targetCol
                )

                if (attackResult) {
                    // If the attack killed the target unit, trigger death animation
                    if (willKillTarget) {
                        // Add this position to entities in death animation
                        _entitiesInDeathAnimation.value += Pair(targetRow, targetCol)

                        // Play death animation
                        playDeathAnimation(targetUnit, Pair(targetRow, targetCol))
                    } else {
                        // If not killed, just animate the health decrease
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
                // Check if this attack killed the target
                if (targetUnit.isDead()) {
                    // Play death animation
                    playDeathAnimation(targetUnit, Pair(targetRow, targetCol))
                }

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
    private fun playCard(cardIndex: Int, targetRow: Int, targetCol: Int) {
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
    private fun playCard(cardIndex: Int) {
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

    private suspend fun playCardsRandomly(context: PlayerContext) {
        val player = context.player

        // Keep trying to play cards until we can't
        var cardPlayed = true
        while (cardPlayed && player.hand.isNotEmpty()) {
            cardPlayed = false

            // Shuffle hand to play randomly
            val shuffledIndices = player.hand.indices.shuffled()

            for (cardIndex in shuffledIndices) {
                if (cardIndex >= player.hand.size) continue

                val card = player.hand[cardIndex]
                if (card.manaCost > player.currentMana) continue

                when (card) {
                    is UnitCard -> {
                        // Deploy based on unit type
                        val targetRow = when (card.unitType) {
                            UnitType.CAVALRY, UnitType.INFANTRY -> 1 // Front row for melee
                            UnitType.ARTILLERY, UnitType.MISSILE, UnitType.MUSKET -> 0 // Back row for ranged
                        }

                        val deployed = deployUnitEasy(context, cardIndex, targetRow)
                        if (deployed) {
                            cardPlayed = true
                            break
                        }
                    }
                    is FortificationCard -> {
                        // Deploy fortifications on first row (row 0 for AI) with ranged units
                        val deployed = deployFortificationEasy(context, cardIndex, targetRow = 0)
                        if (deployed) {
                            cardPlayed = true
                            break
                        }
                    }

                }
            }
        }
    }

    /**
     * Deploy unit randomly on specified row, with smart fallback
     */
    private suspend fun deployUnitEasy(context: PlayerContext, cardIndex: Int, targetRow: Int): Boolean {
        if (context.player.hand[cardIndex] !is UnitCard) return false

        // First, try to deploy on the preferred row
        val availableColumns = (0 until gameManager.gameBoard.columns)
            .filter { col ->
                gameManager.gameBoard.isPositionCompletelyEmpty(targetRow, col)
            }
            .shuffled() // Randomize deployment within the row

        for (col in availableColumns) {
            val deployed = context.playCard(cardIndex, gameManager, Pair(targetRow, col))
            if (deployed) {
                updateAllGameStates()
                delay(500) // Animation delay
                return true
            }
        }

        // If preferred row is full, try the other row based on unit type
        val alternateRow = if (targetRow == 0) 1 else 0
        val alternateColumns = (0 until gameManager.gameBoard.columns)
            .filter { col ->
                gameManager.gameBoard.isPositionCompletelyEmpty(alternateRow, col)
            }
            .shuffled()

        for (col in alternateColumns) {
            val deployed = context.playCard(cardIndex, gameManager, Pair(alternateRow, col))
            if (deployed) {
                updateAllGameStates()
                delay(500) // Animation delay
                return true
            }
        }

        // If both rows have no space, try any valid deployment position
        val allValidPositions = gameManager.getValidDeploymentPositions(context.player.id).shuffled()
        for (pos in allValidPositions) {
            val deployed = context.playCard(cardIndex, gameManager, pos)
            if (deployed) {
                updateAllGameStates()
                delay(500) // Animation delay
                return true
            }
        }

        return false
    }

    /**
     * Deploy fortification on first row with ranged units
     */
    private suspend fun deployFortificationEasy(context: PlayerContext, cardIndex: Int, targetRow: Int): Boolean {
        if (context.player.hand[cardIndex] !is FortificationCard) return false

        // Fortifications go on row 0 with ranged units
        val availableColumns = (0 until gameManager.gameBoard.columns)
            .filter { col ->
                gameManager.gameBoard.isPositionCompletelyEmpty(targetRow, col)
            }
            .shuffled()

        // Try to place fortifications spread out
        for (col in availableColumns) {
            val deployed = context.playCard(cardIndex, gameManager, Pair(targetRow, col))
            if (deployed) {
                updateAllGameStates()
                delay(500) // Animation delay
                return true
            }
        }

        // If first row is full, try second row
        val alternateRow = 1
        val alternateColumns = (0 until gameManager.gameBoard.columns)
            .filter { col ->
                gameManager.gameBoard.isPositionCompletelyEmpty(alternateRow, col)
            }
            .shuffled()

        for (col in alternateColumns) {
            val deployed = context.playCard(cardIndex, gameManager, Pair(alternateRow, col))
            if (deployed) {
                updateAllGameStates()
                delay(500) // Animation delay
                return true
            }
        }

        // Final fallback: any valid position
        val allValidPositions = gameManager.getValidDeploymentPositions(context.player.id).shuffled()
        for (pos in allValidPositions) {
            val deployed = context.playCard(cardIndex, gameManager, pos)
            if (deployed) {
                updateAllGameStates()
                delay(500) // Animation delay
                return true
            }
        }

        return false
    }

    private suspend fun moveAIUnitsAggressively(context: PlayerContext, board: Board) {
        val movableUnits = context.getMovableUnits(gameManager)

        // Check if opponent has no units left
        val opponentHasUnits = board.getPlayerUnits(0).isNotEmpty() || _gameManager.players[0].hand.isNotEmpty()

        // If opponent has no units, move all units to row 4 for direct attack
        if (!opponentHasUnits) {
            moveAllUnitsToRow4(movableUnits, board, context)
            return
        }

        // Otherwise, move units based on their type with new aggressive positioning
        val artilleryUnits = movableUnits.filter { it.unitType == UnitType.ARTILLERY }
        val musketUnits = movableUnits.filter { it.unitType == UnitType.MUSKET }
        val infantryUnits = movableUnits.filter { it.unitType == UnitType.INFANTRY }
        val cavalryUnits = movableUnits.filter { it.unitType == UnitType.CAVALRY }

        // Move units in order: Artillery first (back), then Muskets, then aggressive units
        moveArtilleryToBackRows(artilleryUnits, board, context)
        moveMusketToMidRows(musketUnits, board, context)
        moveAggressiveUnitsToFront(infantryUnits + cavalryUnits, board, context)
    }

    /**
     * Move all units to row 4 when opponent has no units left
     */
    private suspend fun moveAllUnitsToRow4(units: List<UnitCard>, board: Board, context: PlayerContext) {
        val sortedUnits = units.sortedBy { unit ->
            board.getUnitPosition(unit)?.second ?: 0
        }

        for (unit in sortedUnits) {
            val currentPos = board.getUnitPosition(unit) ?: continue
            val validMoves = gameManager.getValidMoveDestinations(unit)

            if (validMoves.isEmpty()) continue

            // Find the move that gets closest to row 4
            val bestMove = validMoves.minByOrNull { move ->
                // Priority: Get as close to row 4 as possible
                abs(move.first - 4) * 100 +
                        // Secondary: maintain column spread
                        abs(move.second - currentPos.second)
            }

            bestMove?.let { targetPos ->
                executeMove(
                    currentPos.first,
                    currentPos.second,
                    targetPos.first,
                    targetPos.second,
                    context
                )
                updateAllGameStates()
                delay(600)
            }
        }
    }

    /**
     * Move artillery units to rows 0-1
     */
    private suspend fun moveArtilleryToBackRows(artilleryUnits: List<UnitCard>, board: Board, context: PlayerContext) {
        val sortedUnits = artilleryUnits.sortedBy { unit ->
            board.getUnitPosition(unit)?.second ?: 0
        }

        for (unit in sortedUnits) {
            val currentPos = board.getUnitPosition(unit) ?: continue
            val validMoves = gameManager.getValidMoveDestinations(unit)

            if (validMoves.isEmpty()) continue

            // Ideal rows for artillery: 0-1
            val bestMove = validMoves.minByOrNull { move ->
                val rowScore = when (move.first) {
                    0 -> 0     // Perfect position
                    1 -> 10    // Good position
                    else -> abs(move.first - 0) * 100 // Further is worse
                }

                // Check column spacing
                val columnOccupied = artilleryUnits.any { otherUnit ->
                    if (otherUnit == unit) false
                    else {
                        val otherPos = board.getUnitPosition(otherUnit)
                        otherPos?.second == move.second && otherPos.first in 0..1
                    }
                }

                rowScore + if (columnOccupied) 50 else 0
            }

            bestMove?.let { targetPos ->
                executeMove(
                    currentPos.first,
                    currentPos.second,
                    targetPos.first,
                    targetPos.second,
                    context
                )
                updateAllGameStates()
                delay(600)
            }
        }
    }

    /**
     * Move musket units to rows 1-2
     */
    private suspend fun moveMusketToMidRows(musketUnits: List<UnitCard>, board: Board, context: PlayerContext) {
        val sortedUnits = musketUnits.sortedBy { unit ->
            board.getUnitPosition(unit)?.second ?: 0
        }

        for (unit in sortedUnits) {
            val currentPos = board.getUnitPosition(unit) ?: continue
            val validMoves = gameManager.getValidMoveDestinations(unit)

            if (validMoves.isEmpty()) continue

            // Ideal rows for muskets: 1-2
            val bestMove = validMoves.minByOrNull { move ->
                val rowScore = when (move.first) {
                    1 -> 0     // Perfect position
                    2 -> 0     // Also perfect
                    0 -> 30    // Acceptable but not ideal
                    else -> abs(move.first - 1.5f).toInt() * 100 // Further is worse
                }

                // Check column spacing
                val columnOccupied = musketUnits.any { otherUnit ->
                    if (otherUnit == unit) false
                    else {
                        val otherPos = board.getUnitPosition(otherUnit)
                        otherPos?.second == move.second && otherPos.first in 1..2
                    }
                }

                rowScore + if (columnOccupied) 50 else 0
            }

            bestMove?.let { targetPos ->
                executeMove(
                    currentPos.first,
                    currentPos.second,
                    targetPos.first,
                    targetPos.second,
                    context
                )
                updateAllGameStates()
                delay(600)
            }
        }
    }

    /**
     * Move infantry and cavalry aggressively to rows 2-3
     */
    private suspend fun moveAggressiveUnitsToFront(aggressiveUnits: List<UnitCard>, board: Board, context: PlayerContext) {
        // Sort by type then column - cavalry on flanks, infantry in center
        val sortedUnits = aggressiveUnits.sortedWith(
            compareBy(
                { it.unitType != UnitType.CAVALRY }, // Cavalry first
                { unit ->
                    val pos = board.getUnitPosition(unit)?.second ?: 0
                    if (unit.unitType == UnitType.CAVALRY) {
                        // Cavalry prefers flanks
                        minOf(pos, board.columns - 1 - pos)
                    } else {
                        // Infantry prefers center
                        abs(pos - board.columns / 2)
                    }
                }
            )
        )

        for (unit in sortedUnits) {
            val currentPos = board.getUnitPosition(unit) ?: continue
            val validMoves = gameManager.getValidMoveDestinations(unit)

            if (validMoves.isEmpty()) continue

            // Ideal rows for aggressive units: 2-3
            val bestMove = validMoves.minByOrNull { move ->
                val rowScore = when (move.first) {
                    2 -> 0     // Perfect position
                    3 -> 0     // Also perfect
                    4 -> 20    // Very aggressive but risky
                    1 -> 30    // Acceptable fallback
                    else -> abs(move.first - 2.5f).toInt() * 100
                }

                // Column preference based on unit type
                val columnScore = if (unit.unitType == UnitType.CAVALRY) {
                    // Cavalry prefers flanks for flanking maneuvers
                    when (move.second) {
                        0, board.columns - 1 -> 0 // Flanks are ideal
                        1, board.columns - 2 -> 10 // Near flanks are good
                        else -> 30 // Center is less ideal for cavalry
                    }
                } else {
                    // Infantry prefers to spread evenly with slight center bias
                    val centerDistance = abs(move.second - board.columns / 2)
                    centerDistance * 5
                }

                // Check if position is already occupied by aggressive units
                val positionOccupied = aggressiveUnits.any { otherUnit ->
                    if (otherUnit == unit) false
                    else {
                        val otherPos = board.getUnitPosition(otherUnit)
                        otherPos == Pair(move.first, move.second)
                    }
                }

                rowScore + columnScore + if (positionOccupied) 100 else 0
            }

            bestMove?.let { targetPos ->
                executeMove(
                    currentPos.first,
                    currentPos.second,
                    targetPos.first,
                    targetPos.second,
                    context
                )
                updateAllGameStates()
                delay(600)
            }
        }
    }
    private suspend fun simulateAIAttack() {
        // Get all AI units that can attack
        val aiUnits = opponentContext.units.filter { it.canAttackThisTurn }

        // Process each unit's attack sequentially
        for (aiUnit in aiUnits) {
            // Check if unit can still attack (in case game state changed)
            if (!aiUnit.canAttackThisTurn) continue

            // Find the unit's position
            val aiUnitPos = _gameManager.gameBoard.getUnitPosition(aiUnit) ?: continue

            // Find valid targets
            val validTargets = opponentContext.getValidAttackTargets(
                aiUnitPos.first,
                aiUnitPos.second,
                _gameManager
            )

            if (validTargets.isNotEmpty()) {
                // AI strategy: Attack the weakest target first
                val targetWithHealth = validTargets.mapNotNull { target ->
                    val targetEnemy = _gameManager.gameBoard.getUnitAt(target.first, target.second)?:_gameManager.gameBoard.getFortificationAt(target.first, target.second)
                    Log.d("TARGAY","${targetEnemy?.name}")
                    when (targetEnemy){
                        is FortificationCard -> Pair(target, targetEnemy.health)
                        is UnitCard -> Pair(target, targetEnemy.health)
                        else -> targetEnemy?.let { Pair(target, it.manaCost) } // Honestly didn't know what to fill it in with
                    }
                }.sortedBy { it.second } // Sort by health (lowest first)

                val bestTarget = targetWithHealth.firstOrNull()?.first ?: validTargets.first()

                // Execute attack and wait for it to complete
                if(_gameManager.gameBoard.getUnitAt(bestTarget.first,bestTarget.second) != null)
                    executeAIAttack(aiUnitPos.first,aiUnitPos.second,bestTarget.first, bestTarget.second, opponentContext)
                else
                    executeAIAttackAgainstFortification(aiUnitPos.first,aiUnitPos.second,bestTarget.first, bestTarget.second)
                // Add delay between attacks for visual clarity
                delay(500)

            } else if (opponentContext.canAttackOpponentDirectly(
                    aiUnitPos.first,
                    aiUnitPos.second,
                    _gameManager
                )
            ) {
                // Direct attack on player
                val damageAmount = aiUnit.attack

                // Animate player health decrease and wait for completion
                animatePlayerHealthDecreaseForAI(true, damageAmount)

                // Execute the actual attack
                _gameManager.executeDirectAttack(aiUnit, 0)
                updateAllGameStates()

                // Add delay after direct attack
                delay(500)
            }
        }

        // NEW: Attack with AI fortifications
        val aiFortifications = opponentContext.fortifications.filter {
            it.fortType == FortificationType.TOWER && it.canAttackThisTurn
        }

        for (tower in aiFortifications) {
            // Check if tower can still attack (in case game state changed)
            if (!tower.canAttackThisTurn) continue

            // Find the tower's position
            val towerPos = _gameManager.gameBoard.getFortificationPosition(tower) ?: continue

            // Find valid targets for the tower
            val validTargets = getValidAttackTargetsForAIFortification(tower, towerPos.first, towerPos.second)

            if (validTargets.isNotEmpty()) {
                // AI strategy: Attack the weakest target first
                val targetWithHealth = validTargets.mapNotNull { target ->
                    val targetUnit = _gameManager.gameBoard.getUnitAt(target.first, target.second)
                    targetUnit?.let { Pair(target, it.health) }
                }.sortedBy { it.second } // Sort by health (lowest first)

                val bestTarget = targetWithHealth.firstOrNull()?.first ?: validTargets.first()

                // Execute fortification attack and wait for it to complete
                executeFortificationAttack(towerPos.first, towerPos.second, bestTarget.first, bestTarget.second)

                // Add delay between attacks for visual clarity
                delay(500)
            }
        }
    }

    private fun getValidAttackTargetsForAIFortification(
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
                    // Check if there's an enemy unit at this position (player units for AI)
                    val targetUnit = _gameManager.gameBoard.getUnitAt(targetRow, targetCol)
                    if (targetUnit != null) {
                        val targetUnitOwner = _gameManager.gameBoard.getUnitOwner(targetUnit)

                        // Only include player units (owner == 0)
                        if (targetUnitOwner == 0) {
                            attackTargets.add(Pair(targetRow, targetCol))
                        }
                    }
                }
            }
        }

        return attackTargets
    }

    private suspend fun executeFortificationAttack(
        fortificationRow: Int,
        fortificationCol: Int,
        targetRow: Int,
        targetCol: Int
    ) {
        val fortification = _gameManager.gameBoard.getFortificationAt(fortificationRow, fortificationCol) ?: return
        val targetUnit = _gameManager.gameBoard.getUnitAt(targetRow, targetCol) ?: return

        // Only towers can attack
        if (fortification.fortType != FortificationType.TOWER) return

        // Get target position for animation
        val targetPos = cellPositions[Pair(targetRow, targetCol)] ?: return

        // Start attack animation (use artillery animation for towers)
        _attackingUnitType.value = UnitType.ARTILLERY
        _attackTargetPosition.value = targetPos
        _isSimpleAttackVisible.value = true
        soundManager.playSound(SoundType.ARTILLERY_ATTACK)

        // Wait for attack animation
        delay(800)
        _isSimpleAttackVisible.value = false

        // Wait before applying damage
        delay(300)

        // Store original health
        val originalHealth = targetUnit.health
        val willKillTarget = targetUnit.health <= fortification.attack

        // Execute the attack
        val attackResult = _gameManager.executeFortificationAttack(fortification, targetRow, targetCol)

        if (attackResult) {
            if (willKillTarget) {
                // Add position to death animation tracking
                _entitiesInDeathAnimation.value += Pair(targetRow, targetCol)

                // Play death animation and wait for it
                playDeathAnimationForAI(targetUnit, Pair(targetRow, targetCol))
            } else {
                // Animate health decrease and wait
                val actualDamage = originalHealth - targetUnit.health
                val tempHealth = targetUnit.health
                targetUnit.health = originalHealth

                // Use the suspending version of health animation
                animateHealthDecreaseForAI(targetUnit, actualDamage)

                // Restore actual health
                targetUnit.health = tempHealth
            }

            updateAllGameStates()
        }
    }
    private suspend fun executeAIAttackAgainstFortification(
        attackerRow: Int,
        attackerCol: Int,
        targetRow: Int,
        targetCol: Int
    ) {
        val attackerUnit = _gameManager.gameBoard.getUnitAt(attackerRow, attackerCol) ?: return
        val targetFort = _gameManager.gameBoard.getFortificationAt(targetRow, targetCol) ?: return

        // Check if this attack has a counter bonus
        val hasCounterBonus = _gameManager.hasFortificationCounterBonus(attackerUnit)
        _isCounterBonus.value = hasCounterBonus

        // Get target position for animation
        val targetPos = cellPositions[Pair(targetRow, targetCol)] ?: return

        // Start attack animation
        _attackingUnitType.value = attackerUnit.unitType
        _attackTargetPosition.value = targetPos
        _isSimpleAttackVisible.value = true
        playUnitAttackSound(attackerUnit.unitType)

        // Wait for attack animation
        delay(800)
        _isSimpleAttackVisible.value = false

        // Calculate damage
        val damage = if (hasCounterBonus) attackerUnit.attack * 2 else attackerUnit.attack

        // Wait before applying damage
        delay(300)

        // Store original health
        val originalHealth = targetFort.health
        val willKillTarget = targetFort.health <= damage

        // Perform the actual attack
        val attackResult = _gameManager.executeUnitAttackFortification(
            attackerUnit,
            targetRow,
            targetCol
        )

        if (attackResult) {
            if (willKillTarget) {
                // Add position to death animation tracking
                _entitiesInDeathAnimation.value += Pair(targetRow, targetCol)

                // Play death animation and wait for it
                playDeathAnimationForAI(targetFort, Pair(targetRow, targetCol))
            } else {
                // Animate health decrease and wait
                val actualDamage = originalHealth - targetFort.health
                val tempHealth = targetFort.health
                targetFort.health = originalHealth

                // Use the suspending version of health animation
                animateHealthDecreaseForAI(targetFort, actualDamage)

                // Restore actual health
                targetFort.health = tempHealth
            }

            // Reset counter state
            _isCounterBonus.value = false
            updateAllGameStates()
        }
    }

    // Helper function to execute attack and wait for all animations to complete
    private suspend fun executeAIAttack(
        attackerRow: Int,
        attackerCol: Int,
        targetRow: Int,
        targetCol: Int,
        context: PlayerContext
    ) {
        val attackerUnit = _gameManager.gameBoard.getUnitAt(attackerRow, attackerCol) ?: return
        val targetUnit =
            _gameManager.gameBoard.getUnitAt(targetRow, targetCol) ?: return

        // Check if this attack has a counter bonus
        val hasCounterBonus = _gameManager.hasCounterBonus(attackerUnit, targetUnit)
        _isCounterBonus.value = hasCounterBonus

        // Get target position for animation
        val targetPos = cellPositions[Pair(targetRow, targetCol)] ?: return

        // Start attack animation
        _attackingUnitType.value = attackerUnit.unitType
        _attackTargetPosition.value = targetPos
        _isSimpleAttackVisible.value = true
        playUnitAttackSound(attackerUnit.unitType)

        // Wait for attack animation
        delay(800)
        _isSimpleAttackVisible.value = false

        // Calculate damage
        val damage = if (hasCounterBonus) attackerUnit.attack * 2 else attackerUnit.attack

        // Wait before applying damage
        delay(300)

        // Store original health
        val originalHealth = targetUnit.health
        val willKillTarget = targetUnit.health <= damage

        // Perform the actual attack
        val attackResult = _gameManager.executeAttackWithContext(
            context,
            attackerRow,
            attackerCol,
            targetRow,
            targetCol
        )

        if (attackResult) {
            if (willKillTarget) {
                // Add position to death animation tracking
                _entitiesInDeathAnimation.value += Pair(targetRow, targetCol)

                // Play death animation and wait for it
                playDeathAnimationForAI(targetUnit, Pair(targetRow, targetCol))
            } else {
                // Animate health decrease and wait
                val actualDamage = originalHealth - targetUnit.health
                val tempHealth = targetUnit.health
                targetUnit.health = originalHealth

                // Use a suspending version of health animation
                animateHealthDecreaseForAI(targetUnit, actualDamage)

                // Restore actual health
                targetUnit.health = tempHealth
            }

            // Reset counter state
            _isCounterBonus.value = false
            updateAllGameStates()
        }
    }

    // Suspending version of animateHealthDecrease
    private suspend fun animateHealthDecreaseForAI(unit: Card, damageAmount: Int) {
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

        // Animate health decrements
        while (remainingDamage > 0) {
            val currentHealth = _visualHealthMap.value[unit] ?: startHealth
            _visualHealthMap.value = _visualHealthMap.value.toMutableMap().apply {
                put(unit, currentHealth - 1)
            }

            if (remainingDamage % 2 == 0) {
                soundManager.playSound(SoundType.DAMAGE_TAP, volume = 0.3f)
            }

            val tickDelay = when {
                damageAmount > 5 -> 80L
                else -> 120L
            }
            delay(tickDelay)
            remainingDamage--
        }

        // Wait before completing
        delay(400)

        // Remove from visual map
        _visualHealthMap.value = _visualHealthMap.value.toMutableMap().apply {
            remove(unit)
        }
    }

    // Suspending version for player health animation
    private suspend fun animatePlayerHealthDecreaseForAI(isPlayer: Boolean, damageAmount: Int) {
        val player = if (isPlayer) _gameManager.players[0] else _gameManager.players[1]
        val startHealth = player.health
        var remainingDamage = damageAmount

        // Set initial visual health
        if (isPlayer) {
            _playerVisualHealth.value = startHealth
        } else {
            _opponentVisualHealth.value = startHealth
        }

        // Play initial hit sound
        soundManager.playSound(SoundType.PLAYER_HIT)

        // Animate health decrements
        while (remainingDamage > 0) {
            val currentVisualHealth = if (isPlayer)
                _playerVisualHealth.value ?: startHealth
            else
                _opponentVisualHealth.value ?: startHealth

            if (isPlayer) {
                _playerVisualHealth.value = currentVisualHealth - 1
            } else {
                _opponentVisualHealth.value = currentVisualHealth - 1
            }

            if (remainingDamage % 3 == 0) {
                soundManager.playSound(SoundType.PLAYER_HIT, volume = 0.2f)
            }

            val tickDelay = when {
                damageAmount > 10 -> 70L
                damageAmount > 7 -> 90L
                else -> 180L
            }
            delay(tickDelay)
            remainingDamage--
        }

        // Small delay after animation
        delay(300)

        // Clear visual health values
        if (isPlayer) {
            _playerVisualHealth.value = null
        } else {
            _opponentVisualHealth.value = null
        }
    }

    // Suspending version of death animation
    private suspend fun playDeathAnimationForAI(entity: Card, position: Pair<Int, Int>) {
        val cellPos = cellPositions[position] ?: return

        when (entity) {
            is UnitCard -> {
                _deathEntityType.value = entity.unitType
                soundManager.playSound(SoundType.UNIT_DEATH)
            }
            is FortificationCard -> {
                _deathEntityType.value = entity.fortType
                soundManager.playSound(SoundType.FORTIFICATION_DESTROY)
            }
            else -> return
        }

        // Set animation position and make it visible
        _deathAnimationPosition.value = cellPos
        _isDeathAnimationVisible.value = true

        // Wait for animation to complete
        delay(1200)

        // Hide animation
        _isDeathAnimationVisible.value = false

        // Remove entity from game
        when (entity) {
            is UnitCard -> {
                val unitPos = _gameManager.gameBoard.getUnitPosition(entity)
                if (unitPos != null) {
                    _gameManager.gameBoard.removeUnit(unitPos.first, unitPos.second)
                }
            }
            is FortificationCard -> {
                val fortPos = _gameManager.gameBoard.getFortificationPosition(entity)
                if (fortPos != null) {
                    _gameManager.gameBoard.removeFortification(fortPos.first, fortPos.second)
                }
            }
        }

        // Remove from tracking
        _entitiesInDeathAnimation.value -= position
    }
    private suspend fun playAITacticCards(context: PlayerContext) {
        val player = context.player

        // Keep trying to play tactic cards until we can't
        var cardPlayed = true
        while (cardPlayed && player.hand.isNotEmpty()) {
            cardPlayed = false

            // Look for tactic cards in hand
            val tacticCardsWithIndex = player.hand.mapIndexedNotNull { index, card ->
                if (card is TacticCard && card.manaCost <= player.currentMana) {
                    index to card
                } else null
            }

            if (tacticCardsWithIndex.isEmpty()) break

            // Try to play each tactic card with smart targeting
            for ((_, tacticCard) in tacticCardsWithIndex) {
                val played = playAITacticCardSmart(context, tacticCard)
                if (played) {
                    cardPlayed = true
                    updateAllGameStates()
                    delay(800) // Animation delay
                    break // Re-evaluate hand after playing a card
                }
            }
        }
    }

    // Add this smart tactic card playing function
    private suspend fun playAITacticCardSmart(context: PlayerContext, card: TacticCard): Boolean {
        when (card.targetType) {
            TargetType.NONE -> {
                // Cards like "Draw 2" that don't need targets - always play these
                val success = card.play(context.player, gameManager, null)
                if (success) {
                    // Show effect animation if applicable
                    when (card.cardType) {
                        TacticCardType.SPECIAL -> {
                            playTacticCardSound(card.cardType)
                            delay(500)
                        }
                        else -> {}
                    }
                }
                return success
            }

            TargetType.ENEMY -> {
                // Find the best enemy target based on card type
                val enemyTargets = getAllPlayerTargets() // Get player's units/fortifications

                if (enemyTargets.isEmpty()) return false

                val bestTarget = when (card.cardType) {
                    TacticCardType.DIRECT_DAMAGE -> {
                        // Target the unit with lowest health that can be killed
                        findBestDamageTarget(enemyTargets, card)
                    }
                    TacticCardType.DEBUFF -> {
                        // Target the strongest enemy unit
                        findStrongestTarget(enemyTargets)
                    }
                    else -> enemyTargets.randomOrNull()
                }

                bestTarget?.let { target ->
                    val linearPos = target.first * gameManager.gameBoard.columns + target.second
                    val success = card.play(context.player, gameManager, linearPos)

                    if (success) {
                        // Show effect animation
                        playTacticCardSound(card.cardType)
                        val targetPos = cellPositions[target]
                        if (targetPos != null) {
                            _tacticEffectPosition.value = targetPos
                            _tacticEffectType.value = card.cardType
                            _isTacticEffectVisible.value = true
                            delay(1200) // Wait for animation
                            _isTacticEffectVisible.value = false
                        }
                        return true
                    }
                }
            }

            TargetType.FRIENDLY -> {
                // Find the best friendly target based on card type
                val friendlyTargets = getAllAITargets() // Get AI's units/fortifications

                if (friendlyTargets.isEmpty()) return false

                val bestTarget = when (card.cardType) {
                    TacticCardType.BUFF -> {
                        // Buff the strongest unit or one about to attack
                        findBestBuffTarget(friendlyTargets)
                    }
                    else -> friendlyTargets.randomOrNull()
                }

                bestTarget?.let { target ->
                    val linearPos = target.first * gameManager.gameBoard.columns + target.second
                    val success = card.play(context.player, gameManager, linearPos)

                    if (success) {
                        // Show effect animation
                        playTacticCardSound(card.cardType)
                        val targetPos = cellPositions[target]
                        if (targetPos != null) {
                            _tacticEffectPosition.value = targetPos
                            _tacticEffectType.value = card.cardType
                            _isTacticEffectVisible.value = true
                            delay(1200) // Wait for animation
                            _isTacticEffectVisible.value = false
                        }
                        return true
                    }
                }
            }

            TargetType.BOARD, TargetType.ANY -> {
                // For area effects, find optimal position
                val bestPosition = when (card.cardType) {
                    TacticCardType.AREA_EFFECT -> {
                        // Find position that hits most enemies
                        findBestAreaEffectPosition()
                    }
                    else -> {
                        // Random valid position
                        val allPositions = mutableListOf<Pair<Int, Int>>()
                        for (row in 0 until gameManager.gameBoard.rows) {
                            for (col in 0 until gameManager.gameBoard.columns) {
                                allPositions.add(Pair(row, col))
                            }
                        }
                        allPositions.randomOrNull()
                    }
                }

                bestPosition?.let { pos ->
                    val linearPos = pos.first * gameManager.gameBoard.columns + pos.second
                    val success = card.play(context.player, gameManager, linearPos)

                    if (success) {
                        // Show effect animation
                        playTacticCardSound(card.cardType)
                        val targetPos = cellPositions[pos]
                        if (targetPos != null) {
                            _tacticEffectPosition.value = targetPos
                            _tacticEffectType.value = card.cardType
                            _isTacticEffectVisible.value = true
                            delay(1200) // Wait for animation
                            _isTacticEffectVisible.value = false
                        }
                        return true
                    }
                }
            }
        }

        return false
    }

// Helper functions for smart targeting

    private fun getAllPlayerTargets(): List<Pair<Int, Int>> {
        val targets = mutableListOf<Pair<Int, Int>>()

        for (row in 0 until gameManager.gameBoard.rows) {
            for (col in 0 until gameManager.gameBoard.columns) {
                // Check for player units
                val unit = gameManager.gameBoard.getUnitAt(row, col)
                if (unit != null && gameManager.gameBoard.getUnitOwner(unit) == 0) {
                    targets.add(Pair(row, col))
                    continue
                }

                // Check for player fortifications
                val fort = gameManager.gameBoard.getFortificationAt(row, col)
                if (fort != null && gameManager.gameBoard.getFortificationOwner(fort) == 0) {
                    targets.add(Pair(row, col))
                }
            }
        }

        return targets
    }

    private fun getAllAITargets(): List<Pair<Int, Int>> {
        val targets = mutableListOf<Pair<Int, Int>>()

        for (row in 0 until gameManager.gameBoard.rows) {
            for (col in 0 until gameManager.gameBoard.columns) {
                // Check for AI units
                val unit = gameManager.gameBoard.getUnitAt(row, col)
                if (unit != null && gameManager.gameBoard.getUnitOwner(unit) == 1) {
                    targets.add(Pair(row, col))
                    continue
                }

                // Check for AI fortifications
                val fort = gameManager.gameBoard.getFortificationAt(row, col)
                if (fort != null && gameManager.gameBoard.getFortificationOwner(fort) == 1) {
                    targets.add(Pair(row, col))
                }
            }
        }

        return targets
    }

    private fun findBestDamageTarget(targets: List<Pair<Int, Int>>, card: TacticCard): Pair<Int, Int>? {
        // Estimate damage from card (this is a simplified approach)
        val estimatedDamage = when (card.name.lowercase()) {
            "fireball" -> 4
            "lightning bolt" -> 3
            "magic missile" -> 2
            else -> 3 // Default estimate
        }

        // Find units that can be killed by this damage
        val killableTargets = targets.filter { target ->
            val unit = gameManager.gameBoard.getUnitAt(target.first, target.second)
            val fort = gameManager.gameBoard.getFortificationAt(target.first, target.second)

            when {
                unit != null -> unit.health <= estimatedDamage
                fort != null -> fort.health <= estimatedDamage
                else -> false
            }
        }

        // If we can kill something, prioritize that
        if (killableTargets.isNotEmpty()) {
            // Among killable targets, prioritize high-value units
            return killableTargets.maxByOrNull { target ->
                val unit = gameManager.gameBoard.getUnitAt(target.first, target.second)
                unit?.attack ?: 0
            }
        }

        // Otherwise, target the lowest health enemy
        return targets.minByOrNull { target ->
            val unit = gameManager.gameBoard.getUnitAt(target.first, target.second)
            val fort = gameManager.gameBoard.getFortificationAt(target.first, target.second)

            when {
                unit != null -> unit.health
                fort != null -> fort.health
                else -> Int.MAX_VALUE
            }
        }
    }

    private fun findStrongestTarget(targets: List<Pair<Int, Int>>): Pair<Int, Int>? {
        return targets.maxByOrNull { target ->
            val unit = gameManager.gameBoard.getUnitAt(target.first, target.second)
            unit?.attack ?: 0
        }
    }

    private fun findBestBuffTarget(targets: List<Pair<Int, Int>>): Pair<Int, Int>? {
        // Prioritize units that can attack this turn
        val attackingUnits = targets.filter { target ->
            val unit = gameManager.gameBoard.getUnitAt(target.first, target.second)
            unit?.canAttackThisTurn == true
        }

        if (attackingUnits.isNotEmpty()) {
            // Among attacking units, buff the strongest
            return attackingUnits.maxByOrNull { target ->
                val unit = gameManager.gameBoard.getUnitAt(target.first, target.second)
                unit?.attack ?: 0
            }
        }

        // Otherwise, buff the unit with highest attack
        return targets.maxByOrNull { target ->
            val unit = gameManager.gameBoard.getUnitAt(target.first, target.second)
            unit?.attack ?: 0
        }
    }

    private fun findBestAreaEffectPosition(): Pair<Int, Int>? {
        var bestPosition: Pair<Int, Int>? = null
        var maxEnemies = 0

        // Check each position on the board
        for (centerRow in 0 until gameManager.gameBoard.rows) {
            for (centerCol in 0 until gameManager.gameBoard.columns) {
                var enemyCount = 0

                // Check 3x3 area around this position (radius 1)
                for (row in (centerRow - 1)..(centerRow + 1)) {
                    for (col in (centerCol - 1)..(centerCol + 1)) {
                        if (row in 0 until gameManager.gameBoard.rows &&
                            col in 0 until gameManager.gameBoard.columns) {

                            // Check for player units
                            val unit = gameManager.gameBoard.getUnitAt(row, col)
                            if (unit != null && gameManager.gameBoard.getUnitOwner(unit) == 0) {
                                enemyCount++
                            }

                            // Check for player fortifications
                            val fort = gameManager.gameBoard.getFortificationAt(row, col)
                            if (fort != null && gameManager.gameBoard.getFortificationOwner(fort) == 0) {
                                enemyCount++
                            }
                        }
                    }
                }

                // Update best position if this hits more enemies
                if (enemyCount > maxEnemies) {
                    maxEnemies = enemyCount
                    bestPosition = Pair(centerRow, centerCol)
                }
            }
        }

        return bestPosition
    }

    private fun simulateAITurn() {
        viewModelScope.launch {

            delay(500)
            playAITacticCards(opponentContext)

            delay(500)
            playCardsRandomly(opponentContext)

            delay(500)
            moveAIUnitsAggressively(opponentContext,_gameManager.gameBoard)

            delay(500)
            simulateAIAttack()

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

    // Property for tracking health change for units
    private val _visualHealthMap = mutableStateOf<Map<Card, Int>>(emptyMap())
    val visualHealthMap: State<Map<Card, Int>> = _visualHealthMap

    private fun animateHealthDecrease(unit: Card, damageAmount: Int, completion: () -> Unit) {

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
    private fun animatePlayerHealthDecrease(isPlayer: Boolean, damageAmount: Int, completion: () -> Unit) {
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
    // Death animation states and functions
    private val _isDeathAnimationVisible = mutableStateOf(false)
    val isDeathAnimationVisible: State<Boolean> = _isDeathAnimationVisible

    private val _deathEntityType = mutableStateOf<Any>(UnitType.INFANTRY)
    val deathEntityType: State<Any> = _deathEntityType

    private val _deathAnimationPosition = mutableStateOf(Pair(0f, 0f))
    val deathAnimationPosition: State<Pair<Float, Float>> = _deathAnimationPosition

    private val _entitiesInDeathAnimation = mutableStateOf<Set<Pair<Int, Int>>>(emptySet())
    val entitiesInDeathAnimation: State<Set<Pair<Int, Int>>> = _entitiesInDeathAnimation

    // Method to trigger death animation
    private fun playDeathAnimation(entity: Card, position: Pair<Int, Int>) {
        val cellPos = cellPositions[position] ?: return

        // If this entity is already in death animation, don't trigger again
        if (position in _entitiesInDeathAnimation.value) return

        // Add position to tracked positions
        _entitiesInDeathAnimation.value += position

        when (entity) {
            is UnitCard -> {
                _deathEntityType.value = entity.unitType
                // Play appropriate death sound
                soundManager.playSound(SoundType.UNIT_DEATH)
            }
            is FortificationCard -> {
                _deathEntityType.value = entity.fortType
                soundManager.playSound(SoundType.FORTIFICATION_DESTROY)
            }
            else -> return
        }

        // Set animation position and make it visible
        _deathAnimationPosition.value = cellPos
        _isDeathAnimationVisible.value = true

        // Schedule removal from tracking set and game state after animation completes
        viewModelScope.launch {
            // Determine animation duration based on entity type
            val animationDuration = 2000L

            // Wait for full animation duration
            delay(animationDuration)

            // Hide the animation first
            _isDeathAnimationVisible.value = false

            // Now we can safely remove the entity from the game
            when (entity) {
                is UnitCard -> {
                    val unitPos = _gameManager.gameBoard.getUnitPosition(entity)
                    if (unitPos != null) {
                        _gameManager.gameBoard.removeUnit(unitPos.first, unitPos.second)
                    }
                }
                is FortificationCard -> {
                    val fortPos = _gameManager.gameBoard.getFortificationPosition(entity)
                    if (fortPos != null) {
                        _gameManager.gameBoard.removeFortification(fortPos.first, fortPos.second)
                    }
                }
            }

            // Now that entity is removed from game state, we can remove it from our tracking
            _entitiesInDeathAnimation.value -= position

            // Update game state
            updateAllGameStates()
        }
    }

    // Called when animation completes
    fun onDeathAnimationComplete() {
        _isDeathAnimationVisible.value = false
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


    private fun checkGameOver() {
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