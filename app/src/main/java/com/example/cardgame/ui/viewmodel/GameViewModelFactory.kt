package com.example.cardgame.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cardgame.data.repository.CardRepository
import com.example.cardgame.data.storage.CardLoader

class GameViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            // Create dependencies
            val cardLoader = CardLoader(context.applicationContext)
            val cardRepository = CardRepository(cardLoader)

            // Create and return the ViewModel with injected dependencies
            return GameViewModel(cardRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}