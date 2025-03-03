package com.example.cardgame.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardgame.data.enum.GameState
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.model.card.EnhancedTacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.model.formation.Formation
import com.example.cardgame.data.repository.CardRepository
import com.example.cardgame.game.GameManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(private val cardRepository: CardRepository) : ViewModel() {

    private val _gameManager = GameManager()
    val gameManager: GameManager get() = _gameManager

    // UI States - same as before
    private val _playerHandState = mutableStateOf<List<Card>>(_gameManager.players[0].hand)
    val playerHandState: State<List<Card>> = _playerHandState

    private val _playerBoardState = mutableStateOf<List<UnitCard?>>(getPlayerBoardUnits(0))
    val playerBoardState: State<List<UnitCard?>> = _playerBoardState

    private val _opponentBoardState = mutableStateOf<List<UnitCard?>>(getPlayerBoardUnits(1))
    val opponentBoardState: State<List<UnitCard?>> = _opponentBoardState

    private val _selectedUnitPosition = mutableStateOf(-1)
    val selectedUnitPosition: State<Int> = _selectedUnitPosition

    private val _playerMana = mutableStateOf(0)
    val playerMana: State<Int> = _playerMana

    private val _playerMaxMana = mutableStateOf(10)
    val playerMaxMana: State<Int> = _playerMaxMana

    private val _playerHealth = mutableStateOf(30)
    val playerHealth: State<Int> = _playerHealth

    private val _opponentHealth = mutableStateOf(30)
    val opponentHealth: State<Int> = _opponentHealth

    private val _isPlayerTurn = mutableStateOf(true)
    val isPlayerTurn: State<Boolean> = _isPlayerTurn

    private val _gameState = mutableStateOf(GameState.NOT_STARTED)
    val gameState: State<GameState> = _gameState

    private val _statusMessage = mutableStateOf("")
    val statusMessage: State<String> = _statusMessage

    // Animation states
    private val _isDeckShuffleVisible = mutableStateOf(false)
    val isDeckShuffleVisible: State<Boolean> = _isDeckShuffleVisible

    private val _currentTacticalCard = mutableStateOf<EnhancedTacticCard?>(null)
    val currentTacticalCard: State<EnhancedTacticCard?> = _currentTacticalCard

    private val _isTacticalCardAnimationVisible = mutableStateOf(false)
    val isTacticalCardAnimationVisible: State<Boolean> = _isTacticalCardAnimationVisible

    private val _lastAttackDamage = mutableStateOf(0)
    val lastAttackDamage: State<Int> = _lastAttackDamage

    private val _playerMaxHealth = mutableStateOf(30)
    val playerMaxHealth: State<Int> = _playerMaxHealth

    private val _opponentMaxHealth = mutableStateOf(30)
    val opponentMaxHealth: State<Int> = _opponentMaxHealth

    private val _isGameOver = mutableStateOf(false)
    val isGameOver: State<Boolean> = _isGameOver

    private val _isPlayerWinner = mutableStateOf(false)
    val isPlayerWinner: State<Boolean> = _isPlayerWinner

    // Available decks
    private val _availableDecks = mutableStateOf<List<String>>(emptyList())
    val availableDecks: State<List<String>> = _availableDecks
    init {
        loadAvailableDecks()
    }

    fun startGame(playerDeckName: String = "roman_deck", opponentDeckName: String = "medieval_deck") {
        // Reset game over state
        _isGameOver.value = false

        // Load decks
        val playerDeck = cardRepository.loadDeck(playerDeckName)
        val opponentDeck = cardRepository.loadDeck(opponentDeckName)

        if (playerDeck == null || opponentDeck == null) {
            _statusMessage.value = "Failed to load decks"
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
    }
    fun getDeckInfo(deckName: String): Deck? {
        return cardRepository.loadDeck(deckName)
    }

    private fun loadAvailableDecks() {
        viewModelScope.launch {
            _availableDecks.value = cardRepository.getAvailableDeckNames()
        }
    }


    private fun getPlayerBoardUnits(playerIndex: Int): List<UnitCard?> {
        val player = _gameManager.players[playerIndex]
        return (0 until player.board.maxSize).map { position ->
            player.board.getUnitAt(position)
        }
    }

    fun playCard(cardIndex: Int, targetPosition: Int? = null) {
        if (!_isPlayerTurn.value) return

        val player = _gameManager.players[0]

        // Check if this is a tactical card for animation
        val card = player.hand.getOrNull(cardIndex)
        if (card is EnhancedTacticCard) {
            _currentTacticalCard.value = card
            _isTacticalCardAnimationVisible.value = true

            viewModelScope.launch {
                delay(1500) // Wait for animation
                _isTacticalCardAnimationVisible.value = false

                // Now play the card
                val isCardPlayed = player.playCard(cardIndex, _gameManager, targetPosition)
                handleCardPlayResult(isCardPlayed)
            }
        } else {
            // Regular card play
            val isCardPlayed = player.playCard(cardIndex, _gameManager, targetPosition)
            handleCardPlayResult(isCardPlayed)
        }
    }

    private fun handleCardPlayResult(isCardPlayed: Boolean) {
        if (isCardPlayed) {
            _statusMessage.value = "Card played successfully"
            updateAllGameStates()
        } else {
            _statusMessage.value = "Cannot play this card"
        }
    }

    fun attackEnemyUnit(targetPosition: Int) {
        if (!_isPlayerTurn.value || _selectedUnitPosition.value == -1) return

        val attacker = _gameManager.players[0].board.getUnitAt(_selectedUnitPosition.value)
        val target = _gameManager.players[1].board.getUnitAt(targetPosition)

        if (attacker != null && target != null) {
            val attackResult = attacker.attackUnit(target, _gameManager)
            if (attackResult) {
                _statusMessage.value = "Attack successful!"
                _selectedUnitPosition.value = -1
                updateAllGameStates()
            } else {
                if (_gameManager.players[1].board.hasTauntUnit() && !target.hasTaunt) {
                    _statusMessage.value = "You must attack a unit with Taunt first!"
                } else {
                    _statusMessage.value = "Cannot attack with this unit"
                }
            }
        }
    }

    /**
     * Attack the opponent directly
     */
    fun attackOpponentDirectly() {
        if (!_isPlayerTurn.value || _selectedUnitPosition.value == -1) return

        val attacker = _gameManager.players[0].board.getUnitAt(_selectedUnitPosition.value)

        if (attacker != null) {
            val attackResult = attacker.attackOpponent(_gameManager)
            if (attackResult) {
                _statusMessage.value = "Direct attack on opponent successful!"
                _selectedUnitPosition.value = -1
                updateAllGameStates()

                // Check for game over
                checkGameOver()
            } else {
                if (_gameManager.players[1].board.hasTauntUnit()) {
                    _statusMessage.value = "You must attack a unit with Taunt first!"
                } else {
                    _statusMessage.value = "Cannot attack with this unit"
                }
            }
        }
    }

    fun selectUnitForAttack(position: Int) {
        if (!_isPlayerTurn.value) return

        val unit = _gameManager.players[0].board.getUnitAt(position)
        if (unit != null && unit.canAttackThisTurn) {
            _selectedUnitPosition.value = position
            _statusMessage.value = "Select a target to attack"
        } else {
            _statusMessage.value = "This unit cannot attack"
        }
    }

    fun performAttack(targetPosition: Int) {
        if (!_isPlayerTurn.value || _selectedUnitPosition.value == -1) return

        val attacker = _gameManager.players[0].board.getUnitAt(_selectedUnitPosition.value)
        val target = _gameManager.players[1].board.getUnitAt(targetPosition)

        if (attacker != null && target != null) {
            // Record damage for animation
            _lastAttackDamage.value = attacker.attack

            val attackResult = attacker.attackUnit(target, _gameManager)
            if (attackResult) {
                _statusMessage.value = "Attack successful!"
                _selectedUnitPosition.value = -1
                updateAllGameStates()
            } else {
                _statusMessage.value = "Cannot attack with this unit"
            }
        }
    }

    fun endTurn() {
        _gameManager.turnManager.endTurn()
        updateAllGameStates()

        // AI turn
        if (!_isPlayerTurn.value) {
            simulateAITurn()
        }
    }

    private fun simulateAITurn() {
        viewModelScope.launch {
            val ai = _gameManager.players[1]

            // AI tries to play cards with animation
            delay(500) // Animation delay

            // Try to play tactical cards first for visual effect
            var cardPlayed = false
            for (i in ai.hand.indices) {
                val card = ai.hand[i]
                if (card is EnhancedTacticCard) {
                    _currentTacticalCard.value = card
                    _isTacticalCardAnimationVisible.value = true

                    delay(1500) // Wait for animation
                    _isTacticalCardAnimationVisible.value = false

                    // Now play the card
                    if (ai.playCard(i, _gameManager)) {
                        updateAllGameStates()
                        cardPlayed = true
                        break
                    }
                }
            }

            // If no tactical card was played, try unit cards
            if (!cardPlayed) {
                for (i in ai.hand.indices) {
                    if (ai.playCard(i, _gameManager)) {
                        updateAllGameStates()
                        delay(500) // Animation delay between actions
                        break
                    }
                }
            }

            // AI attacks with animation
            delay(500)
            val aiUnits = ai.board.getAllUnits()
            val playerUnits = _gameManager.players[0].board.getAllUnits()

            if (playerUnits.isNotEmpty()) {
                for (aiUnit in aiUnits) {
                    if (aiUnit.canAttackThisTurn) {
                        val targetUnit = playerUnits.first()

                        // Find positions for animation
                        val aiUnitPosition = findUnitPosition(1, aiUnit)
                        val playerUnitPosition = findUnitPosition(0, targetUnit)

                        if (aiUnitPosition != -1 && playerUnitPosition != -1) {
                            // Record for animation
                            _lastAttackDamage.value = aiUnit.attack

                            // Trigger attack animation here if needed

                            // Perform the attack
                            aiUnit.attackUnit(targetUnit, _gameManager)
                            updateAllGameStates()
                            delay(500) // Animation delay
                        }
                    }
                }
            }

            // End AI turn
            delay(500)
            _gameManager.turnManager.endTurn()
            updateAllGameStates()
        }
    }

    // Helper function to find a unit's position on the board
    private fun findUnitPosition(playerIndex: Int, unit: UnitCard): Int {
        val board = _gameManager.players[playerIndex].board
        for (i in 0 until board.maxSize) {
            if (board.getUnitAt(i) == unit) {
                return i
            }
        }
        return -1
    }

    private fun updateAllGameStates() {
        val currentPlayer = _gameManager.turnManager.currentPlayer ?: return

        _playerHandState.value = _gameManager.players[0].hand
        _playerBoardState.value = getPlayerBoardUnits(0)
        _opponentBoardState.value = getPlayerBoardUnits(1)

        _playerMana.value = _gameManager.players[0].currentMana
        _playerMaxMana.value = _gameManager.players[0].maxMana

        _playerHealth.value = _gameManager.players[0].health
        _opponentHealth.value = _gameManager.players[1].health

        _isPlayerTurn.value = currentPlayer == _gameManager.players[0]
        _gameState.value = _gameManager.gameState

        // Check for game over after state update
        checkGameOver()
    }

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


    // Get active formations for visualization
    fun getPlayerActiveFormations(): List<Formation> {
        return _gameManager.formationManager.getActiveFormations(_gameManager.players[0])
    }

    fun getOpponentActiveFormations(): List<Formation> {
        return _gameManager.formationManager.getActiveFormations(_gameManager.players[1])
    }

    // Methods for tactical card targeting UI
    fun playTacticalCardWithTarget(cardIndex: Int, targetPosition: Int) {
        if (!_isPlayerTurn.value) return

        val player = _gameManager.players[0]
        val card = player.hand.getOrNull(cardIndex) as? EnhancedTacticCard ?: return

        _currentTacticalCard.value = card
        _isTacticalCardAnimationVisible.value = true

        viewModelScope.launch {
            delay(1500) // Wait for animation
            _isTacticalCardAnimationVisible.value = false

            // Play the card with target
            val isCardPlayed = player.playCard(cardIndex, _gameManager, targetPosition)
            handleCardPlayResult(isCardPlayed)
        }
    }

    // Handle card draw animation
    fun animateCardDraw() {
        viewModelScope.launch {
            // Logic for card draw animation
            // This could trigger UI animations when new cards are added to hand
            updateAllGameStates()
        }
    }

    fun GameViewModel.getPlayerActiveFormations(): List<Formation> {
        return _gameManager.formationManager.getActiveFormations(_gameManager.players[0])
    }

    /**
     * Gets the list of active formations for the opponent.
     */
    fun GameViewModel.getOpponentActiveFormations(): List<Formation> {
        return _gameManager.formationManager.getActiveFormations(_gameManager.players[1])
    }

    /**
     * Initialize the game with formations.
     * Call this during the com.example.cardgame.ui.viewmodel.GameViewModel initialization.
     */
    fun initializeFormations() {
        _gameManager.formationManager.initializePredefinedFormations()
    }


}

