package io.github.nihalhorseless.eternalglory.data.model.effect

import io.github.nihalhorseless.eternalglory.data.enum.FortificationType
import io.github.nihalhorseless.eternalglory.data.model.card.FortificationCard
import io.github.nihalhorseless.eternalglory.game.GameManager
import io.github.nihalhorseless.eternalglory.game.Player

class WeakenUnitEffect : TacticEffect {
    override val name = "Weaken"
    override val description = "Reduces a unit's attack and health to 1"

    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (targetPosition == null) return false

        // Convert linear position to 2D coordinates
        val row = targetPosition / gameManager.gameBoard.columns
        val col = targetPosition % gameManager.gameBoard.columns

        // Check for a unit at the target position
        val targetUnit = gameManager.gameBoard.getUnitAt(row, col)
        if (targetUnit != null) {
            val targetOwnerId = gameManager.gameBoard.getUnitOwner(targetUnit)
            // Only debuff enemy units
            if (targetOwnerId != player.id) {
                // Set attack and health to 1
                targetUnit.attack = 1
                targetUnit.health = 1

                // Ensure we don't reduce health below 1
                if (targetUnit.health < 1) {
                    targetUnit.health = 1
                }

                return true
            }
        }

        return false
    }
}
/**
 * A debuff effect that transforms a unit into a Stone Wall fortification
 */
class PetrifyUnitEffect : TacticEffect {
    override val name = "Petrify"
    override val description = "Transforms a unit into a Stone Wall"

    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (targetPosition == null) return false

        // Convert linear position to 2D coordinates
        val row = targetPosition / gameManager.gameBoard.columns
        val col = targetPosition % gameManager.gameBoard.columns

        // Check for a unit at the target position
        val targetUnit = gameManager.gameBoard.getUnitAt(row, col)
        if (targetUnit != null) {
            val targetOwnerId = gameManager.gameBoard.getUnitOwner(targetUnit)
            // Only transform enemy units
            if (targetOwnerId != player.id) {
                // Get the unit's current health to use for the wall (with some minimum)
                val wallHealth = maxOf(targetUnit.health, 3)

                // Create a Stone Wall fortification
                val stoneWall = FortificationCard(
                    id = 9999, // Use a specific ID or generate one dynamically
                    name = "Petrified Unit",
                    description = "A unit turned to stone",
                    manaCost = 0, // Not meant to be played from hand
                    imagePath = "fortification_stone_wall",
                    attack = 0, // Walls don't attack
                    health = wallHealth,
                    maxHealth = wallHealth,
                    fortType = FortificationType.WALL,
                    canAttackThisTurn = false
                )

                // Remove the unit from the board
                gameManager.gameBoard.removeUnit(row, col)

                // Place the wall at the same position, but owned by the casting player
                val placed = gameManager.gameBoard.placeFortification(stoneWall, row, col, targetOwnerId?:1)

                return placed
            }
        }

        return false
    }
}