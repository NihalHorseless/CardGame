package com.example.cardgame.ui.screens.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.cardgame.ui.screens.GameOverScreen
import com.example.cardgame.ui.theme.CardGameTheme

@Preview
@Composable
fun GameOverScreenPreview() {
    CardGameTheme {
        GameOverScreen(isPlayerWinner = true, onReturnToMainMenu = {}, onPlaySound = {})
    }
}