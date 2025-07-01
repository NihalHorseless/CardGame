package io.github.nihalhorseless.eternalglory.game.testing

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nihalhorseless.eternalglory.BuildConfig
import kotlinx.coroutines.delay

class PerformanceMonitor {
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    private val fpsHistory = mutableListOf<Int>()

    @Composable
    fun PerformanceOverlay(
        modifier: Modifier = Modifier
    ) {
        if (!BuildConfig.DEBUG) return

        var currentFps by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(16) // Check every frame
                frameCount++

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFpsTime >= 1000) {
                    currentFps = frameCount
                    fpsHistory.add(frameCount)
                    frameCount = 0
                    lastFpsTime = currentTime

                    // Alert if FPS drops below 30
                    if (currentFps < 30) {
                        Log.w("Performance", "Low FPS detected: $currentFps")
                    }
                }
            }
        }

        // Show FPS overlay
        Box(
            modifier = modifier
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(8.dp)
        ) {
            Text(
                text = "FPS: $currentFps",
                color = when {
                    currentFps >= 55 -> Color.Green
                    currentFps >= 30 -> Color.Yellow
                    else -> Color.Red
                },
                fontSize = 12.sp
            )
        }
    }
}