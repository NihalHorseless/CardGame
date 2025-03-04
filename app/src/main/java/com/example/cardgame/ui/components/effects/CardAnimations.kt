package com.example.cardgame.ui.components.effects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.R
import com.example.cardgame.data.enum.UnitType
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// Card Play Animation
@Composable
fun CardPlayAnimation(
    isVisible: Boolean,
    onAnimationComplete: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { 300 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -300 }) + fadeOut()
    ) {
        val rotation = remember { Animatable(0f) }

        LaunchedEffect(isVisible) {
            if (isVisible) {
                rotation.animateTo(
                    targetValue = 720f,
                    animationSpec = tween(800, easing = LinearEasing)
                )
                onAnimationComplete()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation.value),
            contentAlignment = Alignment.Center
        ) {
            // Glowing effect
            Canvas(modifier = Modifier.size(120.dp)) {
                drawCircle(
                    color = Color(0xFF00AAFF),
                    radius = 100f,
                    alpha = 0.6f
                )
            }
        }
    }
}
@Composable
fun AttackAnimation(
    isAttacking: Boolean,
    attackerX: Float,
    attackerY: Float,
    targetX: Float,
    targetY: Float,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (isAttacking) 1f else 0f,
        animationSpec = tween(500),
        finishedListener = { if (it == 1f) onAnimationComplete() }
    )

    progress = animatedProgress

    LaunchedEffect(isAttacking) {
        if (isAttacking) {
            delay(500) // Animation duration
            onAnimationComplete()
        }
    }

    if (isAttacking) {
        Canvas(modifier = modifier) {
            val startX = attackerX
            val startY = attackerY
            val endX = targetX
            val endY = targetY

            // Draw attack path as a curved line
            val path = Path()
            path.moveTo(startX, startY)

            // Create curved attack path with a control point
            val controlX = (startX + endX) / 2
            val controlY = (startY + endY) / 2 - 50f

            path.quadraticTo(controlX, controlY, endX, endY)

            // Draw the path with progress animation
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(Color.Yellow, Color.Red),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY)
                ),
                style = Stroke(
                    width = 5f,
                    cap = StrokeCap.Round
                ),
                alpha = progress * 0.8f
            )

            // Draw attack impact at target
            if (progress > 0.8f) {
                drawCircle(
                    color = Color.Red,
                    radius = 40f * ((progress - 0.8f) * 5f),
                    center = Offset(endX, endY),
                    alpha = (1f - (progress - 0.8f) * 5f)
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
                        when {
                            event.type == PointerEventType.Enter -> isHovered = true
                            event.type == PointerEventType.Exit -> isHovered = false
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
fun SimpleAttackAnimation(
    isVisible: Boolean,
    unitType: UnitType,
    targetX: Float,
    targetY: Float,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get appropriate weapon icon based on unit type
    val weaponIconRes = when (unitType) {
        UnitType.INFANTRY -> R.drawable.attack_infantry
        UnitType.CAVALRY -> R.drawable.attack_cavalry
        UnitType.ARTILLERY -> R.drawable.attack_artillery
        UnitType.MISSILE -> R.drawable.attack_missile
    }

    // Animation states
    val animatedScale by animateFloatAsState(
        targetValue = if (isVisible) 1.5f else 0f,
        animationSpec = tween(300, easing = EaseOutBack)
    )

    val animatedRotation by animateFloatAsState(
        targetValue = if (isVisible) 360f else 0f,
        animationSpec = tween(500)
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300)
    )

    // Trigger completion after animation plays
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(800) // Show the weapon for this duration
            onAnimationComplete()
        }
    }

    // Generate small random offsets for shaking effect
    var shakeOffsetX by remember { mutableStateOf(0f) }
    var shakeOffsetY by remember { mutableStateOf(0f) }

    // Update shake effect
    LaunchedEffect(isVisible) {
        if (isVisible) {
            while (true) {
                shakeOffsetX = (-5..5).random().toFloat()
                shakeOffsetY = (-5..5).random().toFloat()
                delay(50) // Update shake every 50ms
            }
        }
    }

    if (isVisible) {
        Box(modifier = modifier) {
            // We use IntOffset for pixel-perfect positioning
            val iconSize = 40.dp
            val halfIconSizePx = with(LocalDensity.current) { iconSize.toPx() / 2 }

            Image(
                painter = painterResource(id = weaponIconRes),
                contentDescription = "Attack Animation",
                modifier = Modifier
                    .size(iconSize)
                    .offset {
                        IntOffset(
                            x = (targetX - halfIconSizePx + shakeOffsetX).roundToInt(),
                            y = (targetY - halfIconSizePx + shakeOffsetY).roundToInt()
                        )
                    }
                    .scale(animatedScale)
                    .rotate(animatedRotation)
                    .alpha(animatedAlpha)
            )
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
    var yOffset by remember { mutableStateOf(0f) }
    val animatedYOffset by animateFloatAsState(
        targetValue = if (isVisible) -50f else 0f,
        animationSpec = tween(800),
        finishedListener = { if (it == -50f) onAnimationComplete() }
    )

    val textColor = if (isHealing) Color.Green else Color.Red
    val prefix = if (isHealing) "+" else "-"

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
                .offset(x = x.dp, y = (y + yOffset).dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$prefix$damage",
                color = textColor,
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

// Mana Crystal Animation
@Composable
fun ManaAnimation(
    currentMana: Int,
    maxMana: Int
) {
    Row(
        modifier = Modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(maxMana) { index ->
            val isFilled = index < currentMana

            val transition = updateTransition(targetState = isFilled, label = "ManaTransition")

            val color by transition.animateColor(
                transitionSpec = { tween(300) },
                label = "ColorAnim"
            ) { filled ->
                if (filled) Color(0xFF00AAFF) else Color(0xFF333333)
            }

            val size by transition.animateDp(
                transitionSpec = { tween(300) },
                label = "SizeAnim"
            ) { filled ->
                if (filled) 24.dp else 20.dp
            }

            Box(
                modifier = Modifier
                    .size(size)
                    .background(color, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}