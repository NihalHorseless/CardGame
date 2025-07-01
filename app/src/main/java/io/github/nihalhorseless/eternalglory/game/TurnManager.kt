package io.github.nihalhorseless.eternalglory.game

import io.github.nihalhorseless.eternalglory.data.enum.FortificationType

class TurnManager(private val gameManager: GameManager) {
    var currentPlayer: Player? = null
    private var turnNumber: Int = 0

    fun startGame() {
        turnNumber = 1
        currentPlayer = gameManager.players[0]
        startTurn()
    }

    private fun startTurn() {
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

            // Reset fortifications for attack (towers only)
            context.fortifications.forEach { fortification ->
                if (fortification.fortType == FortificationType.TOWER) {
                    fortification.canAttackThisTurn = true
                }
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