package com.example.cardgame.ui.components.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.cardgame.R

@Composable
fun BattlefieldBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Main battlefield image
        Image(
            painter = painterResource(id = R.drawable.battlefield_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Optional: Add a semi-transparent overlay to ensure game elements are visible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color(0xFF2D3250).copy(alpha = 0.25f)
                )
        )
    }
}