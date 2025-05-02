package com.example.cardgame.ui.components.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cardgame.R
import com.example.cardgame.data.enum.UnitType
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun UnitMovementAnimation(
    isVisible: Boolean,
    unitType: UnitType,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    // Create animatables for position and appearance
    val xPosition = remember { Animatable(startX) }
    val yPosition = remember { Animatable(startY) }
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }

    // Animation duration
    val movementDuration = 500 // milliseconds

    // Start animations when visible
    LaunchedEffect(key1 = isVisible) {
        // Fade in
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 100)
        )

        // Scale up
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 150)
        )

        // Short delay before movement
        delay(50)

        // Animate position
        xPosition.animateTo(
            targetValue = endX,
            animationSpec = tween(durationMillis = movementDuration, easing = EaseInOutQuad)
        )
        yPosition.animateTo(
            targetValue = endY,
            animationSpec = tween(durationMillis = movementDuration, easing = EaseInOutQuad)
        )

        // Wait briefly at destination
        delay(100)

        // Fade out
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 100)
        )

        // Notify animation completion
        onAnimationComplete()
    }

    // Determine which unit icon to show based on unit type
    val unitResource = when (unitType) {
        UnitType.INFANTRY -> R.drawable.unit_type_infantry
        UnitType.CAVALRY -> R.drawable.unit_type_cavalry
        UnitType.ARTILLERY -> R.drawable.unit_type_cannon
        UnitType.MISSILE -> R.drawable.unit_type_missile
        UnitType.MUSKET -> R.drawable.unit_type_musket
    }

    // Convert to dp for offset
    val xPositionDp = with(LocalDensity.current) { (xPosition.value - 25).dp }
    val yPositionDp = with(LocalDensity.current) { (yPosition.value - 25).dp }

    Box(modifier = modifier) {
        // Draw moving unit
        Image(
            painter = painterResource(id = unitResource),
            contentDescription = "Moving ${unitType.name}",
            modifier = Modifier
                .offset(x = xPositionDp, y = yPositionDp)
                .size(50.dp)
                .scale(scale.value)
                .alpha(alpha.value),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun UnitDeathAnimation(
    unitType: UnitType,
    isVisible: Boolean,
    position: Pair<Float, Float>,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation progress values
    val flipProgress by animateFloatAsState(
        targetValue = if (isVisible) 0f else 180f,
        animationSpec = tween(600, easing = EaseInOutQuad),
        finishedListener = { if (it >= 180f) onAnimationComplete() }
    )

    // Only start disintegration after flip reaches halfway point
    val disintegrationProgress by animateFloatAsState(
        targetValue = if (flipProgress < 90f) 0f else 1f,
        animationSpec = tween(800, easing = EaseOutQuad)
    )

    // Unit size
    val unitSize = 65.dp

    if (isVisible || flipProgress > 0f) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Convert to dp for Compose positioning
            val positionX = with(LocalDensity.current) { position.first.toDp() }
            val positionY = with(LocalDensity.current) { position.second.toDp() }

            // Container for the unit and particles
            Box(
                modifier = Modifier
                    .offset(x = positionX - unitSize/2, y = positionY - unitSize/2)
                    .size(unitSize)
            ) {
                // 3D flip effect for the unit
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = flipProgress
                            cameraDistance = 8f * density
                        }
                ) {
                    // Front of card (original unit)
                    if (flipProgress <= 90f) {
                        Image(
                            painter = painterResource(id = getUnitTypeResource(unitType)),
                            contentDescription = "Dying unit",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(alpha = 1 - (flipProgress / 90f))
                        )
                    }
                    // Back of card (flipped)
                    else {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = 1 - disintegrationProgress
                                    rotationY = 180f
                                }
                        ) {
                            drawRect(
                                color = Color(0xFF1A237E),
                                size = size
                            )
                        }
                    }
                }

                // Disintegration particles - only visible after the flip
                if (disintegrationProgress > 0f) {
                    // Create a random number of particles
                    repeat(20) { i ->
                        val particleProgress =
                            (disintegrationProgress * (0.5f + (i.toFloat() / 20f)))
                                .coerceIn(0f, 1f)

                        // Calculate random offsets for each particle
                        val randomAngle = i * 18f + Random.nextFloat() * 30f
                        val distance = 100f * particleProgress
                        val xOffset = cos(Math.toRadians(randomAngle.toDouble())).toFloat() * distance
                        val yOffset = sin(Math.toRadians(randomAngle.toDouble())).toFloat() * distance

                        // Particle size and opacity
                        val particleSize = (8f - 6f * particleProgress).dp
                        val particleOpacity = 1f - particleProgress

                        Box(
                            modifier = Modifier
                                .offset(
                                    x = ((unitSize.value/2) + xOffset).dp,
                                    y = ((unitSize.value/2) + yOffset).dp
                                )
                                .size(particleSize)
                                .alpha(particleOpacity)
                                .background(
                                    color = when (unitType) {
                                        UnitType.INFANTRY -> Color(0xFFB71C1C)
                                        UnitType.CAVALRY -> Color(0xFF1A237E)
                                        UnitType.ARTILLERY -> Color(0xFF3E2723)
                                        UnitType.MISSILE -> Color(0xFF004D40)
                                        UnitType.MUSKET -> Color(0xFF004D40)
                                    },
                                    shape = if (Random.nextBoolean()) CircleShape else RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

// Helper to get unit image resource
private fun getUnitTypeResource(unitType: UnitType): Int {
    return when (unitType) {
        UnitType.INFANTRY -> R.drawable.unit_type_infantry
        UnitType.CAVALRY -> R.drawable.unit_type_cavalry
        UnitType.ARTILLERY -> R.drawable.unit_type_cannon
        UnitType.MISSILE -> R.drawable.unit_type_missile
        UnitType.MUSKET -> R.drawable.unit_type_musket
    }
}