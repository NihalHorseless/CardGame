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

    fun playCard(cardIndex: Int, gameManager: GameManager, targetPosition: Pair<Int, Int>? = null): Boolean {
        if (cardIndex < 0 || cardIndex >= hand.size) return false

        val card = hand[cardIndex]

        // For unit cards, we need to check if the board position is valid
        if (card is UnitCard) {
            // Get target position or find an empty one in player's deployment zone
            val position = targetPosition ?:
            gameManager.gameBoard.getFirstEmptyPositionInDeploymentZone(id) ?:
            return false

            // Check if player can afford the card
            if (currentMana < card.manaCost) return false

            // Pay the mana cost
            currentMana -= card.manaCost

            // Remove card from hand
            hand.remove(card)

            // Clone the card for the board
            val boardCard = card.clone()

            // Place the cloned card on the unified board
            val (row, col) = position
            val placed = gameManager.gameBoard.placeUnit(boardCard, row, col, id)

            if (placed) {
                // Initialize attack availability based on charge ability
                boardCard.canAttackThisTurn = boardCard.hasCharge

                // Apply other effects if needed
                // gameManager.formationManager.applyFormationEffects(this)

                return true
            }

            // If placement failed, refund the cost and return the card to hand
            currentMana += card.manaCost
            hand.add(card)
            return false
        } else {
            // For non-unit cards, delegate to the card's play method
            // Note: This would need to be updated to handle the new board structure
            return card.play(this, gameManager, null)
        }
    }

    fun takeDamage(amount: Int) {
        health -= amount
        health = maxOf(0, health)
    }

    fun heal(amount: Int) {
        health += amount
        health = minOf(maxHealth, health)
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
    fun getMyUnits(gameBoard: Board): List<UnitCard> {
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