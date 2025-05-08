package com.example.cardgame.game

import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.model.card.UnitCard

class Player(
    val id: Int,
    val name: String,
    var health: Int = 30,
    val maxHealth: Int = 30,
    var maxMana: Int = 10,
    var currentMana: Int = 0
) {
    val deck = Deck("", "Player Deck", "")
    val hand = mutableListOf<Card>()

    // No longer has a private board - will use shared board from GameManager

    fun drawCard(): Boolean {
        val card = deck.drawCard()

        if (card != null) {
            if (hand.size < 10) {
                hand.add(card)
                return true
            }
        } else {
            // Fatigue damage
            takeDamage(1)
        }

        return false
    }

    fun setDeck(newDeck: Deck) {
        // Clear current deck
        deck.cards.clear()

        // Add all cards from the new deck
        deck.cards.addAll(newDeck.cards)
    }

    fun takeDamage(amount: Int) {
        health -= amount
        health = maxOf(0, health)
    }

    fun drawInitialHand(count: Int = 3) {
        repeat(count) {
            drawCard()
        }
    }

    fun isDead(): Boolean = health <= 0

    /**
     * Gets all units on the game board that belong to this player.
     */
    private fun getMyUnits(gameBoard: Board): List<UnitCard> {
        return gameBoard.getPlayerUnits(id)
    }

    /**
     * Gets the number of units this player has on the board.
     */
    fun getUnitCount(gameBoard: Board): Int {
        return getMyUnits(gameBoard).size
    }

    /**
     * Checks if this player has a unit at the specified position.
     */
    fun hasUnitAt(gameBoard: Board, row: Int, col: Int): Boolean {
        val unit = gameBoard.getUnitAt(row, col) ?: return false
        return gameBoard.getUnitOwner(unit) == id
    }
}