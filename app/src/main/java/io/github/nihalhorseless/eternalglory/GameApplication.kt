package io.github.nihalhorseless.eternalglory

import android.app.Application
import io.github.nihalhorseless.eternalglory.data.ServiceLocator
import io.github.nihalhorseless.eternalglory.util.CrashHandler

class GameApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CrashHandler.init(this)
        // Initialize ServiceLocator with application context
        ServiceLocator.init(this)
    }
}