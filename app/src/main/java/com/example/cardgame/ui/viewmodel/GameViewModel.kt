package com.example.cardgame.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardgame.data.enum.GameState
import com.example.cardgame.data.enum.InteractionMode
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.repository.CardRepository
import com.example.cardgame.game.GameManager
import com.example.cardgame.game.PlayerContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(private val cardRepository: CardRepository) : ViewModel() {
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

    // Current player ID (default to player 0)
    private val _currentPlayerId = mutableIntStateOf(0)
    val currentPlayerId: State<Int> = _currentPlayerId

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

        // Only handle unit cards for now
        if (card is UnitCard) {
            // Check if player has enough mana
            if (_playerMana.value < card.manaCost) {
                _statusMessage.value = "Not enough mana!"
                return
            }

            // Set interaction mode to card targeting
            _interactionMode.value = InteractionMode.CARD_TARGETING
            _selectedCardIndex.value = cardIndex

            // Get valid deployment positions
            _validDeploymentPositions.value = getValidDeploymentPositions(0) // 0 is player ID

            _statusMessage.value = "Select a position to deploy the unit"
        } else {
            // For now, just play non-unit cards automatically
            playCard(cardIndex)
        }
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
     * Handles clicking on a cell in the unified board
     */
    /**
     * Handles clicking on a cell in the unified board with direct interaction style
     */
    fun onCellClick(row: Int, col: Int) {
        if (!_isPlayerTurn.value) return

        // If in deployment mode, try to place the unit
        if (_interactionMode.value == InteractionMode.CARD_TARGETING) {
            if (_validDeploymentPositions.value.contains(Pair(row, col))) {
                deployCardAtPosition(row, col)
            } else {
                _statusMessage.value = "Cannot deploy unit at this position"
            }
            return
        }

        // Otherwise handle normal unit selection/movement/attack
        val clickedUnit = _gameManager.gameBoard.getUnitAt(row, col)
        val unitOwner = clickedUnit?.let { _gameManager.gameBoard.getUnitOwner(it) }

        // Regular unit selection logic...
        if (_selectedCell.value == null && clickedUnit != null && unitOwner == 0) {
            // Select the unit and immediately show movement and attack options
            _selectedCell.value = Pair(row, col)

            // Show valid attack targets if unit can attack
            if (clickedUnit.canAttackThisTurn) {
                _validAttackTargets.value = playerContext.getValidAttackTargets(row, col, _gameManager)
            } else {
                _validAttackTargets.value = emptyList()
            }

            // Show valid movement destinations if unit can move
            if (playerContext.canUnitMove(row, col, _gameManager)) {
                _validMoveDestinations.value = playerContext.getValidMoveDestinations(row, col, _gameManager)
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

                // Update the selected cell to the new position
                _selectedCell.value = Pair(toRow, toCol)

                // Clear movement destinations since unit has moved
                _validMoveDestinations.value = emptyList()

                // If the unit can still attack, show attack targets
                if (unit.canAttackThisTurn) {
                    _validAttackTargets.value = playerContext.getValidAttackTargets(toRow, toCol, _gameManager)
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
            _validMoveDestinations.value = playerContext.getValidMoveDestinations(row, col, _gameManager)
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
     * Handle cell click when in default interaction mode
     */
    private fun handleDefaultModeClick(row: Int, col: Int, clickedUnit: UnitCard?, unitOwner: Int?) {
        when {
            // Click on own unit - select it and show options
            clickedUnit != null && unitOwner == 0 -> {
                _selectedCell.value = Pair(row, col)

                // Check if unit can attack
                if (clickedUnit.canAttackThisTurn) {
                    _validAttackTargets.value = playerContext.getValidAttackTargets(row, col, _gameManager)
                } else {
                    _validAttackTargets.value = emptyList()
                }

                // Check if unit can move
                if (playerContext.canUnitMove(row, col, _gameManager)) {
                    _validMoveDestinations.value = playerContext.getValidMoveDestinations(row, col, _gameManager)
                    _statusMessage.value = "Unit selected. Click 'Move' or 'Attack' button."
                } else {
                    _validMoveDestinations.value = emptyList()
                    _statusMessage.value = "Unit selected. This unit has already moved."
                }
            }

            // Click on enemy unit - if we have a unit selected, try to attack
            clickedUnit != null && unitOwner == 1 && _selectedCell.value != null -> {
                val (selectedRow, selectedCol) = _selectedCell.value!!
                val attackerUnit = _gameManager.gameBoard.getUnitAt(selectedRow, selectedCol)

                if (attackerUnit != null && Pair(row, col) in _validAttackTargets.value) {
                    executeAttack(selectedRow, selectedCol, row, col)
                } else {
                    _statusMessage.value = "Cannot attack that target."
                }
            }

            // Click on empty cell - deselect
            else -> {
                _selectedCell.value = null
                _validMoveDestinations.value = emptyList()
                _validAttackTargets.value = emptyList()
                _statusMessage.value = ""
            }
        }
    }

    /**
     * Handle cell click when in unit attacking mode
     */
    private fun handleAttackModeClick(row: Int, col: Int) {
        val selectedCell = _selectedCell.value ?: return

        // Check if the clicked cell is a valid attack target
        if (Pair(row, col) in _validAttackTargets.value) {
            executeAttack(selectedCell.first, selectedCell.second, row, col)
        } else {
            // Cancel attack mode
            _interactionMode.value = InteractionMode.DEFAULT
            _statusMessage.value = "Attack canceled."
        }
    }

    /**
     * Handle cell click when in unit moving mode
     */
    private fun handleMoveModeClick(row: Int, col: Int) {
        val selectedCell = _selectedCell.value ?: return

        // Check if the clicked cell is a valid movement destination
        if (Pair(row, col) in _validMoveDestinations.value) {
            executeMove(selectedCell.first, selectedCell.second, row, col)
        } else {
            // Cancel move mode
            _interactionMode.value = InteractionMode.DEFAULT
            _statusMessage.value = "Move canceled."
        }
    }

    /**
     * Handle cell click when in card targeting mode
     */
    private fun handleCardTargetingModeClick(row: Int, col: Int) {
        // This would be implemented when card targeting is added
        _interactionMode.value = InteractionMode.DEFAULT
    }

    /**
     * Switch to movement mode for the selected unit
     */
    fun enterMoveMode() {
        if (!_isPlayerTurn.value) return
        if (_selectedCell.value == null) return

        val (row, col) = _selectedCell.value!!
        val unit = _gameManager.gameBoard.getUnitAt(row, col) ?: return

        if (playerContext.canUnitMove(row, col, _gameManager)) {
            _interactionMode.value = InteractionMode.UNIT_MOVING
            _validMoveDestinations.value = playerContext.getValidMoveDestinations(row, col, _gameManager)
            _statusMessage.value = "Select a destination to move to."
        } else {
            _statusMessage.value = "This unit cannot move."
        }
    }

    /**
     * Switch to attack mode for the selected unit
     */
    fun enterAttackMode() {
        if (!_isPlayerTurn.value) return
        if (_selectedCell.value == null) return

        val (row, col) = _selectedCell.value!!
        val unit = _gameManager.gameBoard.getUnitAt(row, col) ?: return

        if (unit.canAttackThisTurn) {
            _interactionMode.value = InteractionMode.UNIT_ATTACKING
            _validAttackTargets.value = playerContext.getValidAttackTargets(row, col, _gameManager)
            _statusMessage.value = "Select a target to attack."
        } else {
            _statusMessage.value = "This unit cannot attack."
        }
    }

    /**
     * Execute a movement action
     */


    /**
     * Execute an attack between units with animations
     */
    private fun executeAttack(attackerRow: Int, attackerCol: Int, targetRow: Int, targetCol: Int) {
        // Get target position for animation
        val targetPos = cellPositions[Pair(targetRow, targetCol)]
        val attackerUnit = _gameManager.gameBoard.getUnitAt(attackerRow, attackerCol) ?: return

        if (targetPos != null) {
            // Start attack animation
            _attackingUnitType.value = attackerUnit.unitType
            _attackTargetPosition.value = targetPos
            _isSimpleAttackVisible.value = true

            // Attack logic after animation
            viewModelScope.launch {
                // Wait for attack animation
                delay(800)
                _isSimpleAttackVisible.value = false

                // Show damage number
                _damageToShow.intValue = attackerUnit.attack
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
                    _statusMessage.value = "Attack successful!"
                    _selectedCell.value = null
                    _validAttackTargets.value = emptyList()
                    _validMoveDestinations.value = emptyList()
                    _interactionMode.value = InteractionMode.DEFAULT
                    updateAllGameStates()
                } else {
                    _statusMessage.value = "Cannot attack with this unit"
                    _interactionMode.value = InteractionMode.DEFAULT
                }
            }
        } else {
            // Fallback if position tracking failed
            val attackResult = _gameManager.executeAttackWithContext(
                playerContext,
                attackerRow,
                attackerCol,
                targetRow,
                targetCol
            )

            if (attackResult) {
                _statusMessage.value = "Attack successful!"
                _selectedCell.value = null
                _validAttackTargets.value = emptyList()
                _validMoveDestinations.value = emptyList()
                _interactionMode.value = InteractionMode.DEFAULT
                updateAllGameStates()
            } else {
                _statusMessage.value = "Cannot attack with this unit"
                _interactionMode.value = InteractionMode.DEFAULT
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
                    val bestMove = validMoves.minByOrNull { it.first } // Move to lowest row (toward player)

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

                    // Animation
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
                } else if (opponentContext.canAttackOpponentDirectly(aiUnitPos.first, aiUnitPos.second, _gameManager)) {
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

            // Update game state in manager
            _gameManager.gameState = GameState.FINISHED
        }
    }
}