package com.example.cardgame.ui.components.effects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun DeckShuffleAnimation(
    isVisible: Boolean,
    onAnimationComplete: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var currentRotation by remember { mutableFloatStateOf(0f) }
                var cardOffset by remember { mutableFloatStateOf(0f) }

                LaunchedEffect(key1 = isVisible) {
                    if (isVisible) {
                        // Shuffle animation
                        repeat(5) {
                            currentRotation = if (it % 2 == 0) 15f else -15f
                            cardOffset = if (it % 2 == 0) 10f else -10f
                            delay(200)
                        }
                        delay(300)
                        onAnimationComplete()
                    }
                }

                Box(
                    modifier = Modifier.offset(x = cardOffset.dp)
                ) {
                    // Animated deck of cards
                    repeat(10) { index ->
                        Box(
                            modifier = Modifier
                                .offset(x = (index * 0.5f).dp, y = (index * 0.5f).dp)
                                .size(width = 60.dp, height = 80.dp)
                                .rotate(currentRotation)
                                .background(
                                    color = Color(0xFF1E3C72),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Shuffling Deck...",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}