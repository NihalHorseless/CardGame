package com.example.cardgame.data.storage

import android.content.Context
import com.example.cardgame.data.model.abilities.Ability
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class CardLoader(private val context: Context) {

    private val appContext = context.applicationContext

    // Create a custom Gson instance with type adapters for our card classes
    private val gson = GsonBuilder()
        .registerTypeAdapter(Card::class.java, CardTypeAdapter())
        .registerTypeAdapter(Ability::class.java, AbilityTypeAdapter())
        .create()

    // Cache for all loaded cards
    private val cardCache = mutableMapOf<Int, Card>()

    // Cache for predefined decks
    private val deckCache = mutableMapOf<String, Deck>()

    /**
     * Load all cards from the main cards JSON file
     */
    fun loadAllCards(): List<Card> {
        return loadCardsFromAssets("cards.json")
    }

    /**
     * Load cards from a specific JSON file in assets
     */
    fun loadCardsFromAssets(fileName: String): List<Card> {
        try {
            val inputStream = appContext.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()

            val cardListType = object : TypeToken<List<Card>>() {}.type
            val cards = gson.fromJson<List<Card>>(jsonString, cardListType)

            // Cache the cards for quick access
            cards.forEach { card -> cardCache[card.id] = card }

            return cards
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Get a card by its ID, from cache or by loading if needed
     */
    fun getCardById(id: Int): Card? {
        // Return from cache if available
        cardCache[id]?.let { return it }

        // If not in cache, try loading all cards
        if (cardCache.isEmpty()) {
            loadAllCards()
        }

        return cardCache[id]
    }

    /**
     * Load a predefined deck from assets
     */
    fun loadDeck(deckName: String): Deck? {
        // Return from cache if available
        deckCache[deckName]?.let { return it }

        try {
            // Try to load the deck definition
            val fileName = "$deckName.json"
            val inputStream = appContext.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()

            // Parse the deck JSON
            val deckData = gson.fromJson<DeckDefinition>(jsonString, DeckDefinition::class.java)

            // Make sure all cards are loaded
            if (cardCache.isEmpty()) {
                loadAllCards()
            }

            // Create the deck by looking up each card ID
            val cardList = deckData.cardIds.mapNotNull { cardId -> getCardById(cardId) }

            val deck = Deck(
                id = deckData.id,
                name = deckData.name,
                description = deckData.description,
                cards = cardList.toMutableList()
            )

            // Cache the deck
            deckCache[deckName] = deck

            return deck
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Get a list of all available predefined decks
     */
    fun getAvailableDeckNames(): List<String> {
        try {
            val deckFolderPath = "decks"
            val fileList = appContext.assets.list(deckFolderPath) ?: return emptyList()

            return fileList
                .filter { it.endsWith(".json") }
                .map { it.removeSuffix(".json") }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Data class for deck definition in JSON
     */
    private data class DeckDefinition(
        val id: String,
        val name: String,
        val description: String,
        val cardIds: List<Int>
    )
}