package com.example.cardgame.data.repository

import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.storage.CardLoader

class CardRepository(
    private val cardLoader: CardLoader,
    private val customDeckRepository: CustomDeckRepository
) {
    private val TAG = "CardRepository"

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
     * Get a list of all available predefined player deck names
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
     * Load a predefined player deck by name
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

    /**
     * Get all available decks, including both predefined and custom decks
     */
    suspend fun getAllAvailableDecks(): List<String> {
        val predefinedDecks = getAvailablePlayerDeckNames()
        val customDecks = customDeckRepository.getCustomDeckIds()

        // Combine both lists
        return predefinedDecks + customDecks
    }

    suspend fun getAllAvailableDeckNames(): List<String> {
        // Get predefined deck names
        val predefinedDecks = getAvailablePlayerDeckNames().map {
            it.replace("_", " ").split(" ").joinToString(" ") { word -> word.capitalize() }
        }

        // Get custom decks
        val customDecks = customDeckRepository.getAllCustomDecks().map { it.name }

        // Combine both lists and return
        return predefinedDecks + customDecks
    }

    /**
     * Load any deck by name, checking both predefined and custom decks
     */
    suspend fun loadAnyDeck(deckName: String): Deck? {
        // First try loading as a custom deck (from database)
        val customDeck = customDeckRepository.loadDeck(deckName)
        if (customDeck != null) {
            return customDeck
        }

        // If not found in custom decks, try predefined decks
        return loadPlayerDeck(deckName) ?: loadAIDeck(deckName)
    }

    /**
     * Save a custom deck
     */
    suspend fun saveCustomDeck(deck: Deck): Boolean {
        return customDeckRepository.saveDeck(deck)
    }

    /**
     * Delete a custom deck
     */
    suspend fun deleteCustomDeck(deckId: String): Boolean {
        return customDeckRepository.deleteDeck(deckId)
    }

    /**
     * Get all custom decks
     */
    suspend fun getCustomDecks(): List<Deck> {
        return customDeckRepository.getAllCustomDecks()
    }

    /**
     * Get the count of custom decks
     */
    suspend fun getCustomDeckCount(): Int {
        return customDeckRepository.getCustomDeckCount()
    }
}
