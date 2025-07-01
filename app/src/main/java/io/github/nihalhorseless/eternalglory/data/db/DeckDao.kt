package io.github.nihalhorseless.eternalglory.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    suspend fun getAllDecks(): List<DeckEntity>

    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: String): DeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity)

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeck(deckId: String): Int

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun getDeckCount(): Int
}

@Dao
interface DeckCardDao {
    @Query("SELECT * FROM deck_cards WHERE deckId = :deckId ORDER BY position")
    suspend fun getCardsForDeck(deckId: String): List<DeckCardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<DeckCardEntity>)

    @Query("DELETE FROM deck_cards WHERE deckId = :deckId")
    suspend fun deleteCardsForDeck(deckId: String): Int
}