package com.example.cardgame.data.storage

import android.content.Context
import android.util.Log
import com.example.cardgame.data.model.abilities.Ability
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class CardLoader(private val context: Context) {

    private val TAG = "CardLoader"
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
        return loadCardsFromAssets("decks/cards.json")
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

            Log.d(TAG, "Loaded ${cards.size} cards from $fileName")
            return cards
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cards from $fileName", e)
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
            // Try to load the deck definition from decks subdirectory
            val fileName = "decks/$deckName.json"
            Log.d(TAG, "Trying to load deck: $fileName")

            val inputStream = appContext.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()

            // Parse the deck JSON
            val deckData = gson.fromJson(jsonString, DeckDefinition::class.java)
            Log.d(TAG, "Loaded deck definition: ${deckData.name} with ${deckData.cardIds.size} cards")

            // Make sure all cards are loaded
            if (cardCache.isEmpty()) {
                loadAllCards()
            }

            // Create the deck by looking up each card ID
            val cardList = deckData.cardIds.mapNotNull { cardId ->
                val card = getCardById(cardId)
                if (card == null) {
                    Log.w(TAG, "Card with ID $cardId not found for deck $deckName")
                }
                card
            }

            Log.d(TAG, "Successfully loaded ${cardList.size}/${deckData.cardIds.size} cards for deck")

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
            Log.e(TAG, "Error loading deck: $deckName", e)
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

            val deckNames = fileList
                .filter { it.endsWith("deck.json") }
                .map { it.removeSuffix(".json") }

            Log.d(TAG, "Found ${deckNames.size} decks: ${deckNames.joinToString()}")
            return deckNames
        } catch (e: Exception) {
            Log.e(TAG, "Error listing available decks", e)
            return emptyList()
        }
    }

    /**
     * Create default decks if none exist in assets
     */
    fun createDefaultDecksIfNeeded(): List<String> {
        val availableDecks = getAvailableDeckNames()
        if (availableDecks.isNotEmpty()) {
            return availableDecks
        }

        // Create temporary decks in memory since we can't write to assets at runtime
        // In a real app, you'd create these files in your assets folder
        Log.d(TAG, "No decks found, creating default decks in memory")

        // Load all cards first
        val allCards = loadAllCards()
        if (allCards.isEmpty()) {
            Log.e(TAG, "Cannot create default decks: no cards available")
            return emptyList()
        }

        // Create some simple decks
        val unitCards = allCards.filter { it.toString().contains("UnitCard") }
        val tacticCards = allCards.filter { it.toString().contains("TacticCard") || it.toString().contains("EnhancedTacticCard") }

        // Player deck
        val playerDeckCards = (unitCards.take(12) + tacticCards.take(8)).toMutableList()
        val playerDeck = Deck(
            id = "player_deck",
            name = "Player's Balanced Deck",
            description = "A balanced deck with units and tactics",
            cards = playerDeckCards
        )
        deckCache["player_deck"] = playerDeck

        // Opponent deck
        val opponentDeckCards = (unitCards.takeLast(12) + tacticCards.takeLast(8)).toMutableList()
        val opponentDeck = Deck(
            id = "opponent_deck",
            name = "Opponent's Deck",
            description = "AI opponent's balanced deck",
            cards = opponentDeckCards
        )
        deckCache["opponent_deck"] = opponentDeck

        // Medieval themed deck
        val medievalCards = allCards.filter {
            it.toString().contains("MEDIEVAL") || it.toString().contains("TacticCard")
        }.take(20).toMutableList()
        val medievalDeck = Deck(
            id = "medieval_deck",
            name = "Medieval Forces",
            description = "A themed deck with medieval units",
            cards = medievalCards
        )
        deckCache["medieval_deck"] = medievalDeck

        Log.d(TAG, "Created default decks in memory")
        return listOf("player_deck", "opponent_deck", "medieval_deck")
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