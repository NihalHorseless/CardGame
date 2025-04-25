package com.example.cardgame

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.cardgame.ui.navigations.CardGameNavigation
import com.example.cardgame.ui.viewmodel.GameViewModel
import com.example.cardgame.ui.screens.GameScreen
import com.example.cardgame.ui.theme.CardGameTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        screenConfigurations()
        enableEdgeToEdge()
        setContent {
            CardGameTheme {
                CardGameNavigation()
            }
        }
    }

    private fun screenConfigurations() {
        window?.let {
            // Enable immersive mode
            WindowCompat.setDecorFitsSystemWindows(it, false)
            WindowInsetsControllerCompat(it, it.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        // Lock to portrait mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

