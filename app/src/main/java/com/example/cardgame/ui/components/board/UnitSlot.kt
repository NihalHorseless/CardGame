package com.example.cardgame.ui.components.board

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.data.model.card.UnitCard

@Composable
fun UnitSlot(
    unit: UnitCard?,
    isSelected: Boolean,
    isPlayerUnit: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> Color.Yellow
        isPlayerUnit && unit?.canAttackThisTurn == true -> Color.Green
        else -> Color.Gray
    }

    val borderWidth = if (isSelected) 3.dp else 1.dp

    // Pulsating animation for cards that can attack
    val pulseMagnitude by animateFloatAsState(
        targetValue = if (isPlayerUnit && unit?.canAttackThisTurn == true && !isSelected) 1.1f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Selection animation
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else if (isPlayerUnit && unit?.canAttackThisTurn == true) pulseMagnitude else 1.0f,
        animationSpec = tween(200)
    )

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(100.dp)
            .scale(scale)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (unit == null) Color(0x33FFFFFF) else Color(0xFF424242),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = unit != null) { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (unit != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = unit.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Attack value
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Red, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unit.attack.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Health value with color animation based on damage
                    val healthColor = when {
                        unit.health < unit.maxHealth / 3 -> Color.Red
                        unit.health < unit.maxHealth * 2 / 3 -> Color(0xFFFFA500) // Orange
                        else -> Color.Green
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(healthColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unit.health.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Show ability indicators
                if (unit.abilities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        unit.abilities.forEach { ability ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(2.dp)
                                    .background(Color.Yellow, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}