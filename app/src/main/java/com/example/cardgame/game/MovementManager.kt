package com.example.cardgame.game

import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.card.UnitCard

/**
 * Class that handles unit movement on the board
 */
class MovementManager(private val gameBoard: Board) {

    // Track which units have moved this turn
    private val movedUnits = mutableSetOf<UnitCard>()

    /**
     * Get the movement range for a unit based on its type
     * Cavalry: 2 tiles
     * Other units: 1 tile
     */
    fun getMovementRange(unit: UnitCard): Int {
        return when (unit.unitType) {
            UnitType.CAVALRY -> 2
            else -> 1
        }
    }
    fun resetMovementForUnit(unit: UnitCard) {
        // Remove this unit from the moved units set so it can move again
        movedUnits.remove(unit)
    }

    fun getAttackRange(unit: UnitCard): Int {
        return when (unit.unitType) {
            UnitType.MISSILE -> 2
            UnitType.ARTILLERY -> 3
            else -> 1 // Infantry and Cavalry have melee range
        }
    }

    fun getMinAttackRange(unit: UnitCard): Int {
        return when (unit.unitType) {
            UnitType.ARTILLERY -> 2 // Artillery can't attack adjacent units
            else -> 1 // All other units can attack adjacent units
        }
    }

    /**
     * Reset movement for all units at the start of a player's turn
     */
    fun resetMovement(playerId: Int) {
        // Clear moved status for this player's units
        val playerUnits = gameBoard.getPlayerUnits(playerId)
        movedUnits.removeAll(playerUnits.toSet())
    }

    /**
     * Check if a unit can move
     */
    fun canUnitMove(unit: UnitCard): Boolean {
        return !movedUnits.contains(unit)
    }

    /**
     * Mark a unit as having moved this turn
     */
    fun markUnitAsMoved(unit: UnitCard) {
        movedUnits.add(unit)
    }

    /**
     * Get all valid movement destinations for a unit
     */
    fun getValidMoveDestinations(unit: UnitCard): List<Pair<Int, Int>> {
        // If unit has already moved, return empty list
        if (!canUnitMove(unit)) return emptyList()

        // Get unit's current position
        val position = gameBoard.getUnitPosition(unit) ?: return emptyList()
        val (row, col) = position

        // Get movement range
        val range = getMovementRange(unit)

        // Calculate all possible destinations within range
        val destinations = mutableListOf<Pair<Int, Int>>()

        // Use breadth-first search to find all reachable tiles within range
        val visited = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Triple<Int, Int, Int>>() // row, col, remaining moves

        // Start at current position with full movement range
        queue.add(Triple(row, col, range))
        visited.add(Pair(row, col))

        while (queue.isNotEmpty()) {
            val (currentRow, currentCol, movesLeft) = queue.removeFirst()

            // If we've used all moves, don't explore further from this position
            if (movesLeft == 0) continue

            // Check all four adjacent tiles
            val adjacentTiles = listOf(
                Pair(currentRow - 1, currentCol), // up
                Pair(currentRow + 1, currentCol), // down
                Pair(currentRow, currentCol - 1), // left
                Pair(currentRow, currentCol + 1)  // right
            )

            for ((nextRow, nextCol) in adjacentTiles) {
                // Skip if out of bounds
                if (nextRow < 0 || nextRow >= gameBoard.rows ||
                    nextCol < 0 || nextCol >= gameBoard.columns) {
                    continue
                }

                // Skip if already visited
                if (Pair(nextRow, nextCol) in visited) {
                    continue
                }

                // Skip if occupied by unit OR fortification
                if (!gameBoard.isPositionCompletelyEmpty(nextRow, nextCol)) {
                    continue
                }

                // Add to valid destinations
                destinations.add(Pair(nextRow, nextCol))

                // Mark as visited
                visited.add(Pair(nextRow, nextCol))

                // Continue BFS with one less move
                queue.add(Triple(nextRow, nextCol, movesLeft - 1))
            }
        }

        return destinations
    }

    /**
     * Move a unit from one position to another
     * Returns true if the move was successful
     */
    fun moveUnit(unit: UnitCard, targetRow: Int, targetCol: Int): Boolean {
        // Check if unit can move
        if (!canUnitMove(unit)) return false

        // Get valid destinations
        val validDestinations = getValidMoveDestinations(unit)

        // Check if target position is valid
        if (Pair(targetRow, targetCol) !in validDestinations) return false

        // Get current position
        val currentPos = gameBoard.getUnitPosition(unit) ?: return false
        val (currentRow, currentCol) = currentPos

        // Get unit owner
        val ownerId = gameBoard.getUnitOwner(unit) ?: return false

        // Remove unit from current position
        gameBoard.removeUnit(currentRow, currentCol)

        // Place unit at new position
        val success = gameBoard.placeUnit(unit, targetRow, targetCol, ownerId)

        if (success) {
            // Mark unit as moved
            markUnitAsMoved(unit)
            return true
        }

        // If failed to place at new position, put back at original position
        gameBoard.placeUnit(unit, currentRow, currentCol, ownerId)
        return false
    }
}