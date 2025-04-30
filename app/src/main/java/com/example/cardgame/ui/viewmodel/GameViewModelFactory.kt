package com.example.cardgame.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cardgame.audio.SoundManager
import com.example.cardgame.data.ServiceLocator
import com.example.cardgame.data.repository.CampaignRepository
import com.example.cardgame.data.repository.CardRepository
import com.example.cardgame.data.repository.DeckBuilderRepository
import com.example.cardgame.data.storage.CardLoader

class GameViewModelFactory(private val soundManager: SoundManager) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GameViewModel::class.java) -> {
                // Get repositories from ServiceLocator
                val cardRepository = ServiceLocator.provideCardRepository()
                val campaignRepository = ServiceLocator.provideCampaignRepository()

                // Create and return the ViewModel with injected dependencies
                GameViewModel(cardRepository, campaignRepository, soundManager) as T
            }

            modelClass.isAssignableFrom(DeckBuilderViewModel::class.java) -> {
                // Get repositories from ServiceLocator
                val deckBuilderRepository = ServiceLocator.provideDeckBuilderRepository()

                DeckBuilderViewModel(deckBuilderRepository, soundManager) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        // Factory for creating a factory with SoundManager
        fun create(context: android.content.Context): GameViewModelFactory {
            val soundManager = SoundManager(context.applicationContext).apply { initialize() }
            return GameViewModelFactory(soundManager)
        }
    }
}