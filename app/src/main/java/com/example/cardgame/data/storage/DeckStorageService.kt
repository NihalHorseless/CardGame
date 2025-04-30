package com.example.cardgame.data.storage


import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.cardgame.data.model.abilities.Ability
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class DeckStorageService(private val context: Context) {
    private val TAG = "DeckStorageService"
    private val PREFS_NAME = "player_decks"
    private val DECK_LIST_KEY = "deck_list"
    private val MAX_PLAYER_DECKS = 5

    private val cardLoader = CardLoader(context)

    // Create a Gson instance that can handle our card types
    private val gson = GsonBuilder()
        .registerTypeAdapter(Card::class.java, CardTypeAdapter())
        .registerTypeAdapter(Ability::class.java, AbilityTypeAdapter())
        .create()

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get a list of all stored custom deck names
     */
    fun getCustomDeckNames(): List<String> {
        val deckListJson = prefs.getString(DECK_LIST_KEY, null) ?: return emptyList()
        return try {
            gson.fromJson(deckListJson, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading deck list", e)
            emptyList()
        }
    }

    /**
     * Get the total number of custom decks
     */
    fun getCustomDeckCount(): Int {
        return getCustomDeckNames().size
    }

    /**
     * Save a custom deck
     * @return true if saved successfully, false if maximum deck limit reached
     */
    fun saveDeck(deck: Deck): Boolean {
        // Get current deck list
        val deckNames = getCustomDeckNames().toMutableList()

        // If it's a new deck, check for maximum limit
        if (!deckNames.contains(deck.id) && deckNames.size >= MAX_PLAYER_DECKS) {
            return false
        }

        // Add to deck list if new
        if (!deckNames.contains(deck.id)) {
            deckNames.add(deck.id)
            saveDeckList(deckNames)
        }

        // Save the deck data
        val deckJson = gson.toJson(DeckStorage(
            id = deck.id,
            name = deck.name,
            description = deck.description,
            cardIds = deck.cards.map { it.id }
        ))

        prefs.edit().putString(deck.id, deckJson).apply()
        return true
    }

    /**
     * Load a custom deck by ID
     */
    fun loadDeck(deckId: String): Deck? {
        val deckJson = prefs.getString(deckId, null) ?: return null

        try {
            val deckStorage: DeckStorage = gson.fromJson(deckJson, DeckStorage::class.java)

            // Load all cards by their IDs
            val cards = deckStorage.cardIds.mapNotNull { cardId ->
                cardLoader.getCardById(cardId)
            }

            return Deck(
                id = deckStorage.id,
                name = deckStorage.name,
                description = deckStorage.description,
                isPlayerOwned = true,
                cards = cards.toMutableList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading deck $deckId", e)
            return null
        }
    }

    /**
     * Delete a custom deck
     */
    fun deleteDeck(deckId: String): Boolean {
        // Get current deck list
        val deckNames = getCustomDeckNames().toMutableList()

        // Remove from list
        if (!deckNames.remove(deckId)) {
            return false // Deck not found
        }

        // Update deck list
        saveDeckList(deckNames)

        // Remove deck data
        prefs.edit().remove(deckId).apply()
        return true
    }

    /**
     * Save the list of deck names
     */
    private fun saveDeckList(deckNames: List<String>) {
        val json = gson.toJson(deckNames)
        prefs.edit().putString(DECK_LIST_KEY, json).apply()
    }

    /**
     * Data class for storing deck info without the actual card objects
     */
    private data class DeckStorage(
        val id: String,
        val name: String,
        val description: String,
        val cardIds: List<Int>
    )
}