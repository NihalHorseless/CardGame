package com.example.cardgame.game

import com.example.cardgame.data.model.card.UnitCard

class Board(val maxSize: Int = 7) {
    private val slots = arrayOfNulls<UnitCard>(maxSize)

    fun placeUnit(unit: UnitCard, position: Int): Boolean {
        if (position < 0 || position >= maxSize) return false
        if (slots[position] != null) return false

        slots[position] = unit
        return true
    }

    fun removeUnit(position: Int) {
        if (position in 0 until maxSize) {
            slots[position] = null
        }
    }

    fun getUnitAt(position: Int): UnitCard? {
        return if (position in 0 until maxSize) slots[position] else null
    }

    fun getAllUnits(): List<UnitCard> {
        return slots.filterNotNull()
    }

    fun isFull(): Boolean = slots.none { it == null }

    fun isPositionOccupied(position: Int): Boolean {
        return position in 0 until maxSize && slots[position] != null
    }
    fun hasTauntUnit(): Boolean {
        return getAllUnits().any { it.hasTaunt }
    }

    fun getFirstEmptyPosition(): Int {
        return slots.indexOfFirst { it == null }.takeIf { it >= 0 } ?: -1
    }

    fun clearAllUnits() {
        for (i in 0 until maxSize) {
            removeUnit(i)
        }
    }
}