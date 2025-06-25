package com.example.cardgame.ui.screens

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.R
import com.example.cardgame.ui.theme.TurkishRed
import com.example.cardgame.ui.theme.libreFont
import kotlinx.coroutines.delay

@Composable
fun GameOverScreen(
    isPlayerWinner: Boolean,
    onReturnToMainMenu: () -> Unit,
    onPlaySound: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation states
    var isVisible by remember { mutableStateOf(false) }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 0.9f else 0f,
        animationSpec = tween(1000)
    )

    val headerScale by animateFloatAsState(
        targetValue = if (isVisible) 1.2f else 0.5f,
        animationSpec = tween(1000, easing = EaseOutBack)
    )

    // Start animation when composed
    LaunchedEffect(key1 = Unit) {
        delay(300) // Short delay before starting animation
        isVisible = true
        onPlaySound()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF12121F)
                    ),
                    radius = 1000f
                )
            )
            .alpha(animatedAlpha),
        contentAlignment = Alignment.Center
    ) {
        Image(
        painter = painterResource(id = if(isPlayerWinner) R.drawable.victory_image else R.drawable.defeat_image),
        contentDescription = "Game Over Screen Background Image",
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(0.8f)
                .fillMaxHeight()
        ) {
            val resultMessage = if (isPlayerWinner) "Victoire!" else "Défaite!"
            val resultColor = if (isPlayerWinner) Color(0xFFFFD700) else TurkishRed

            // Game over header
            Text(
                text = resultMessage,
                color = resultColor,
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = libreFont,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(headerScale)
            )


            // Buttons with appropriate animations

            GameOverButton(
                text = "Return to Menu",
                onClick = onReturnToMainMenu,
                colors = ButtonDefaults.buttonColors(
                    containerColor = resultColor
                ),
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}

@Composable
fun GameOverButton(
    text: String,
    onClick: () -> Unit,
    colors: ButtonColors,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = tween(200)
    )

    Button(
        onClick = onClick,
        colors = colors,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .scale(scale)
            .padding(8.dp)
            .height(56.dp)
            .border(
                width = 4.dp,
                color =  Color(0xFF0D222D),
                shape = RoundedCornerShape(2.dp)
            )
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
            }
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}