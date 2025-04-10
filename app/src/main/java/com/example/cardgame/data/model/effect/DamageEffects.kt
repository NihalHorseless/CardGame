package com.example.cardgame.data.model.effect

import com.example.cardgame.game.GameManager
import com.example.cardgame.game.Player

/**
 * Base class for damage-dealing effects
 */
abstract class DamageEffect(
    override val name: String,
    override val description: String,
    val damageAmount: Int
) : TacticEffect

/**
 * Direct damage to a single target
 */
class DirectDamageEffect(
    damageAmount: Int
) : DamageEffect(
    name = "Direct Damage",
    description = "Deal $damageAmount damage to a target",
    damageAmount = damageAmount
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
            // Only damage enemy units
            if (targetOwnerId != player.id) {
                targetUnit.takeDamage(damageAmount)
                gameManager.checkForDestroyedUnits()
                return true
            }
        }

        // If no unit or not an enemy, check for fortification
        val targetFort = gameManager.gameBoard.getFortificationAt(row, col)
        if (targetFort != null) {
            val targetOwnerId = gameManager.gameBoard.getFortificationOwner(targetFort)
            // Only damage enemy fortifications
            if (targetOwnerId != player.id) {
                targetFort.takeDamage(damageAmount)
                gameManager.checkForDestroyedFortifications()
                return true
            }
        }

        return false
    }
}

/**
 * Area of effect damage in a radius around target
 */
class AreaDamageEffect(
    damageAmount: Int,
    val radius: Int = 1 // 1 = 3x3 area, 2 = 5x5 area, etc.
) : DamageEffect(
    name = "Area Damage",
    description = "Deal $damageAmount damage to all enemies in a ${radius * 2 + 1}x${radius * 2 + 1} area",
    damageAmount = damageAmount
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
                        // Only damage enemy units
                        if (unitOwnerId != player.id) {
                            unit.takeDamage(damageAmount)
                            effectApplied = true
                        }
                    }

                    // Check for fortifications
                    val fort = gameManager.gameBoard.getFortificationAt(row, col)
                    if (fort != null) {
                        val fortOwnerId = gameManager.gameBoard.getFortificationOwner(fort)
                        // Only damage enemy fortifications
                        if (fortOwnerId != player.id) {
                            fort.takeDamage(damageAmount)
                            effectApplied = true
                        }
                    }
                }
            }
        }

        if (effectApplied) {
            gameManager.checkForDestroyedUnits()
            gameManager.checkForDestroyedFortifications()
        }

        return effectApplied
    }
}