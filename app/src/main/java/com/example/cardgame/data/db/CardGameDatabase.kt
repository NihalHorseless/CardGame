package com.example.cardgame.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DeckEntity::class, DeckCardEntity::class], version = 1)
abstract class CardGameDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun deckCardDao(): DeckCardDao

    companion object {
        @Volatile
        private var INSTANCE: CardGameDatabase? = null

        fun getInstance(context: Context): CardGameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CardGameDatabase::class.java,
                    "card_game_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}