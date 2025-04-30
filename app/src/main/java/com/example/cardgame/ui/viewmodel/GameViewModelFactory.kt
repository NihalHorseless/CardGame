package com.example.cardgame.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cardgame.audio.SoundManager
import com.example.cardgame.data.repository.CampaignRepository
import com.example.cardgame.data.repository.CardRepository
import com.example.cardgame.data.repository.DeckBuilderRepository
import com.example.cardgame.data.storage.CardLoader

class GameViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(GameViewModel::class.java) -> {
                // Create dependencies
                val cardLoader = CardLoader(context.applicationContext)
                val cardRepository = CardRepository(cardLoader)
                val campaignRepository = CampaignRepository(context)
                val soundManager = SoundManager(context.applicationContext).apply { initialize() }

                // Create and return the ViewModel with injected dependencies
                return GameViewModel(cardRepository, campaignRepository, soundManager) as T
            }

            modelClass.isAssignableFrom(DeckBuilderViewModel::class.java) -> {
                val deckBuilderRepository = DeckBuilderRepository(context.applicationContext)
                val soundManager = SoundManager(context.applicationContext).apply { initialize() }
                return DeckBuilderViewModel(deckBuilderRepository,soundManager) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}