package com.example.cardgame.game

import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck

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
    val board = Board()

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

    fun playCard(cardIndex: Int, gameManager: GameManager, targetPosition: Int? = null): Boolean {
        if (cardIndex < 0 || cardIndex >= hand.size) return false

        val card = hand[cardIndex]
        return card.play(this, gameManager, targetPosition)
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
}