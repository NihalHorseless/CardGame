package io.github.nihalhorseless.eternalglory.data

import android.content.Context
import io.github.nihalhorseless.eternalglory.audio.MusicManager
import io.github.nihalhorseless.eternalglory.data.db.CardGameDatabase
import io.github.nihalhorseless.eternalglory.data.repository.CampaignRepository
import io.github.nihalhorseless.eternalglory.data.repository.CardRepository
import io.github.nihalhorseless.eternalglory.data.repository.CustomDeckRepository
import io.github.nihalhorseless.eternalglory.data.repository.DeckBuilderRepository
import io.github.nihalhorseless.eternalglory.data.storage.CardLoader

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
    private val musicManager by lazy {
        val appContext = checkNotNull(applicationContext) { "Application context not initialized" }
        MusicManager(appContext)
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
    fun provideMusicManager(): MusicManager = musicManager
    fun provideCardRepository(): CardRepository = cardRepository
    fun provideCampaignRepository(): CampaignRepository = campaignRepository
    fun provideDeckBuilderRepository(): DeckBuilderRepository = deckBuilderRepository
}