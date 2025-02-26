package com.example.cardgame.game

import com.example.cardgame.data.model.card.Card

class Player(
    val id: Int,
    val name: String,
    var health: Int = 30,
    val maxHealth: Int = 30,
    var maxMana: Int = 10,
    var currentMana: Int = 0
) {
    val deck = mutableListOf<Card>()
    val hand = mutableListOf<Card>()
    val board = Board()

    fun drawCard() {
        if (deck.isNotEmpty()) {
            val card = deck.removeAt(0)
            if (hand.size < 10) {
                hand.add(card)
            }
        } else {
            // Fatigue damage
            takeDamage(1)
        }
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

    fun isDead(): Boolean = health <= 0
}