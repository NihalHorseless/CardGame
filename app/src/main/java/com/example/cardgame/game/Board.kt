package com.example.cardgame.game

import com.example.cardgame.data.model.card.UnitCard

/**
 * Represents a unified game board where both players place their units.
 */
class Board(val rows: Int = 5, val columns: Int = 5) {
    // 2D array to store units, null means empty cell
    private val grid = Array(rows) { arrayOfNulls<UnitCard>(columns) }

    // Player ID to track ownership of each unit
    private val unitOwners = mutableMapOf<UnitCard, Int>()

    /**
     * Places a unit on the board at the specified position.
     *
     * @param unit The unit card to place
     * @param row The row position (0-based)
     * @param col The column position (0-based)
     * @param ownerId The ID of the player who owns this unit
     * @return true if placement was successful, false otherwise
     */
    fun placeUnit(unit: UnitCard, row: Int, col: Int, ownerId: Int): Boolean {
        if (row < 0 || row >= rows || col < 0 || col >= columns) return false
        if (grid[row][col] != null) return false

        grid[row][col] = unit
        unitOwners[unit] = ownerId
        return true
    }

    /**
     * Removes a unit from the specified position.
     */
    fun removeUnit(row: Int, col: Int) {
        if (row in 0 until rows && col in 0 until columns) {
            val unit = grid[row][col]
            if (unit != null) {
                unitOwners.remove(unit)
                grid[row][col] = null
            }
        }
    }

    /**
     * Gets the unit at the specified position.
     */
    fun getUnitAt(row: Int, col: Int): UnitCard? {
        return if (row in 0 until rows && col in 0 until columns) {
            grid[row][col]
        } else null
    }

    /**
     * Gets the owner ID of the specified unit.
     */
    fun getUnitOwner(unit: UnitCard): Int? = unitOwners[unit]

    /**
     * Gets the position of a unit on the board.
     *
     * @return Pair of (row, col) or null if the unit is not on the board
     */
    fun getUnitPosition(unit: UnitCard): Pair<Int, Int>? {
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                if (grid[row][col] == unit) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }

    /**
     * Gets all units belonging to a specific player.
     */
    fun getPlayerUnits(playerId: Int): List<UnitCard> {
        return unitOwners.entries
            .filter { it.value == playerId }
            .map { it.key }
    }

    /**
     * Checks if a position on the board is empty.
     */
    fun isPositionEmpty(row: Int, col: Int): Boolean {
        return getUnitAt(row, col) == null
    }

    /**
     * Gets all units on the board.
     */
    fun getAllUnits(): List<UnitCard> {
        return unitOwners.keys.toList()
    }

    /**
     * Checks if a unit with taunt ability exists for the specified player.
     */
    fun hasTauntUnit(playerId: Int): Boolean {
        return getPlayerUnits(playerId).any { it.hasTaunt }
    }

    /**
     * Clears all units from the board.
     */
    fun clearAllUnits() {
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                grid[row][col] = null
            }
        }
        unitOwners.clear()
    }

    /**
     * Gets an empty position in the player's deployment zone.
     * For player 0, this is the bottom half (rows 3-4).
     * For player 1, this is the top half (rows 0-1).
     * The middle row (row 2) is neutral territory.
     */
    fun getFirstEmptyPositionInDeploymentZone(playerId: Int): Pair<Int, Int>? {
        // Calculate deployment zone boundaries with the middle row as neutral
        val startRow = if (playerId == 0) rows / 2 + 1 else 0
        val endRow = if (playerId == 0) rows else rows / 2

        for (row in startRow until endRow) {
            for (col in 0 until columns) {
                if (isPositionEmpty(row, col)) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }

    /**
     * Checks if a player's deployment zone is full.
     */
    fun isDeploymentZoneFull(playerId: Int): Boolean {
        return getFirstEmptyPositionInDeploymentZone(playerId) == null
    }

    /**
     * Gets all units adjacent to the specified position.
     */
    fun getAdjacentUnits(row: Int, col: Int): List<UnitCard> {
        val adjacentPositions = listOf(
            Pair(row - 1, col), // above
            Pair(row + 1, col), // below
            Pair(row, col - 1), // left
            Pair(row, col + 1)  // right
        )

        return adjacentPositions
            .mapNotNull { (r, c) -> getUnitAt(r, c) }
    }

    /**
     * Checks if a position is in a player's deployment zone.
     */
    fun isInDeploymentZone(row: Int, playerId: Int): Boolean {
        // With 5x5 board, rows 0-1 belong to player 1, rows 3-4 belong to player 0, row 2 is neutral
        return when (playerId) {
            0 -> row >= rows / 2 + 1 // Player 0: rows 3-4
            1 -> row < rows / 2      // Player 1: rows 0-1
            else -> false
        }
    }

    /**
     * Gets the number of units a player has on the board.
     */
    fun getPlayerUnitCount(playerId: Int): Int {
        return getPlayerUnits(playerId).size
    }
}