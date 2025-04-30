package com.example.cardgame.data.repository

import android.content.Context
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.storage.CardLoader
import com.example.cardgame.data.storage.DeckStorageService


/**
 * Repository for deck building operations
 */
class DeckBuilderRepository(
    private val cardRepository: CardRepository
) {
    // Constants for deck validation
    companion object {
        const val MIN_DECK_SIZE = 20
        const val MAX_DECK_SIZE = 40
        const val MAX_CUSTOM_DECKS = 5
    }

    /**
     * Get all available cards for deck building
     */
    fun getAllAvailableCards(): List<Card> {
        return cardRepository.getAllCards()
    }

    /**
     * Get a filtered list of cards based on search criteria
     */
    fun searchCards(query: String, filters: Map<String, Any> = emptyMap()): List<Card> {
        return cardRepository.getAllCards().filter { card ->
            // Simple name search
            val matchesQuery = card.name.contains(query, ignoreCase = true) ||
                    card.description.contains(query, ignoreCase = true)

            // Additional filtering logic based on filters map could go here

            matchesQuery
        }
    }

    /**
     * Get all player's custom decks
     */
    suspend fun getCustomDecks(): List<Deck> {
        return cardRepository.getCustomDecks()
    }

    /**
     * Get a specific deck by ID
     */
    suspend fun getDeck(deckId: String): Deck? {
        return cardRepository.loadAnyDeck(deckId)
    }

    /**
     * Create a new empty deck
     * @return null if maximum deck limit reached
     */
    suspend fun createDeck(name: String, description: String): Deck? {
        // Check if we've reached the maximum number of custom decks
        if (cardRepository.getCustomDeckCount() >= MAX_CUSTOM_DECKS) {
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

        return if (cardRepository.saveCustomDeck(newDeck)) newDeck else null
    }

    /**
     * Save changes to an existing deck
     * @return false if deck couldn't be saved
     */
    suspend fun saveDeck(deck: Deck): Boolean {
        return cardRepository.saveCustomDeck(deck)
    }

    /**
     * Delete a deck
     */
    suspend fun deleteDeck(deckId: String): Boolean {
        return cardRepository.deleteCustomDeck(deckId)
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

        return DeckValidationResult(true, "Deck is valid")
    }

    /**
     * Result class for deck validation
     */
    data class DeckValidationResult(
        val isValid: Boolean,
        val message: String
    )
}