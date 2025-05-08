package com.example.cardgame.ui.components.effects

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import com.example.cardgame.R
import com.example.cardgame.data.enum.TacticCardType
import kotlinx.coroutines.delay

@Composable
fun TacticCardEffectAnimation(
    isVisible: Boolean,
    cardType: TacticCardType,
    targetPosition: Pair<Float, Float>,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    // Setup Coil's ImageLoader with GIF decoder
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    // Select the appropriate GIF resource based on card type
    val gifResourceId = when (cardType) {
        TacticCardType.DIRECT_DAMAGE -> R.drawable.tactical_card_rockets // Fire/impact effect
        TacticCardType.AREA_EFFECT -> R.drawable.big_kaboom // Explosion effect
        TacticCardType.BUFF -> R.drawable.buff_particles // Green magical glow
        TacticCardType.DEBUFF -> R.drawable.tactical_card_debuff // Purple energy swirl
        TacticCardType.SPECIAL -> R.drawable.buff_particles // Blue magical sparkles
    }

    // Determine animation size based on type (area effects are larger)
    val animationSize = when (cardType) {
        TacticCardType.AREA_EFFECT -> 210.dp
        TacticCardType.DIRECT_DAMAGE -> 100.dp
        TacticCardType.DEBUFF -> 60.dp
        else -> 70.dp
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Convert to dp for proper Compose positioning
        val targetXDp = with(LocalDensity.current) { targetPosition.first.toDp() }
        val targetYDp = with(LocalDensity.current) { targetPosition.second.toDp() }

        // Calculate offset to center animation on target
        val offsetX = targetXDp - (animationSize / 2)
        val offsetY = targetYDp - (animationSize / 2)

        // For area effects, add a subtle scale pulsing
        val scale = if (cardType == TacticCardType.AREA_EFFECT) {
            val pulseScale by rememberInfiniteTransition().animateFloat(
                initialValue = 0.9f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            pulseScale
        } else 1f

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(gifResourceId)
                .build(),
            contentDescription = "Spell Effect Animation",
            imageLoader = imageLoader,
            modifier = Modifier
                .size(animationSize)
                .offset(x = offsetX, y = offsetY)
                .scale(scale)
        )

        // Additional particle effects for certain spell types


        // Trigger completion after animation duration
        LaunchedEffect(isVisible) {
                delay(1200) // Animation duration - slightly longer than GIFs
                onAnimationComplete()
        }
    }
}