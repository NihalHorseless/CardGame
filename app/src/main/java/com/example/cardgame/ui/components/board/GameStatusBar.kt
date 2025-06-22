package com.example.cardgame.ui.components.board

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cardgame.R
import com.example.cardgame.ui.theme.bloodDropShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameStatusBar(
    playerMana: Int,
    playerMaxMana: Int,
    isPlayerTurn: Boolean,
    onEndTurn: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track current rotation state - persists between compositions
        var isTurnChangeInProgress by remember { mutableStateOf(false) }
        var currentRotation by remember { mutableFloatStateOf(0f) }

        // Animate to target rotation when it changes
        val rotation by animateFloatAsState(
            targetValue = currentRotation,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )



        // Reset rotation when turn changes
        LaunchedEffect(isPlayerTurn) {
            // When it becomes player's turn again, reset to 0 degrees
            if (isPlayerTurn) {
                currentRotation = 0f
                isTurnChangeInProgress = false
            }
        }

        // Remember the coroutine scope tied to this composable
        val scope = rememberCoroutineScope()

        // Blood mana display
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
            Button(
                onClick = {
                    // Only allow clicking if no turn change is in progress
                    if (!isTurnChangeInProgress && isPlayerTurn) {
                        isTurnChangeInProgress = true

                        // Set rotation to 180 degrees when clicked
                        currentRotation = 180f

                        // Delay the actual turn end slightly to show animation
                        scope.launch {
                            delay(400) // Half the animation time
                            onEndTurn()
                        }
                    }
                },
                enabled = isPlayerTurn && !isTurnChangeInProgress,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5BF11),
                    disabledContainerColor = Color(0xFF315A74)
                ),
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp)
                    .align(Alignment.CenterHorizontally)
                    .border(width = 2.dp,
                        color = Color(0xFF969696),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.hourglass_mountain),
                    contentDescription = "End Turn",
                    tint = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer(scaleX = 1.2f, scaleY = 1.2f)
                        .rotate(rotation)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            BloodManaIndicator(
                currentMana = playerMana,
                maxMana = playerMaxMana
            )
        }

    }
}

@Composable
fun BloodManaIndicator(
    currentMana: Int,
    maxMana: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(maxMana) { index ->
            val isFilled = index < currentMana

            val transition = updateTransition(targetState = isFilled, label = "BloodManaTransition")

            val color by transition.animateColor(
                transitionSpec = { tween(300) },
                label = "ColorAnim"
            ) { filled ->
                if (filled) Color(0xFFC41E3A) else Color(0xFF4A0404)
                // Crimson when filled, dark red when empty
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
                    .shadow(
                        elevation = if (isFilled) 4.dp else 1.dp,
                        shape = bloodDropShape
                    )
                    .background(color, bloodDropShape)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (isFilled) 0.5f else 0.2f),
                        shape = bloodDropShape
                    )
            ) {
                // Optional blood splatter effect for filled drops
                if (isFilled) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw tiny splatter dots around the main drop
                        val splatterColor = Color(0xFFAC0000)
                        for (i in 0..2) {
                            val x = (Math.random() * size.toPx()).toFloat()
                            val y = (Math.random() * size.toPx() * 0.7f).toFloat()
                            val radius = (1 + Math.random() * 2).toFloat()
                            drawCircle(
                                color = splatterColor,
                                radius = radius,
                                center = Offset(x, y),
                                alpha = 0.7f
                            )
                        }
                    }
                }
            }
        }
    }
}