package com.example.cardgame.ui.components.effects

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import com.example.cardgame.R
import com.example.cardgame.data.enum.UnitType
import kotlinx.coroutines.delay

@Composable
fun CardSlotAnimation(
    isVisible: Boolean,
    targetX: Float,
    targetY: Float,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Simple fade in and scale up animation
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300)
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = tween(300, easing = EaseOutBack)
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(300) // Animation duration
            onAnimationComplete()
        }
    }

    if (isVisible) {
        Box(
            modifier = modifier
                .offset(x = targetX.dp - 32.5.dp, y = targetY.dp - 40.dp)
                .size(65.dp, 80.dp)
                .scale(scale)
                .alpha(alpha)
        ) {
            // A simple glowing effect
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFF5271FF),
                    radius = 60f,
                    alpha = 0.5f * alpha
                )
            }
        }
    }
}

@Composable
fun GifAttackAnimation(
    unitType: UnitType,
    isVisible: Boolean,
    targetX: Float,
    targetY: Float,
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
    val animationRes = when (unitType) {
        UnitType.CAVALRY -> R.drawable.blood_splash
        UnitType.INFANTRY -> R.drawable.blood_splash
        UnitType.MISSILE -> R.drawable.arrow_rain_two
        UnitType.ARTILLERY -> R.drawable.blood_explosion
        UnitType.MUSKET -> R.drawable.artillery_hit_anim
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Convert to dp for proper Compose positioning
        val targetXDp = with(LocalDensity.current) { targetX.toDp() }
        val targetYDp = with(LocalDensity.current) { targetY.toDp() }

        // Calculate offset to center animation on target
        // (half the animation size)
        val animSize = when (unitType) {
            UnitType.CAVALRY -> 70.dp
            UnitType.INFANTRY -> 70.dp
            UnitType.MISSILE -> 50.dp
            UnitType.ARTILLERY -> 90.dp
            UnitType.MUSKET -> 80.dp
        }
        val offsetX =
            if (unitType == UnitType.INFANTRY || unitType == UnitType.CAVALRY) targetXDp - (animSize / 2 ) else targetXDp - (animSize / 2)
        val offsetY =
            if (unitType == UnitType.INFANTRY || unitType == UnitType.CAVALRY) targetYDp - (40.dp) else targetYDp - (animSize / 2)

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(animationRes)
                .build(),
            contentDescription = "Attack Animation",
            imageLoader = imageLoader,
            modifier = Modifier
                .size(animSize)
                .offset(x = offsetX, y = offsetY)
        )

        // Auto-dismiss animation after its estimated duration (adjust based on your GIFs)
        LaunchedEffect(isVisible) {
            delay(1000) // Adjust this duration to match your GIF length
            onAnimationComplete()
        }
    }
}

