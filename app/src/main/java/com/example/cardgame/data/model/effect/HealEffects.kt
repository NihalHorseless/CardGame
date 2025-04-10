package com.example.cardgame.data.model.effect

import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player

/**
 * Base class for healing effects
 */
abstract class HealingEffect(
    override val name: String,
    override val description: String,
    val healAmount: Int
) : TacticEffect

/**
 * Direct healing to a single target
 */
class SingleTargetHealingEffect(
    healAmount: Int
) : HealingEffect(
    name = "Healing",
    description = "Restore $healAmount health to a friendly target",
    healAmount = healAmount
) {
    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (targetPosition == null) return false

        // Convert linear position to 2D coordinates
        val row = targetPosition / gameManager.gameBoard.columns
        val col = targetPosition % gameManager.gameBoard.columns

        // First check for a unit at the target position
        val targetUnit = gameManager.gameBoard.getUnitAt(row, col)
        if (targetUnit != null) {
            val targetOwnerId = gameManager.gameBoard.getUnitOwner(targetUnit)
            // Only heal friendly units
            if (targetOwnerId == player.id) {
                targetUnit.heal(healAmount)
                return true
            }
        }

        // If no unit or not friendly, check for fortification
        val targetFort = gameManager.gameBoard.getFortificationAt(row, col)
        if (targetFort != null) {
            val targetOwnerId = gameManager.gameBoard.getFortificationOwner(targetFort)
            // Only heal friendly fortifications
            if (targetOwnerId == player.id) {
                targetFort.heal(healAmount)
                return true
            }
        }

        return false
    }
}

/**
 * Area healing for all friendly units in a radius
 */
class AreaHealingEffect(
    healAmount: Int,
    val radius: Int = 1 // 1 = 3x3 area, 2 = 5x5 area, etc.
) : HealingEffect(
    name = "Area Healing",
    description = "Restore $healAmount health to all friendly units in a ${radius * 2 + 1}x${radius * 2 + 1} area",
    healAmount = healAmount
) {
    override fun apply(player: Player, gameManager: GameManager, targetPosition: Int?): Boolean {
        if (targetPosition == null) return false

        // Convert linear position to 2D coordinates
        val centerRow = targetPosition / gameManager.gameBoard.columns
        val centerCol = targetPosition % gameManager.gameBoard.columns

        var effectApplied = false

        // Apply to all positions within radius
        for (rowOffset in -radius..radius) {
            for (colOffset in -radius..radius) {
                val row = centerRow + rowOffset
                val col = centerCol + colOffset

                // Check if position is within board bounds
                if (row in 0 until gameManager.gameBoard.rows &&
                    col in 0 until gameManager.gameBoard.columns) {

                    // Check for units
                    val unit = gameManager.gameBoard.getUnitAt(row, col)
                    if (unit != null) {
                        val unitOwnerId = gameManager.gameBoard.getUnitOwner(unit)
                        // Only heal friendly units
                        if (unitOwnerId == player.id) {
                            unit.heal(healAmount)
                            effectApplied = true
                        }
                    }

                    // Check for fortifications
                    val fort = gameManager.gameBoard.getFortificationAt(row, col)
                    if (fort != null) {
                        val fortOwnerId = gameManager.gameBoard.getFortificationOwner(fort)
                        // Only heal friendly fortifications
                        if (fortOwnerId == player.id) {
                            fort.heal(healAmount)
                            effectApplied = true
                        }
                    }
                }
            }
        }

        return effectApplied
    }
}