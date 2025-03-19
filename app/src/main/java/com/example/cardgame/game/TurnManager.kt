package com.example.cardgame.game

class TurnManager(private val gameManager: GameManager) {
    var currentPlayer: Player? = null
    var turnNumber: Int = 0

    // Keep track of the current player context
    private val currentPlayerContext: PlayerContext?
        get() = currentPlayer?.let { gameManager.getPlayerContext(it) }

    fun startGame() {
        turnNumber = 1
        currentPlayer = gameManager.players[0]
        startTurn()
    }

    fun startTurn() {
        currentPlayer?.let { player ->
            // Reset mana based on turn number (capped at max)
            player.currentMana = player.maxMana.coerceAtMost(turnNumber)

            // Draw a card
            player.drawCard()

            // Reset units for attack
            val context = gameManager.getPlayerContext(player)
            context.units.forEach { unit ->
                unit.canAttackThisTurn = true
            }

            // Reset movement for this player's units
            gameManager.movementManager.resetMovement(player.id)
        }
    }

    fun endTurn() {
        // Switch to next player
        currentPlayer = if (currentPlayer == gameManager.players[0])
            gameManager.players[1] else gameManager.players[0]

        if (currentPlayer == gameManager.players[1]) turnNumber++
        startTurn()
    }
}