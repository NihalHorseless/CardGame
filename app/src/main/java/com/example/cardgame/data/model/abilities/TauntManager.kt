package com.example.cardgame.data.model.abilities

import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.game.Board

/**
 * Helper class to manage taunt effects and targeting rules
 */
class TauntManager(private val gameBoard: Board) {

    /**
     * Checks if a unit is protected by adjacent taunt units
     */
    fun isProtectedByTaunt(unit: UnitCard): Boolean {
        // Important: Taunt units themselves are not protected by other taunt units
        if (unit.hasTaunt) return false

        val position = gameBoard.getUnitPosition(unit) ?: return false
        val (row, col) = position
        val ownerId = gameBoard.getUnitOwner(unit) ?: return false

        // Check all adjacent cells for friendly taunt units
        val adjacentPositions = listOf(
            Pair(row - 1, col), // above
            Pair(row + 1, col), // below
            Pair(row, col - 1), // left
            Pair(row, col + 1)  // right
        )

        return adjacentPositions.any { (adjRow, adjCol) ->
            val adjacentUnit = gameBoard.getUnitAt(adjRow, adjCol)

            // Check if adjacent unit exists, is owned by the same player, and has taunt
            adjacentUnit != null &&
                    gameBoard.getUnitOwner(adjacentUnit) == ownerId &&
                    adjacentUnit.hasTaunt
        }
    }

    /**
     * Gets all valid attack targets for a specific player, considering taunt protection
     */
    fun getValidAttackTargets(attackerUnit: UnitCard, opponentId: Int): List<UnitCard> {
        // Get all opponent units
        val opponentUnits = gameBoard.getPlayerUnits(opponentId)

        // Filter out units protected by taunt, but always include taunt units themselves
        return opponentUnits.filter { unit ->
            unit.hasTaunt || !isProtectedByTaunt(unit)
        }
    }

    /**
     * Checks if a unit can be targeted for an attack
     */
    fun canUnitBeTargeted(targetUnit: UnitCard): Boolean {
        // Taunt units can always be targeted
        return targetUnit.hasTaunt || !isProtectedByTaunt(targetUnit)
    }

    /**
     * Checks if a player has any taunt units on the board
     */
    fun hasAnyTauntUnits(playerId: Int): Boolean {
        return gameBoard.getPlayerUnits(playerId).any { it.hasTaunt }
    }

    /**
     * Gets all taunt units for a player
     */
    fun getTauntUnits(playerId: Int): List<UnitCard> {
        return gameBoard.getPlayerUnits(playerId).filter { it.hasTaunt }
    }
}