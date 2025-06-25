package com.example.cardgame.data.repository

import android.util.Log
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.storage.CardLoader
import com.example.cardgame.util.CardTestData.sampleDeck
import com.example.cardgame.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

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
    private fun getAvailablePlayerDeckNames(): List<String> {
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
    private fun loadPlayerDeck(deckName: String): Deck? {
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
    suspend fun getDeckInfo(deckId: String): HashMap<String,Int>? {
        // Try as player deck first, then as AI deck
        val deck = loadAnyDeck(deckName = deckId)?: sampleDeck
        var infoHash = HashMap<String,Int>()
        var tacticsCard = 0
        var fortCards = 0
        var unitCards = 0

        for (i in deck.cards) {
            when (i) {
                is FortificationCard -> fortCards ++
                is TacticCard -> tacticsCard ++
                is UnitCard -> unitCards ++
            }
        }
        infoHash["tactics"] = tacticsCard
        infoHash["units"] = unitCards
        infoHash["forts"] = fortCards
        return infoHash
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
    suspend fun loadAnyDeckSafe(deckName: String): Result<Deck?> = withContext(Dispatchers.IO) {
        try {
            // First try loading as a custom deck
            val customDeck = customDeckRepository.loadDeck(deckName)
            if (customDeck != null) {
                return@withContext Result.Success(customDeck)
            }

            // Try predefined decks
            val deck = loadPlayerDeck(deckName) ?: loadAIDeck(deckName)
            Result.Success(deck)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading deck: $deckName", e)
            Result.Error(e, "Failed to load deck: ${e.message}")
        }
    }

    suspend fun getAllAvailableDecksSafe(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val predefinedDecks = getAvailablePlayerDeckNames()
            val customDecks = customDeckRepository.getCustomDeckIds()
            Result.Success(predefinedDecks + customDecks)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading available decks", e)
            Result.Error(e, "Failed to load deck list")
        }
    }
    suspend fun getAllAvailableDeckNames(): List<String> {
        // Get predefined deck names
        val predefinedDecks = getAvailablePlayerDeckNames().map { it ->
            it.replace("_", " ").split(" ").joinToString(" ") { word -> word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            } }
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
