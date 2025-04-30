package com.example.cardgame.data.repository

import android.content.Context
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.storage.CardLoader
import com.example.cardgame.data.storage.DeckStorageService

class DeckBuilderRepository(context: Context) {
    private val cardLoader = CardLoader(context)
    private val deckStorage = DeckStorageService(context)

    // Cache for available cards
    private val availableCards by lazy { cardLoader.loadAllCards() }

    /**
     * Get all available cards for deck building
     */
    fun getAllAvailableCards(): List<Card> {
        return availableCards
    }

    /**
     * Get a filtered list of cards based on search criteria
     */
    fun searchCards(query: String, filters: Map<String, Any> = emptyMap()): List<Card> {
        return availableCards.filter { card ->
            // Simple name search
            val matchesQuery = card.name.contains(query, ignoreCase = true) ||
                    card.description.contains(query, ignoreCase = true)

            // Additional filtering logic based on filters map could go here
            // For example:
            // val matchesType = filters["type"] == null || card.type == filters["type"]
            // val matchesCost = filters["maxCost"] == null || card.manaCost <= filters["maxCost"] as Int

            matchesQuery // && matchesType && matchesCost, etc.
        }
    }

    /**
     * Get all player's custom decks
     */
    fun getCustomDecks(): List<Deck> {
        return deckStorage.getCustomDeckNames().mapNotNull { deckId ->
            deckStorage.loadDeck(deckId)
        }
    }

    /**
     * Get a specific deck by ID
     */
    fun getDeck(deckId: String): Deck? {
        return deckStorage.loadDeck(deckId)
    }

    /**
     * Create a new empty deck
     * @return null if maximum deck limit reached
     */
    fun createDeck(name: String, description: String): Deck? {
        if (deckStorage.getCustomDeckCount() >= 5) {
            return null
        }

        val deckId = generateDeckId(name)
        val newDeck = Deck(
            id = deckId,
            name = name,
            description = description,
            isPlayerOwned = true,
            cards = mutableListOf()
        )

        return if (deckStorage.saveDeck(newDeck)) newDeck else null
    }

    /**
     * Save changes to an existing deck
     * @return false if deck couldn't be saved
     */
    fun saveDeck(deck: Deck): Boolean {
        return deckStorage.saveDeck(deck)
    }

    /**
     * Delete a deck
     */
    fun deleteDeck(deckId: String): Boolean {
        return deckStorage.deleteDeck(deckId)
    }

    /**
     * Generate a unique deck ID based on name
     */
    private fun generateDeckId(name: String): String {
        val baseId = name.lowercase().replace(Regex("[^a-z0-9]"), "_")
        val timestamp = System.currentTimeMillis()
        return "${baseId}_$timestamp"
    }

    /**
     * Check if a deck is valid (has correct number of cards, etc.)
     */
    fun validateDeck(deck: Deck): DeckValidationResult {
        // Get counts
        val cardCount = deck.cards.size

        // Validation rules
        if (cardCount < MIN_DECK_SIZE) {
            return DeckValidationResult(false, "Deck must have at least $MIN_DECK_SIZE cards")
        }

        if (cardCount > MAX_DECK_SIZE) {
            return DeckValidationResult(false, "Deck cannot have more than $MAX_DECK_SIZE cards")
        }

        // Additional validation rules could go here
        // For example:
        // - Check for card limits (e.g., max 2 copies of the same card)
        // - Check for required card types (e.g., minimum number of units)

        return DeckValidationResult(true, "Deck is valid")
    }

    companion object {
        const val MIN_DECK_SIZE = 20
        const val MAX_DECK_SIZE = 40
    }

    /**
     * Result class for deck validation
     */
    data class DeckValidationResult(
        val isValid: Boolean,
        val message: String
    )
}