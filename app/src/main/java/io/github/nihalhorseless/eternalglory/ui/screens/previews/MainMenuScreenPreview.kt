package io.github.nihalhorseless.eternalglory.ui.screens.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.github.nihalhorseless.eternalglory.ui.screens.MainMenuScreen
import io.github.nihalhorseless.eternalglory.ui.theme.CardGameTheme

@Preview(showBackground = true, widthDp = 393, heightDp = 851)
@Composable
fun MainMenuScreenPreview() {
    CardGameTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1F2233),
                            Color(0xFF2D3250),
                            Color(0xFF1F2233)
                        )
                    )
                )
        ) {
            // Call the actual MainMenuScreen with mock functions
            MainMenuScreen(
                onIntro = {},
                onLeaveGame = {},
                onStartGame = {},
                onShowDeckBuilder = {},
                onShowCampaign = {},
                onShowGuide = {},
                onShowCredits = {},
                isMusicMuted = false,
                onToggleMusicMute = {}
            )
        }
    }
}

@Preview(name = "Pixel 2", device = "id:pixel_2")
@Composable
fun MainMenuScreenPreviewPixel2() {
    MainMenuScreenPreview()
}

@Preview(name = "Pixel 3", device = "id:pixel_3")
@Composable
fun MainMenuScreenPreviewPixel3() {
    MainMenuScreenPreview()
}

@Preview(name = "Pixel 4", device = "id:pixel_4")
@Composable
fun MainMenuScreenPreviewPixel4() {
    MainMenuScreenPreview()
}

@Preview(name = "Pixel 5", device = "id:pixel_5")
@Composable
fun MainMenuScreenPreviewPixel5() {
    MainMenuScreenPreview()
}