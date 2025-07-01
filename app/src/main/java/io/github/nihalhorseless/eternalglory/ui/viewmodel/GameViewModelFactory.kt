package io.github.nihalhorseless.eternalglory.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.nihalhorseless.eternalglory.audio.MusicManager
import io.github.nihalhorseless.eternalglory.audio.SoundManager
import io.github.nihalhorseless.eternalglory.data.ServiceLocator

class GameViewModelFactory(private val soundManager: SoundManager,
                           private val musicManager: MusicManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GameViewModel::class.java) -> {
                // Get repositories from ServiceLocator
                val cardRepository = ServiceLocator.provideCardRepository()
                val campaignRepository = ServiceLocator.provideCampaignRepository()

                // Create and return the ViewModel with injected dependencies
                GameViewModel(cardRepository, campaignRepository, soundManager,musicManager) as T
            }

            modelClass.isAssignableFrom(DeckBuilderViewModel::class.java) -> {
                // Get repositories from ServiceLocator
                val deckBuilderRepository = ServiceLocator.provideDeckBuilderRepository()

                DeckBuilderViewModel(deckBuilderRepository, soundManager,musicManager) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        // Factory for creating a factory with SoundManager
        fun create(context: Context): GameViewModelFactory {
            val soundManager = SoundManager(context.applicationContext).apply { initialize() }
            val musicManager = MusicManager(context.applicationContext)
            return GameViewModelFactory(soundManager,musicManager)
        }
    }
}