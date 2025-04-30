package com.example.cardgame

import android.app.Application
import com.example.cardgame.data.ServiceLocator

class GameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize ServiceLocator with application context
        ServiceLocator.init(this)
    }
}