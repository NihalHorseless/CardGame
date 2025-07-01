package io.github.nihalhorseless.eternalglory.data.storage

import android.content.Context
import android.util.Log
import io.github.nihalhorseless.eternalglory.data.model.abilities.Ability
import io.github.nihalhorseless.eternalglory.data.model.card.Card
import io.github.nihalhorseless.eternalglory.data.model.card.Deck
import io.github.nihalhorseless.eternalglory.data.model.card.TacticCard
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class CardLoader(context: Context) {

    private val TAG = "CardLoader"
    private val appContext = context.applicationContext

    // Create separate Gson instances for different card types
    private val standardGson = GsonBuilder()
        .registerTypeAdapter(Card::class.java, CardTypeAdapter())
        .registerTypeAdapter(Ability::class.java, AbilityTypeAdapter())
        .create()

    private val tacticGson = GsonBuilder()
        .registerTypeAdapter(TacticCard::class.java, TacticCardDeserializer())
        .create()

    // Cache for all loaded cards
    private val cardCache = mutableMapOf<Int, Card>()

    // Cache for predefined decks
    private val deckCache = mutableMapOf<String, Deck>()

    /**
     * Load all cards from both standard and tactic card files
     */
    fun loadAllCards(): List<Card> {
        // Load regular units and fortifications
        val standardCards = loadStandardCards("decks/cards.json")

        // Load tactic cards from their own file
        val tacticCards = loadTacticCards("decks/tactic_cards.json")

        // Combine the lists and return
        return standardCards + tacticCards
    }

    /**
     * Load standard cards (units and fortifications) from a file
     */
    private fun loadStandardCards(fileName: String): List<Card> {
        try {
            val inputStream = appContext.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()

            // Use the standard card type token
            val cardListType = object : TypeToken<List<Card>>() {}.type
            val cards = standardGson.fromJson<List<Card>>(jsonString, cardListType)

            // Cache the cards for quick access
            cards.forEach { card -> cardCache[card.id] = card }

            Log.d(TAG, "Loaded ${cards.size} standard cards from $fileName")
            return cards
        } catch (e: Exception) {
            Log.e(TAG, "Error loading standard cards from $fileName", e)
            return emptyList()
        }
    }

    /**
     * Load tactic cards from a file
     */
    private fun loadTacticCards(fileName: String): List<TacticCard> {
        try {
            val inputStream = appContext.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()

            // Use the tactic card type token
            val tacticCardListType = object : TypeToken<List<TacticCard>>() {}.type
            val tacticCards = tacticGson.fromJson<List<TacticCard>>(jsonString, tacticCardListType)

            // Cache the tactic cards
            tacticCards.forEach { card -> cardCache[card.id] = card }

            Log.d(TAG, "Loaded ${tacticCards.size} tactic cards from $fileName")
            return tacticCards
        } catch (e: Exception) {
            Log.e(TAG, "Error loading tactic cards from $fileName", e)
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
    fun loadDeck(deckName: String, isAIDeck: Boolean = false): Deck? {
        // Check cache first
        deckCache[deckName]?.let { return it }

        try {
            // Determine correct path
            val deckPath = if (isAIDeck) "decks/ai" else "decks/player"
            val fileName = "$deckPath/$deckName.json"

            // Try to load the deck
            val inputStream = appContext.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()

            // Parse the deck JSON
            val deckData = standardGson.fromJson(jsonString, DeckDefinition::class.java)
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
                isPlayerOwned = !isAIDeck,
                cards = cardList.toMutableList()
            )

            // Cache the deck
            deckCache[deckName] = deck

            return deck
        } catch (e: Exception) {
            // Don't try recursive loading - just log and return null
            Log.e(TAG, "Error loading deck: $deckName from ${if(isAIDeck) "AI" else "player"} path", e)
            return null
        }
    }

    fun getAvailableDeckNames(): List<String> {
        try {
            val playerDeckFolderPath = "decks/player"
            val fileList = appContext.assets.list(playerDeckFolderPath) ?: return emptyList()

            val deckNames = fileList
                .filter { it.endsWith("deck.json") }
                .map { it.removeSuffix(".json") }


            Log.d(TAG, "Found ${deckNames.size} player decks: ${deckNames.joinToString()}")
            return deckNames
        } catch (e: Exception) {
            Log.e(TAG, "Error listing available player decks", e)
            return emptyList()
        }
    }
    fun getAvailableAIDeckNames(): List<String> {
        try {
            val aiDeckFolderPath = "decks/ai"
            val fileList = appContext.assets.list(aiDeckFolderPath) ?: return emptyList()

            val deckNames = fileList
                .filter { it.endsWith("deck.json") }
                .map { it.removeSuffix(".json") }

            Log.d(TAG, "Found ${deckNames.size} AI decks: ${deckNames.joinToString()}")
            return deckNames
        } catch (e: Exception) {
            Log.e(TAG, "Error listing available AI decks", e)
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