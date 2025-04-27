package com.example.cardgame.ui.components.effects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// Card Play Animation
@Composable
fun CardPlayAnimation(
    isVisible: Boolean,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Position animation
    val animatedX by animateFloatAsState(
        targetValue = if (isVisible) endX else startX,
        animationSpec = tween(500, easing = EaseOutQuad)
    )

    val animatedY by animateFloatAsState(
        targetValue = if (isVisible) endY else startY,
        animationSpec = tween(500, easing = EaseOutQuad)
    )

    // Scale and rotate for dramatic effect
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1.5f else 0.8f,
        animationSpec = tween(500, easing = EaseOutBack)
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else -10f,
        animationSpec = tween(500)
    )

    // Glow effect opacity
    val glowAlpha by animateFloatAsState(
        targetValue = if (isVisible) 0.7f else 0f,
        animationSpec = tween(500)
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(1000) // Duration of animation
            onAnimationComplete()
        }
    }

    if (isVisible) {
        Box(modifier = modifier) {
            // Glow effect
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(glowAlpha)
            ) {
                drawCircle(
                    color = Color(0xFF5271FF),
                    radius = 150f,
                    center = androidx.compose.ui.geometry.Offset(animatedX, animatedY),
                    alpha = glowAlpha
                )
            }

            // Particle effects (simplified)
            for (i in 0..5) {
                val particleDelay = i * 100
                val particleScale by animateFloatAsState(
                    targetValue = if (isVisible) 0f else 1f,
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = particleDelay
                    )
                )

                val particleAlpha by animateFloatAsState(
                    targetValue = if (isVisible) 0f else 0.7f,
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = particleDelay
                    )
                )

                // Simple particle as a colored box
                Box(
                    modifier = Modifier
                        .offset(
                            x = (animatedX + (i * 20 - 50)).dp,
                            y = (animatedY + (i % 3 * 20 - 30)).dp
                        )
                        .size(10.dp)
                        .scale(particleScale)
                        .alpha(particleAlpha)
                        .background(Color(0xFFFFD700))
                )
            }
        }
    }
}

// Card Hover Effect
@Composable
fun CardWithHoverEffect(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.1f else 1.0f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        )
    )

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 8.dp else 2.dp,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        )
    )

    Box(
        modifier = modifier
            .scale(scale)
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
        content()
    }
}

// Card Flip Animation
@Composable
fun FlippableCard(
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit,
    isFlipped: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 400
        )
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // Show front
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                frontContent()
            }
        } else {
            // Show back (reversed to account for the flip)
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    },
                contentAlignment = Alignment.Center
            ) {
                backContent()
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
        UnitType.CAVALRY -> R.drawable.blood_slash
        UnitType.INFANTRY -> R.drawable.blood_slash
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
            UnitType.CAVALRY -> 50.dp
            UnitType.INFANTRY -> 50.dp
            UnitType.MISSILE -> 50.dp
            UnitType.ARTILLERY -> 90.dp
            UnitType.MUSKET -> 80.dp
        }
        val offsetX =
            if (unitType == UnitType.INFANTRY || unitType == UnitType.CAVALRY) targetXDp - (animSize / 2 ) else targetXDp - (animSize / 2)
        val offsetY =
            if (unitType == UnitType.INFANTRY || unitType == UnitType.CAVALRY) targetYDp - (25.dp) else targetYDp - (animSize / 2)

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

// Damage Number Animation
@Composable
fun DamageNumberEffect(
    damage: Int,
    isHealing: Boolean = false,
    x: Float,
    y: Float,
    isVisible: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Convert the absolute screen coordinates to dp
    val density = LocalDensity.current
    val xDp = with(density) { x.toDp() }
    val yDp = with(density) { y.toDp() }

    var yOffset by remember { mutableFloatStateOf(0f) }
    val animatedYOffset by animateFloatAsState(
        targetValue = if (isVisible) -50f else 0f,
        animationSpec = tween(800),
        finishedListener = { if (it == -50f) onAnimationComplete() }
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(800)
            onAnimationComplete()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center)
    ) {
        yOffset = animatedYOffset

        Box(
            modifier = modifier
                .offset(x = xDp - 20.dp, y = yDp + animatedYOffset.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isHealing) "+$damage" else "-$damage",
                color = if (isHealing) Color.Green else Color.Red,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}
