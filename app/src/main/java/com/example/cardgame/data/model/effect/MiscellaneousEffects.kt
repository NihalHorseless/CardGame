package com.example.cardgame.data.model.effect

import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player

/**
 * Draw cards from your deck
 */
class DrawCardsEffect(
    private val cardCount: Int
) : TacticEffect {
    override val name = "Card Draw"
    override val description = "Draw $cardCount card${if(cardCount > 1) "s" else ""} from your deck"

    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        // This effect doesn't need a target position
        var cardsDrawn = 0

        // Draw the specified number of cards
        repeat(cardCount) {
            // Don't draw beyond maximum hand size
            if (player.hand.size < 10) {
                val success = player.drawCard()
                if (success) cardsDrawn++
            }
        }

        // Effect is successful if at least one card was drawn
        return cardsDrawn > 0
    }
}

/**
 * Grant a unit the ability to attack immediately
 */
class GrantChargeEffect : TacticEffect {
    override val name = "Charge"
    override val description = "Allow a unit to attack immediately"

    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (targetPosition == null) return false

        // Convert linear position to 2D coordinates
        val row = targetPosition / gameManager.gameBoard.columns
        val col = targetPosition % gameManager.gameBoard.columns

        // Check for a unit at the target position
        val targetUnit = gameManager.gameBoard.getUnitAt(row, col)
        if (targetUnit != null) {
            val targetOwnerId = gameManager.gameBoard.getUnitOwner(targetUnit)
            // Only grant charge to friendly units
            if (targetOwnerId == player.id) {
                targetUnit.hasCharge = true
                targetUnit.canAttackThisTurn = true
                return true
            }
        }

        return false
    }
}

/**
 * Grant a unit the taunt ability
 */
class GrantTauntEffect : TacticEffect {
    override val name = "Taunt"
    override val description = "Force enemies to attack this unit if within range"

    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (targetPosition == null) return false

        // Convert linear position to 2D coordinates
        val row = targetPosition / gameManager.gameBoard.columns
        val col = targetPosition % gameManager.gameBoard.columns

        // Check for a unit at the target position
        val targetUnit = gameManager.gameBoard.getUnitAt(row, col)
        if (targetUnit != null) {
            val targetOwnerId = gameManager.gameBoard.getUnitOwner(targetUnit)
            // Only grant taunt to friendly units
            if (targetOwnerId == player.id) {
                targetUnit.hasTaunt = true
                return true
            }
        }

        return false
    }
}

/**
 * Allow a unit to move again this turn
 */
class RefreshMovementEffect : TacticEffect {
    override val name = "Swift Movement"
    override val description = "Allow a unit to move again this turn"

    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (targetPosition == null) return false

        // Convert linear position to 2D coordinates
        val row = targetPosition / gameManager.gameBoard.columns
        val col = targetPosition % gameManager.gameBoard.columns

        // Check for a unit at the target position
        val targetUnit = gameManager.gameBoard.getUnitAt(row, col)
        if (targetUnit != null) {
            val targetOwnerId = gameManager.gameBoard.getUnitOwner(targetUnit)
            // Only refresh friendly units
            if (targetOwnerId == player.id) {
                // Reset the movement for this unit
             //   gameManager.movementManager.resetMovementForUnit(targetUnit)
                return true
            }
        }

        return false
    }
}