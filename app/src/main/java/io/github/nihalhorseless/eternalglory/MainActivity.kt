package io.github.nihalhorseless.eternalglory

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.nihalhorseless.eternalglory.ui.navigations.CardGameNavigation
import io.github.nihalhorseless.eternalglory.ui.theme.CardGameTheme

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


    @SuppressLint("SourceLockedOrientationActivity")
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

