package io.github.nihalhorseless.eternalglory.ui.screens.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.nihalhorseless.eternalglory.ui.screens.GameOverScreen
import io.github.nihalhorseless.eternalglory.ui.theme.CardGameTheme

@Preview
@Composable
fun GameOverScreenPreview() {
    CardGameTheme {
        GameOverScreen(isPlayerWinner = true, onReturnToMainMenu = {}, onPlaySound = {})
    }
}