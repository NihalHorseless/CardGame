package com.example.cardgame.ui.components.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cardgame.R
import com.example.cardgame.data.enum.UnitType
import kotlinx.coroutines.delay

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
fun BoardHighlights(
    validMoveDestinations: List<Pair<Int, Int>>,
    validAttackTargets: List<Pair<Int, Int>>,
    cellPositions: Map<Pair<Int, Int>, Pair<Float, Float>>,
    modifier: Modifier = Modifier
) {
    // Pulsing animation for highlighting
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw movement highlights (blue circles)
            validMoveDestinations.forEach { (row, col) ->
                val position = cellPositions[Pair(row, col)] ?: return@forEach
                val (x, y) = position

                // Draw a hollow blue circle (chess-like move indicator)
                drawCircle(
                    color = Color(0xFF3F51B5), // Blue
                    radius = 20f * pulseSize,
                    center = Offset(x, y),
                    style = Stroke(width = 3.dp.toPx()),
                    alpha = pulseAlpha
                )

                // Add a small dot in the center
                drawCircle(
                    color = Color(0xFF3F51B5), // Blue
                    radius = 5f * pulseSize,
                    center = Offset(x, y),
                    alpha = pulseAlpha
                )
            }

            // Draw attack highlights (red targets)
            validAttackTargets.forEach { (row, col) ->
                val position = cellPositions[Pair(row, col)] ?: return@forEach
                val (x, y) = position
                val size = 25f * pulseSize

                // Draw a target-like indicator for attacks
                // Outer circle
                drawCircle(
                    color = Color(0xFFF44336), // Red
                    radius = size,
                    center = Offset(x, y),
                    style = Stroke(width = 3.dp.toPx()),
                    alpha = pulseAlpha
                )

                // Inner circle
                drawCircle(
                    color = Color(0xFFF44336), // Red
                    radius = size * 0.6f,
                    center = Offset(x, y),
                    style = Stroke(width = 2.dp.toPx()),
                    alpha = pulseAlpha
                )

                // Center dot
                drawCircle(
                    color = Color(0xFFF44336), // Red
                    radius = 4f * pulseSize,
                    center = Offset(x, y),
                    alpha = pulseAlpha
                )
            }
        }
    }
}