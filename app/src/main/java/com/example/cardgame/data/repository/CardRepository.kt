package com.example.cardgame.data.repository

import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.storage.CardLoader

class CardRepository(private val cardLoader: CardLoader) {

    /**
     * Get all cards
     */
    fun getAllCards(): List<Card> {
        return cardLoader.loadAllCards()
    }

    /**
     * Get a card by its ID
     */
    fun getCardById(id: Int): Card? {
        return cardLoader.getCardById(id)
    }

    /**
     * Get a list of all available deck names
     */
    fun getAvailableDeckNames(): List<String> {
        return cardLoader.getAvailableDeckNames()
    }

    /**
     * Load a deck by name
     */
    fun loadDeck(deckName: String): Deck? {
        return cardLoader.loadDeck(deckName)
    }
}