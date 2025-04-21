package com.example.cardgame.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardgame.audio.SoundManager
import com.example.cardgame.audio.SoundType
import com.example.cardgame.data.enum.FortificationType
import com.example.cardgame.data.enum.GameState
import com.example.cardgame.data.enum.InteractionMode
import com.example.cardgame.data.enum.TacticCardType
import com.example.cardgame.data.enum.TargetType
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.repository.CardRepository
import com.example.cardgame.game.GameManager
import com.example.cardgame.game.PlayerContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class GameViewModel(
    private val cardRepository: CardRepository,
    private val soundManager: SoundManager
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
    val gameBoardState: State<Array<Array<UnitCard?>>> = _gameBoardState

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

    private val _playerMana = mutableIntStateOf(0)
    val playerMana: State<Int> = _playerMana

    private val _playerMaxMana = mutableIntStateOf(10)
    val playerMaxMana: State<Int> = _playerMaxMana

    private val _playerHealth = mutableIntStateOf(30)
    val playerHealth: State<Int> = _playerHealth

    private val _opponentHealth = mutableIntStateOf(30)
    val opponentHealth: State<Int> = _opponentHealth

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

    // Movement animation state
    private val _isUnitMovingAnimation = mutableStateOf(false)
    val isUnitMovingAnimation: State<Boolean> = _isUnitMovingAnimation

    private val _moveStartPosition = mutableStateOf(Pair(0f, 0f))
    val moveStartPosition: State<Pair<Float, Float>> = _moveStartPosition

    private val _moveEndPosition = mutableStateOf(Pair(0f, 0f))
    val moveEndPosition: State<Pair<Float, Float>> = _moveEndPosition

    private val _movingUnitType = mutableStateOf(UnitType.INFANTRY)
    val movingUnitType: State<UnitType> = _movingUnitType

    // Damage animation
    private val _isDamageNumberVisible = mutableStateOf(false)
    val isDamageNumberVisible: State<Boolean> = _isDamageNumberVisible

    private val _damageToShow = mutableIntStateOf(0)
    val damageToShow: State<Int> = _damageToShow

    private val _damagePosition = mutableStateOf(Pair(0f, 0f))
    val damagePosition: State<Pair<Float, Float>> = _damagePosition

    private val _isHealingEffect = mutableStateOf(false)
    val isHealingEffect: State<Boolean> = _isHealingEffect

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

    init {
        // Load available decks when ViewModel is created
        loadAvailableDecks()
    }

    /**
     * Loads the list of available decks from the CardRepository
     */
    private fun loadAvailableDecks() {
        viewModelScope.launch {
            val deckNames = cardRepository.getAvailableDeckNames()
            _availableDecks.value = deckNames

            // Set default selections if decks are available
            if (deckNames.isNotEmpty()) {
                // Take the first deck for player and second for opponent by default
                _selectedPlayerDeck.value = deckNames.firstOrNull()
                _selectedOpponentDeck.value = deckNames.getOrNull(1) ?: deckNames.firstOrNull()
            }
        }
    }

    /**
     * Sets the selected deck for the player
     */
    fun setPlayerDeck(deckName: String) {
        _selectedPlayerDeck.value = deckName
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
        return cardRepository.loadDeck(deckName)
    }

    fun registerCellPosition(row: Int, col: Int, x: Float, y: Float) {
        cellPositions[Pair(row, col)] = Pair(x, y)
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

        // Load decks
        val playerDeck = cardRepository.loadDeck(playerDeckName)
        val opponentDeck = cardRepository.loadDeck(opponentDeckName)

        if (playerDeck == null) {
            _statusMessage.value = "Failed to load player deck: $playerDeckName"
            return
        }

        if (opponentDeck == null) {
            _statusMessage.value = "Failed to load opponent deck: $opponentDeckName"
            return
        }

        // Shuffle decks
        playerDeck.shuffle()
        opponentDeck.shuffle()

        // Set player decks
        _gameManager.players[0].setDeck(playerDeck)
        _gameManager.players[1].setDeck(opponentDeck)

        // Start the game
        _gameManager.startGame()
        updateAllGameStates()

        _statusMessage.value = "Game started with decks: $playerDeckName vs $opponentDeckName"
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

        if (_interactionMode.value == InteractionMode.CARD_TARGETING) {
            cancelDeployment()
            return
        }
        // Handle both unit and fortification cards the same way
        if (card is UnitCard || card is FortificationCard) {
            // Check if player has enough mana
            if (_playerMana.intValue < card.manaCost) {
                _statusMessage.value = "Not enough mana!"
                return
            }

            // Set interaction mode to card targeting
            _interactionMode.value = InteractionMode.CARD_TARGETING
            _selectedCardIndex.value = cardIndex

            // Get valid deployment positions
            _validDeploymentPositions.value = getValidDeploymentPositions(0) // 0 is player ID

            _statusMessage.value = "Select a position to deploy"
        } else if (card is TacticCard) {
            handleTacticCardSelection(card, cardIndex)
        } else {
            // For non-unit cards, just play them directly
            playCard(cardIndex)
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

        // Different handling based on card's target type
        when (card.targetType) {
            TargetType.NONE -> {
                // Cards that don't need targets (like card draw) can be played immediately
                val success = card.play(_gameManager.players[0], _gameManager, null)
                if (success) {
                    _statusMessage.value = "${card.name} played successfully"
                    updateAllGameStates()
                } else {
                    _statusMessage.value = "Failed to play ${card.name}"
                }
            }

            else -> {
                // Cards that need targets - switch to targeting mode
                _selectedCardIndex.value = cardIndex
                _interactionMode.value = InteractionMode.CARD_TARGETING

                // Highlight valid targets based on card target type
                _validDeploymentPositions.value = when (card.targetType) {
                    TargetType.FRIENDLY -> getFriendlyTargets()
                    TargetType.ENEMY -> getEnemyTargets()
                    TargetType.BOARD -> getBoardTargets()
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
            _selectedCardIndex.value = null
            _validDeploymentPositions.value = emptyList()
            _interactionMode.value = InteractionMode.DEFAULT

            // Update game state
            updateAllGameStates()
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

    fun cancelDeployment() {
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

        // Update player stats
        _playerMana.intValue = _gameManager.players[0].currentMana
        _playerMaxMana.intValue = _gameManager.players[0].maxMana
        _playerHealth.intValue = _gameManager.players[0].health
        _opponentHealth.intValue = _gameManager.players[1].health

        // Update opponent stats
        _opponentHandSize.intValue = _gameManager.players[1].hand.size

        // Update turn state
        _isPlayerTurn.value = currentPlayer.id == 0
        _gameState.value = _gameManager.gameState

        // Reset selection state
        _selectedCell.value = null
        _validMoveDestinations.value = emptyList()
        _validAttackTargets.value = emptyList()
        _interactionMode.value = InteractionMode.DEFAULT

        // Check for game over
        checkGameOver()
    }

    /**
     * Handles clicking on a cell in the unified board with direct interaction style
     */
    fun onCellClick(row: Int, col: Int) {
        if (!_isPlayerTurn.value) return

        // If in deployment mode, try to place the unit or fortification
        if (_interactionMode.value == InteractionMode.CARD_TARGETING) {
            if (_validDeploymentPositions.value.contains(Pair(row, col))) {
                handleTacticCardTargeting(row, col)
                deployCardAtPosition(row, col)
            } else {
                _statusMessage.value = "Cannot deploy at this position"
            }
            return
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
                        executeAttack(selectedRow, selectedCol, row, col)
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
            executeMove(selectedRow, selectedCol, row, col)
        }
        // If a unit is selected and player clicks on a valid attack target
        else if (_selectedCell.value != null && Pair(row, col) in _validAttackTargets.value) {
            val (selectedRow, selectedCol) = _selectedCell.value!!
            executeAttack(selectedRow, selectedCol, row, col)
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
    private fun executeMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
        val unit = _gameManager.gameBoard.getUnitAt(fromRow, fromCol) ?: return

        // Get start and end positions for animation
        val startPos = cellPositions[Pair(fromRow, fromCol)] ?: return
        val endPos = cellPositions[Pair(toRow, toCol)] ?: return

        // Set up movement animation
        _moveStartPosition.value = startPos
        _moveEndPosition.value = endPos
        _movingUnitType.value = unit.unitType
        _isUnitMovingAnimation.value = true

        // Execute the movement after animation
        viewModelScope.launch {
            delay(500) // Animation duration
            _isUnitMovingAnimation.value = false

            val moveResult = playerContext.moveUnit(fromRow, fromCol, toRow, toCol, _gameManager)

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
        }
    }

    // Other methods remain the same...

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
    private fun executeAttack(attackerRow: Int, attackerCol: Int, targetRow: Int, targetCol: Int) {
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
                // Wait for attack animation
                delay(800)
                _isSimpleAttackVisible.value = false

                // Calculate the actual damage that will be dealt
                val damage = if (hasCounterBonus) {
                    // Apply damage multiplier for counter bonus
                    attackerUnit.attack * 2 // Simplified version - should match GameManager's calculation
                } else {
                    attackerUnit.attack
                }

                // Show damage number
                _damageToShow.intValue = damage
                _damagePosition.value = targetPos
                _isHealingEffect.value = false
                _isDamageNumberVisible.value = true

                // Wait for damage number
                delay(800)
                _isDamageNumberVisible.value = false

                // Perform the actual attack using contexts
                val attackResult = _gameManager.executeAttackWithContext(
                    playerContext,
                    attackerRow,
                    attackerCol,
                    targetRow,
                    targetCol
                )

                if (attackResult) {

                    // Reset selection state
                    _selectedCell.value = null
                    _validAttackTargets.value = emptyList()
                    _validMoveDestinations.value = emptyList()
                    _interactionMode.value = InteractionMode.DEFAULT

                    // Reset counter state
                    _isCounterBonus.value = false

                    updateAllGameStates()
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
                _selectedCell.value = null
                _validAttackTargets.value = emptyList()
                _validMoveDestinations.value = emptyList()
                _interactionMode.value = InteractionMode.DEFAULT
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
        val targetFortHealth = _gameManager.gameBoard.getFortificationAt(targetRow, targetCol)?.health
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

                // Show damage number
                _damageToShow.intValue = damage
                _damagePosition.value = targetPos
                _isHealingEffect.value = false
                _isDamageNumberVisible.value = true

                // Wait for damage number
                delay(800)
                _isDamageNumberVisible.value = false

                // Perform the actual attack
                val attackResult = _gameManager.executeUnitAttackFortification(
                    attackerUnit,
                    targetRow,
                    targetCol
                )

                if (attackResult) {
                    // Reset selection state
                    _selectedCell.value = null
                    _validAttackTargets.value = emptyList()
                    _validMoveDestinations.value = emptyList()
                    _interactionMode.value = InteractionMode.DEFAULT

                    // Reset counter state
                    _isCounterBonus.value = false
                    Log.d("FortHealthVDamage","Damage: $damage  Fort Health: ${targetFortHealth}")

                    if(damage >= targetFortHealth)
                        soundManager.playSound(SoundType.FORTIFICATION_DESTROY)

                    updateAllGameStates()
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
                _selectedCell.value = null
                _validAttackTargets.value = emptyList()
                _validMoveDestinations.value = emptyList()
                _interactionMode.value = InteractionMode.DEFAULT
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
            // Execute direct attack
            val attackResult = _gameManager.executeDirectAttackWithContext(playerContext, row, col)

            if (attackResult) {
                soundManager.playSound(SoundType.PLAYER_HIT)
                _statusMessage.value = "Direct attack successful!"
                _selectedCell.value = null
                _validAttackTargets.value = emptyList()
                _validMoveDestinations.value = emptyList()
                updateAllGameStates()
            } else {
                _statusMessage.value = "Cannot attack opponent directly"
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
                        // Animate the move
                        val startPos = cellPositions[position] ?: continue
                        val endPos = cellPositions[bestMove] ?: continue

                        _moveStartPosition.value = startPos
                        _moveEndPosition.value = endPos
                        _movingUnitType.value = unit.unitType
                        _isUnitMovingAnimation.value = true

                        delay(500) // Animation duration
                        _isUnitMovingAnimation.value = false

                        // Execute the move
                        _gameManager.moveUnit(unit, bestMove.first, bestMove.second)
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

                    // Execute the attack
                    val targetPos = cellPositions[target] ?: continue


                    // Animation and sound effect
                    playUnitAttackSound(aiUnit.unitType)
                    _attackingUnitType.value = aiUnit.unitType
                    _attackTargetPosition.value = targetPos
                    _isSimpleAttackVisible.value = true

                    delay(800)
                    _isSimpleAttackVisible.value = false

                    // Show damage number
                    _damageToShow.intValue = aiUnit.attack
                    _damagePosition.value = targetPos
                    _isHealingEffect.value = false
                    _isDamageNumberVisible.value = true

                    delay(800)
                    _isDamageNumberVisible.value = false

                    // Execute attack
                    _gameManager.executeAttack(aiUnit, target.first, target.second)
                    updateAllGameStates()
                    delay(300)
                } else if (opponentContext.canAttackOpponentDirectly(
                        aiUnitPos.first,
                        aiUnitPos.second,
                        _gameManager
                    )
                ) {
                    // Direct attack on player
                    _gameManager.executeDirectAttack(aiUnit, 0)
                    updateAllGameStates()
                    delay(500)
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

                // Show damage number
                _damageToShow.intValue = fortification.attack
                _damagePosition.value = targetPos
                _isHealingEffect.value = false
                _isDamageNumberVisible.value = true

                // Wait for damage number
                delay(800)
                _isDamageNumberVisible.value = false

                // Execute the attack
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

    private fun playUnitAttackSound(unitType: UnitType){
        val attackSound = when (unitType) {
            UnitType.INFANTRY -> SoundType.INFANTRY_ATTACK
            UnitType.CAVALRY -> SoundType.CAVALRY_ATTACK
            UnitType.ARTILLERY -> SoundType.ARTILLERY_ATTACK
            UnitType.MISSILE -> SoundType.MISSILE_ATTACK
        }
        soundManager.playSound(attackSound, volume = if(unitType == UnitType.ARTILLERY) 0.5f else 1.0f)
    }
    private fun playUnitTapSound(unitType: UnitType){
        val tapSound = when (unitType) {
            UnitType.CAVALRY -> SoundType.CAVALRY_UNIT_TAP
            else -> SoundType.FOOT_UNIT_TAP
        }
        soundManager.playSound(tapSound)
    }

    private fun playUnitMovementSound(unitType: UnitType){
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

    private fun playTacticCardSound(tacticCardType: TacticCardType){
        val effectSound = when (tacticCardType) {
            TacticCardType.BUFF -> SoundType.SPELL_BUFF
            TacticCardType.SPECIAL -> SoundType.SPELL_SPECIAL
            TacticCardType.DEBUFF -> SoundType.SPELL_DEBUFF
            TacticCardType.DIRECT_DAMAGE -> SoundType.SPELL_DIRECT_DAMAGE
            TacticCardType.AREA_EFFECT -> SoundType.SPELL_AREA_EFFECT
        }
        soundManager.playSound(effectSound)
    }

    /**
     * Check for game over condition
     */
    private fun checkGameOver() {
        // Game is over if any player's health is 0 or less
        val playerIsDead = _gameManager.players[0].health <= 0
        val opponentIsDead = _gameManager.players[1].health <= 0

        if (playerIsDead || opponentIsDead) {
            // Set game over state
            _isGameOver.value = true

            // Determine winner
            _isPlayerWinner.value = opponentIsDead && !playerIsDead
            if (isPlayerWinner.value) {
                soundManager.playSound(SoundType.VICTORY)
            }
            else {
                soundManager.playSound(SoundType.DEFEAT)
            }

            // Update game state in manager
            _gameManager.gameState = GameState.FINISHED
        }
    }

}