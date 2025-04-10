package com.example.cardgame.ui.components.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
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

    // Different animation styles based on card type
    val (baseColor, animationStyle) = when (cardType) {
        TacticCardType.DIRECT_DAMAGE -> Color(0xFFB71C1C) to "impact" // Dark red impact
        TacticCardType.AREA_EFFECT -> Color(0xFFFF5722) to "explosion" // Orange explosion
        TacticCardType.BUFF -> Color(0xFF4CAF50) to "glow" // Green glow
        TacticCardType.DEBUFF -> Color(0xFF7B1FA2) to "swirl" // Purple swirl
        TacticCardType.SPECIAL -> Color(0xFF1976D2) to "sparkle" // Blue sparkle
    }

    // Animation parameters
    val infiniteTransition = rememberInfiniteTransition()

    // Pulsating alpha for all effects
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Size animation for growing/shrinking effects
    val size by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Rotation for swirling effects
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(targetPosition.first, targetPosition.second)

        when (animationStyle) {
            "impact" -> {
                // Direct damage - impact effect
                // Draw a circle that expands and fades
                drawCircle(
                    color = baseColor,
                    radius = 80f * size,
                    center = center,
                    alpha = alpha
                )

                // Draw lines radiating outward
                for (angle in 0..360 step 45) {
                    val radian = Math.toRadians(angle.toDouble())
                    val endX = center.x + 100f * size * Math.cos(radian).toFloat()
                    val endY = center.y + 100f * size * Math.sin(radian).toFloat()

                    drawLine(
                        color = baseColor,
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 5f,
                        alpha = alpha * 0.7f
                    )
                }
            }
            "explosion" -> {
                // Area effect - explosion
                // Multiple concentric circles
                drawCircle(
                    color = baseColor,
                    radius = 50f * size,
                    center = center,
                    alpha = alpha
                )

                drawCircle(
                    color = baseColor,
                    radius = 100f * size,
                    center = center,
                    alpha = alpha * 0.7f,
                    style = Stroke(width = 5.dp.toPx())
                )

                drawCircle(
                    color = baseColor,
                    radius = 150f * size,
                    center = center,
                    alpha = alpha * 0.4f,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            "glow" -> {
                // Buff effect - gentle glow
                drawCircle(
                    color = baseColor,
                    radius = 70f * size,
                    center = center,
                    alpha = alpha * 0.7f
                )

                drawCircle(
                    color = Color.White,
                    radius = 40f * size,
                    center = center,
                    alpha = alpha * 0.5f
                )
            }
            "swirl" -> {
                // Debuff effect - swirling energy
                // Rotating ellipses
                drawOval(
                    color = baseColor,
                    topLeft = Offset(
                        center.x - 60f * size,
                        center.y - 90f * size
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        120f * size,
                        180f * size
                    ),
                    alpha = alpha * 0.6f,
                    style = Stroke(width = 4.dp.toPx())
                )

                // Rotated 90 degrees
                drawOval(
                    color = baseColor,
                    topLeft = Offset(
                        center.x - 90f * size,
                        center.y - 60f * size
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        180f * size,
                        120f * size
                    ),
                    alpha = alpha * 0.6f,
                    style = Stroke(width = 4.dp.toPx())
                )

                // Center glow
                drawCircle(
                    color = baseColor,
                    radius = 30f * size,
                    center = center,
                    alpha = alpha
                )
            }
            "sparkle" -> {
                // Special effect - sparkling magic
                // Center glow
                drawCircle(
                    color = baseColor,
                    radius = 50f * size,
                    center = center,
                    alpha = alpha * 0.8f
                )

                // Small sparkles around the center
                val sparkleRadius = 120f * size
                for (i in 0 until 8) {
                    val angle = (rotation + i * 45) % 360
                    val radian = Math.toRadians(angle.toDouble())
                    val x = center.x + sparkleRadius * Math.cos(radian).toFloat()
                    val y = center.y + sparkleRadius * Math.sin(radian).toFloat()

                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = Offset(x, y),
                        alpha = alpha
                    )
                }

                // Particles
                for (i in 0 until 20) {
                    val angle = (rotation + i * 18) % 360
                    val radian = Math.toRadians(angle.toDouble())
                    val distance = 30f + (i % 3) * 20f
                    val x = center.x + distance * size * Math.cos(radian).toFloat()
                    val y = center.y + distance * size * Math.sin(radian).toFloat()

                    drawCircle(
                        color = baseColor.copy(alpha = 0.7f),
                        radius = 4f,
                        center = Offset(x, y),
                        alpha = alpha * (0.5f + (i % 5) * 0.1f)
                    )
                }
            }
        }
    }

    // Trigger completion after animation duration
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(1500) // Animation duration
            onAnimationComplete()
        }
    }
}