package com.example.cardgame.data.repository

import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.storage.CardLoader

class CardRepository(val cardLoader: CardLoader) {

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
     * Get a list of all available player deck names
     */
    fun getAvailablePlayerDeckNames(): List<String> {
        return cardLoader.getAvailableDeckNames()
    }

    /**
     * Get a list of all available AI deck names
     */
    fun getAvailableAIDeckNames(): List<String> {
        return cardLoader.getAvailableAIDeckNames()
    }

    /**
     * Load a player deck by name
     */
    fun loadPlayerDeck(deckName: String): Deck? {
        return cardLoader.loadDeck(deckName, isAIDeck = false)
    }

    /**
     * Load an AI deck by name
     */
    fun loadAIDeck(deckName: String): Deck? {
        return cardLoader.loadDeck(deckName, isAIDeck = true)
    }

    /**
     * Get information about a deck (works for both player and AI decks)
     */
    fun getDeckInfo(deckName: String): Deck? {
        // Try as player deck first, then as AI deck
        return loadPlayerDeck(deckName) ?: loadAIDeck(deckName)
    }
}