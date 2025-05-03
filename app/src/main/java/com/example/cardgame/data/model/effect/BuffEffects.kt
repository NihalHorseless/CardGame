package com.example.cardgame.data.model.effect

import com.example.cardgame.data.enum.FortificationType
import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player

/**
 * Base class for effects that buff units or fortifications
 */
abstract class BuffEffect(
    override val name: String,
    override val description: String,
    val buffAmount: Int,
    val duration: Int = 1 // Number of turns the buff lasts
) : TacticEffect

/**
 * Increases a unit's attack power
 */
class AttackBuffEffect(
    buffAmount: Int,
    duration: Int = 1
) : BuffEffect(
    name = "Attack Boost",
    description = "Increase a unit's attack by $buffAmount for $duration turn${if(duration > 1) "s" else ""}",
    buffAmount = buffAmount,
    duration = duration
) {
    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (targetPosition == null) return false

        // Convert linear position to 2D coordinates
        val row = targetPosition / gameManager.gameBoard.columns
        val col = targetPosition % gameManager.gameBoard.columns

        // Check for a unit at the target position
        val targetUnit = gameManager.gameBoard.getUnitAt(row, col)
        if (targetUnit != null) {
            val targetOwnerId = gameManager.gameBoard.getUnitOwner(targetUnit)
            // Only buff friendly units
            if (targetOwnerId == player.id) {
                // Apply the buff
                targetUnit.attack += buffAmount

                // In a real implementation, you would need to track this
                // temporary effect to remove it after the duration
                // For example:
                // gameManager.addTemporaryEffect(
                //     target = targetUnit,
                //     duration = duration,
                //     onExpire = { targetUnit.attack -= buffAmount }
                // )

                return true
            }
        }

        // Check for a fortification
        val targetFort = gameManager.gameBoard.getFortificationAt(row, col)
        if (targetFort != null && targetFort.fortType == FortificationType.TOWER) {
            val targetOwnerId = gameManager.gameBoard.getFortificationOwner(targetFort)
            // Only buff friendly towers
            if (targetOwnerId == player.id) {
                // Apply the buff
                targetFort.attack += buffAmount

                // In a real implementation, you would track this
                // temporary effect to remove it after the duration

                return true
            }
        }

        return false
    }
}
/**
 * Increases a unit's attack power
 */
class HealthBuffEffect(
    buffAmount: Int,
    duration: Int = 1
) : BuffEffect(
    name = "Health Boost",
    description = "Increase a unit's health by $buffAmount for $duration turn${if(duration > 1) "s" else ""}",
    buffAmount = buffAmount,
    duration = duration
) {
    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (targetPosition == null) return false

        // Convert linear position to 2D coordinates
        val row = targetPosition / gameManager.gameBoard.columns
        val col = targetPosition % gameManager.gameBoard.columns

        // Check for a unit at the target position
        val targetUnit = gameManager.gameBoard.getUnitAt(row, col)
        if (targetUnit != null) {
            val targetOwnerId = gameManager.gameBoard.getUnitOwner(targetUnit)
            // Only buff friendly units
            if (targetOwnerId == player.id) {
                // Apply the buff
                targetUnit.health += buffAmount
                targetUnit.maxHealth += buffAmount

                // In a real implementation, you would need to track this
                // temporary effect to remove it after the duration
                // For example:
                // gameManager.addTemporaryEffect(
                //     target = targetUnit,
                //     duration = duration,
                //     onExpire = { targetUnit.attack -= buffAmount }
                // )

                return true
            }
        }

        // Check for a fortification
        val targetFort = gameManager.gameBoard.getFortificationAt(row, col)
        if (targetFort != null && targetFort.fortType == FortificationType.TOWER) {
            val targetOwnerId = gameManager.gameBoard.getFortificationOwner(targetFort)
            // Only buff friendly towers
            if (targetOwnerId == player.id) {
                // Apply the buff
                targetFort.health += buffAmount
                targetFort.maxHealth += buffAmount

                // In a real implementation, you would track this
                // temporary effect to remove it after the duration

                return true
            }
        }

        return false
    }
}
/**
* An effect that converts an enemy unit to your control
*/
class BribeUnitEffect : TacticEffect {
    override val name = "Bribery"
    override val description = "Bribe an enemy unit"

    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (targetPosition == null) return false

        // Convert linear position to 2D coordinates
        val row = targetPosition / gameManager.gameBoard.columns
        val col = targetPosition % gameManager.gameBoard.columns

        // Check for a unit at the target position
        val targetUnit = gameManager.gameBoard.getUnitAt(row, col)
        if (targetUnit != null) {
            val targetOwnerId = gameManager.gameBoard.getUnitOwner(targetUnit)
            // Only convert enemy units
            if (targetOwnerId != player.id) {
                // Get the unit's position
                val position = gameManager.gameBoard.getUnitPosition(targetUnit)
                if (position != null) {
                    // Remove the unit from its current position
                    gameManager.gameBoard.removeUnit(position.first, position.second)

                    // Place it back at the same position but with new ownership
                    val placed = gameManager.gameBoard.placeUnit(targetUnit, position.first, position.second, player.id)

                    // Reset unit's ability to attack this turn so it can't be used immediately
                    targetUnit.canAttackThisTurn = false

                    // Note: we don't reset its movement because the unit was likely already moved
                    // by the opponent, and this would be unfair otherwise

                    return placed
                }
            }
        }

        return false
    }
}