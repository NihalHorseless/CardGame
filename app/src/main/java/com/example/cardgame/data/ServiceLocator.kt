package com.example.cardgame.data

import android.content.Context
import com.example.cardgame.data.db.CardGameDatabase
import com.example.cardgame.data.repository.CampaignRepository
import com.example.cardgame.data.repository.CardRepository
import com.example.cardgame.data.repository.CustomDeckRepository
import com.example.cardgame.data.repository.DeckBuilderRepository
import com.example.cardgame.data.storage.CardLoader
import com.example.cardgame.data.storage.DeckStorageService

object ServiceLocator {
    private var applicationContext: Context? = null

    // Database
    private val database by lazy {
        val appContext = checkNotNull(applicationContext) { "Application context not initialized" }
        CardGameDatabase.getInstance(appContext)
    }

    // Services
    private val cardLoader by lazy {
        val appContext = checkNotNull(applicationContext) { "Application context not initialized" }
        CardLoader(appContext)
    }

    // Repositories
    private val customDeckRepository by lazy {
        CustomDeckRepository(database, cardLoader)
    }

    private val cardRepository by lazy {
        CardRepository(cardLoader, customDeckRepository)
    }

    private val campaignRepository by lazy {
        val appContext = checkNotNull(applicationContext) { "Application context not initialized" }
        CampaignRepository(appContext)
    }

    private val deckBuilderRepository by lazy {
        DeckBuilderRepository(cardRepository)
    }

    /**
     * Initialize the ServiceLocator with application context
     */
    fun init(context: Context) {
        if (applicationContext == null) {
            applicationContext = context.applicationContext
        }
    }

    // Accessor methods for repositories
    fun provideCardRepository(): CardRepository = cardRepository
    fun provideCampaignRepository(): CampaignRepository = campaignRepository
    fun provideDeckBuilderRepository(): DeckBuilderRepository = deckBuilderRepository
}