package com.example.cardgame.game

class TurnManager(private val gameManager: GameManager) {
    var currentPlayer: Player? = null
    var turnNumber: Int = 0

    fun startGame() {
        turnNumber = 1
        currentPlayer = gameManager.players[0]
        startTurn()
    }

    fun startTurn() {
        currentPlayer?.let { player ->
            // Reset mana based on turn number (capped at max)
            player.currentMana = Math.min(player.maxMana, turnNumber)

            // Draw a card
            player.drawCard()

            // Reset units for attack
            player.board.getAllUnits().forEach { unit ->
                unit.canAttackThisTurn = true
            }
        }
    }

    fun endTurn() {
        // Switch to next player
        currentPlayer = if (currentPlayer == gameManager.players[0])
            gameManager.players[1] else gameManager.players[0]

        turnNumber++
        startTurn()
    }
}