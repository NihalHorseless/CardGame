package com.example.cardgame.ui.components.board

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cardgame.ui.theme.bloodDropShape

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
        // Blood mana display
        Column(verticalArrangement = Arrangement.Center) {
            BloodManaIndicator(
                currentMana = playerMana,
                maxMana = playerMaxMana
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onEndTurn,
                enabled = isPlayerTurn,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A0404)),
                modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally)
            ) {
                Text("End Turn")
            }
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
        modifier = modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                        color = Color.White.copy(alpha = if(isFilled) 0.5f else 0.2f),
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