package com.example.cardgame.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuScreen(
    onIntro: () -> Unit,
    onStartGame: () -> Unit,
    onShowDeckBuilder: () -> Unit,
    onShowOptions: () -> Unit,
    onShowCampaign: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val bgOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    LaunchedEffect(Unit) {
        onIntro()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1F2233),
                        Color(0xFF2D3250),
                        Color(0xFF1F2233)
                    ),
                    start = Offset(bgOffset, 0f),
                    end = Offset(bgOffset + 500f, 500f)
                )
            )
    ) {
        // Animated floating cards background
        Box(modifier = Modifier.fillMaxSize()) {
            repeat(10) { index ->
                val cardXOffset by infiniteTransition.animateFloat(
                    initialValue = -100f,
                    targetValue = 1100f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 15000 + (index * 1000),
                            easing = LinearEasing,
                            delayMillis = index * 500
                        ),
                        repeatMode = RepeatMode.Restart
                    )
                )

                val cardYOffset by infiniteTransition.animateFloat(
                    initialValue = 100f,
                    targetValue = 900f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 20000 + (index * 800),
                            easing = LinearEasing,
                            delayMillis = index * 300
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(20000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = cardXOffset.dp, y = cardYOffset.dp)
                        .rotate(rotation)
                        .alpha(0.2f)
                        .blur(4.dp)
                ) {

                }
            }
        }

        // Title and menu options
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CARD BATTLER",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    shadow = Shadow(
                        color = Color(0xFF5271FF),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Strategy Card Game",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            MenuButton(
                text = "Start Game",
                onClick = onStartGame
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = "Campaign Mode",
                onClick = onShowCampaign
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = "Deck Builder",
                onClick = onShowDeckBuilder
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = "Options",
                onClick = onShowOptions
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = "Exit Game",
                onClick = { /* Handle exit */ }
            )
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = tween(200)
    )

    val buttonGradient = Brush.horizontalGradient(
        colors = if (isHovered) {
            listOf(Color(0xFF5271FF), Color(0xFF6E8AFF))
        } else {
            listOf(Color(0xFF3D55CC), Color(0xFF5271FF))
        }
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(buttonGradient)
            .clickable { onClick() }
            .padding(horizontal = 32.dp, vertical = 12.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White
        )
    }
}