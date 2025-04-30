package com.example.cardgame.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Entity representing a custom deck
 */
@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entity representing a card in a deck
 */
@Entity(
    tableName = "deck_cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deckId")]
)
data class DeckCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: String,
    val cardId: Int,
    val position: Int  // For maintaining card order
)

data class DeckWithCards(
    @Embedded val deck: DeckEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "deckId"
    )
    val cards: List<DeckCardEntity>
)
