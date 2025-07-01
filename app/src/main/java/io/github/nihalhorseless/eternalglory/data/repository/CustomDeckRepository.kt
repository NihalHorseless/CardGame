package io.github.nihalhorseless.eternalglory.data.repository

import androidx.room.withTransaction
import io.github.nihalhorseless.eternalglory.data.db.CardGameDatabase
import io.github.nihalhorseless.eternalglory.data.db.DeckCardEntity
import io.github.nihalhorseless.eternalglory.data.db.DeckEntity
import io.github.nihalhorseless.eternalglory.data.model.card.Deck
import io.github.nihalhorseless.eternalglory.data.storage.CardLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CustomDeckRepository(
    private val database: CardGameDatabase,
    private val cardLoader: CardLoader
) {
    private val deckDao = database.deckDao()
    private val deckCardDao = database.deckCardDao()

    /**
     * Get all custom deck IDs
     */
    suspend fun getCustomDeckIds(): List<String> = withContext(Dispatchers.IO) {
        deckDao.getAllDecks().map { it.id }
    }

    /**
     * Get the count of custom decks
     */
    suspend fun getCustomDeckCount(): Int = withContext(Dispatchers.IO) {
        deckDao.getDeckCount()
    }

    /**
     * Save a custom deck
     * @return true if saved successfully
     */
    suspend fun saveDeck(deck: Deck): Boolean = withContext(Dispatchers.IO) {
        try {
            // Create the deck entity
            val deckEntity = DeckEntity(
                id = deck.id,
                name = deck.name,
                description = deck.description
            )

            // Create card entities
            val cardEntities = deck.cards.mapIndexed { index, card ->
                DeckCardEntity(
                    deckId = deck.id,
                    cardId = card.id,
                    position = index
                )
            }

            // Insert data in a transaction
            database.withTransaction {
                // Insert/update the deck
                deckDao.insertDeck(deckEntity)

                // Delete existing cards and insert new ones
                deckCardDao.deleteCardsForDeck(deck.id)
                deckCardDao.insertCards(cardEntities)
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Load a custom deck by ID
     */
    suspend fun loadDeck(deckId: String): Deck? = withContext(Dispatchers.IO) {
        try {
            // Get deck entity
            val deckEntity = deckDao.getDeckById(deckId) ?: return@withContext null

            // Get associated card entities
            val cardEntities = deckCardDao.getCardsForDeck(deckId)

            // Load actual card objects using the card loader
            val cards = cardEntities
                .sortedBy { it.position }
                .mapNotNull { cardLoader.getCardById(it.cardId) }
                .toMutableList()

            // Create and return the deck object
            Deck(
                id = deckEntity.id,
                name = deckEntity.name,
                description = deckEntity.description,
                isPlayerOwned = true,
                cards = cards
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get all custom decks
     */
    suspend fun getAllCustomDecks(): List<Deck> = withContext(Dispatchers.IO) {
        try {
            // Get all deck entities
            val deckEntities = deckDao.getAllDecks()

            // Load each deck with its cards
            deckEntities.mapNotNull { deckEntity ->
                loadDeck(deckEntity.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Delete a custom deck
     * @return true if deleted successfully
     */
    suspend fun deleteDeck(deckId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Delete the deck (cards will be deleted via CASCADE)
            val deletedRows = deckDao.deleteDeck(deckId)
            deletedRows > 0
        } catch (e: Exception) {
            false
        }
    }
}
