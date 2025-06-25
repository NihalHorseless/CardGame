package com.example.cardgame

import android.app.Application
import com.example.cardgame.data.ServiceLocator
import com.example.cardgame.util.CrashHandler

class GameApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CrashHandler.init(this)
        // Initialize ServiceLocator with application context
        ServiceLocator.init(this)
    }
}