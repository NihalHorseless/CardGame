package com.example.cardgame.ui.components.board

import android.graphics.RectF
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.cardgame.R
import com.example.cardgame.data.enum.UnitEra
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.card.UnitCard

@Composable
fun UnitSlot(
    unit: UnitCard?,
    isSelected: Boolean,
    isPlayerUnit: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.Yellow
            isPlayerUnit && unit?.canAttackThisTurn == true -> Color.Green
            else -> Color.Gray
        }
    )

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

    // Card color based on era
    val cardColor = when (unit?.unitEra) {
        UnitEra.ANCIENT -> Color(0xFF8D6E63)    // Brown
        UnitEra.ROMAN -> Color(0xFFB71C1C)      // Dark Red
        UnitEra.MEDIEVAL -> Color(0xFF1A237E)   // Dark Blue
        UnitEra.MODERN -> Color(0xFF212121)     // Dark Gray
        else -> Color(0xFF424242) // Gray for empty slot
    }

    // This outer box contains both the card and the indicators
    Box(
        modifier = modifier
            .size(65.dp, 80.dp)
            .scale(scale)
    ) {
        // The main card with oval shape
        Card(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isSelected) 12.dp else 4.dp,
                    shape = GenericShape { size, _ ->
                        addOval(Rect(0f, 0f, size.width, size.height))
                    }
                )
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = GenericShape { size, _ ->
                        addOval(Rect(0f, 0f, size.width, size.height))
                    }
                )
                .clickable(enabled = unit != null) { onClick() },
            shape = GenericShape { size, _ ->
                addOval(Rect(0f, 0f, size.width, size.height))
            }, colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            if (unit != null) {
                // Card with unit
                Box(modifier = Modifier.fillMaxSize()) {
                    // Unit Type Icon
                    UnitTypeIcon(
                        unitType = unit.unitType,
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                    )
                }
            } else {
                // Empty slot
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    // Empty slot indicator (optional)
                }
            }
        }

        // Only add the stat indicators if there's a unit in the slot
        if (unit != null) {
            // Attack Value - positioned outside the oval at bottom left
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.BottomStart)
                    .offset((-6).dp, (6).dp)
                    .zIndex(1f)
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xFFFF9800), CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.attack.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Health Value - positioned outside the oval at bottom right
            val healthColor = when {
                unit.health <= unit.maxHealth / 3 -> Color.Red
                unit.health <= unit.maxHealth * 2 / 3 -> Color(0xFFFFA500) // Orange
                else -> Color.Green
            }

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.BottomEnd)
                    .offset((6).dp, (6).dp)
                    .zIndex(1f)
                    .shadow(4.dp, CircleShape)
                    .background(healthColor, CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.health.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            // Taunt indicator
            if (unit.hasTaunt) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(18.dp)
                        .background(Color(0xFF795548), CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_shield_24),
                        contentDescription = "Taunt",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Display an icon based on unit type
 */
@Composable
fun UnitTypeIcon(
    unitType: UnitType,
    modifier: Modifier = Modifier
) {
    val imageRes: Int = when (unitType) {
        UnitType.INFANTRY -> R.drawable.unit_type_infantry
        UnitType.CAVALRY -> R.drawable.unit_type_cavalry
        UnitType.ARTILLERY -> R.drawable.unit_type_cannon
        UnitType.MISSILE -> R.drawable.unit_type_missile
    }

    Image(
        painter = painterResource(imageRes),
        contentDescription = unitType.name,
        modifier = modifier
    )
}