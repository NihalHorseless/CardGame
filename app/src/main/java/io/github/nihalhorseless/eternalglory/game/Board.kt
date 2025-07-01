package io.github.nihalhorseless.eternalglory.game

import io.github.nihalhorseless.eternalglory.data.model.card.FortificationCard
import io.github.nihalhorseless.eternalglory.data.model.card.UnitCard

/**
 * Represents a unified game board where both players place their units.
 */
class Board(val rows: Int = 5, val columns: Int = 5) {
    // 2D array to store units, null means empty cell
    private val grid = Array(rows) { arrayOfNulls<UnitCard>(columns) }

    // Player ID to track ownership of each unit
    private val unitOwners = mutableMapOf<UnitCard, Int>()

    /**
     * 2D array to store fortifications, null means no fortification
     */
    private val fortificationGrid = Array(rows) { arrayOfNulls<FortificationCard>(columns) }

    /**
     * Map to track ownership of fortifications
     */
    private val fortificationOwners = mutableMapOf<FortificationCard, Int>()

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
     * Places a fortification on the board at the specified position.
     */
    fun placeFortification(fortification: FortificationCard, row: Int, col: Int, ownerId: Int): Boolean {
        if (row < 0 || row >= rows || col < 0 || col >= columns) return false

        // Check if the cell is empty (no unit and no fortification)
        if (grid[row][col] != null || fortificationGrid[row][col] != null) return false

        fortificationGrid[row][col] = fortification
        fortificationOwners[fortification] = ownerId
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
     * Removes a fortification from the specified position.
     */
    fun removeFortification(row: Int, col: Int) {
        if (row in 0 until rows && col in 0 until columns) {
            val fortification = fortificationGrid[row][col]
            if (fortification != null) {
                fortificationOwners.remove(fortification)
                fortificationGrid[row][col] = null
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
     * Gets the fortification at the specified position.
     */
    fun getFortificationAt(row: Int, col: Int): FortificationCard? {
        return if (row in 0 until rows && col in 0 until columns) {
            fortificationGrid[row][col]
        } else null
    }

    fun getFortificationOwner(fortification: FortificationCard): Int? = fortificationOwners[fortification]

    /**
     * Gets the position of a fortification on the board.
     */
    fun getFortificationPosition(fortification: FortificationCard): Pair<Int, Int>? {
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                if (fortificationGrid[row][col] == fortification) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }

    /**
     * Gets all fortifications belonging to a specific player.
     */
    fun getPlayerFortifications(playerId: Int): List<FortificationCard> {
        return fortificationOwners.entries
            .filter { it.value == playerId }
            .map { it.key }
    }

    /**
     * Checks if a position is truly empty (no units and no fortifications).
     */
    fun isPositionCompletelyEmpty(row: Int, col: Int): Boolean {
        return isPositionEmpty(row, col) && getFortificationAt(row, col) == null
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
        return getUnitAt(row, col) == null && getFortificationAt(row, col) == null
    }

    /**
     * Clears all units from the board.
     */
    fun clearAllUnits() {
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                grid[row][col] = null
                fortificationGrid[row][col] = null
            }
        }
        unitOwners.clear()
        fortificationOwners.clear()
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

}