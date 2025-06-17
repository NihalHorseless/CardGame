package com.example.cardgame.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.cardgame.R
import com.example.cardgame.ui.theme.libreFont

@Composable
fun MainMenuScreen(
    onIntro: () -> Unit,
    onLeaveGame: () -> Unit,
    onStartGame: () -> Unit,
    onShowDeckBuilder: () -> Unit,
    onShowCampaign: () -> Unit,
    onShowGuide: () -> Unit,
    isMusicMuted: Boolean,
    onToggleMusicMute: () -> Unit
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
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        onIntro()
    }
    // Stop and Resume Track
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onIntro()
            } else if (event == Lifecycle.Event.ON_STOP) {
                onLeaveGame()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
        Image(
            painter = painterResource(id = R.drawable.main_screen_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
   /*     // Animated floating cards background
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
                        .size(80.dp,112.dp)
                        .offset(x = cardXOffset.dp, y = cardYOffset.dp)
                        .rotate(rotation)
                        .alpha(0.5f)
                        .blur(2.dp)
                ) {
                    Image(painter = painterResource(R.drawable.card_back), contentDescription = "Card Menu", modifier = Modifier.fillMaxSize())

                }
            }
        }

    */

        // Title and menu options
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ETERNAL GLORY",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = libreFont,
                    shadow = Shadow(
                        color = Color(0xFF0C2D3D),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                color = Color(0xFFC7AD78),
                textAlign = TextAlign.Center)


            Spacer(modifier = Modifier.height(176.dp))

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) {
                MenuButton(
                    text = "Custom Game",
                    onClick = onStartGame
                )
                MenuButton(
                    text = "Campaign Mode",
                    onClick = onShowCampaign
                )
                MenuButton(
                    text = "Deck Builder",
                    onClick = onShowDeckBuilder
                )
                MenuButton(
                    text = "Game Guide",
                    onClick = onShowGuide
                )
            }

        }
        // Add a mute button in the top right corner
        IconButton(
            onClick = { onToggleMusicMute() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(48.dp)
                .background(Color(0xFF512602).copy(alpha = 0.7f), CircleShape)
                .border(1.dp, Color(0xFF0D1B0C), CircleShape)
        ) {
            Icon(
                painter = painterResource(if (isMusicMuted) R.drawable.baseline_music_off else R.drawable.baseline_music_on),
                contentDescription = if (isMusicMuted) "Unmute Music" else "Mute Music",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
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
            listOf(Color(0xFF841B09), Color(0xFF0D3041))
        } else {
            listOf(Color(0xFF841B09), Color(0xFF0D3041))
        }
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .clickable { onClick() }
            .padding(horizontal = 32.dp, vertical = 12.dp)
       /*     .pointerInput(Unit) {
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

        */
        ,contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6E3901),
            fontFamily = libreFont
        )
    }

}