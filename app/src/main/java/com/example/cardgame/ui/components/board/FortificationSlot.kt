package com.example.cardgame.ui.components.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.cardgame.R
import com.example.cardgame.data.enum.FortificationType
import com.example.cardgame.data.model.card.FortificationCard

@Composable
fun FortificationSlot(
    fortification: FortificationCard,
    isSelected: Boolean,
    isPlayerFortification: Boolean,
    canAttack: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine background color based on fortification type
    val backgroundColor = when (fortification.fortType) {
        FortificationType.WALL -> Color(0xFF8D6E63) // Brown for walls
        FortificationType.TOWER -> Color(0xFF5D4037) // Darker brown for towers
    }

    // Border color based on ownership
    val borderColor = if (isPlayerFortification) Color(0xFF4CAF50) else Color(0xFFE57373)

    // Hexagonal shape for fortifications to distinguish them from units
    val hexagonalShape = GenericShape { size, _ ->
        val center = size.width / 2
        val radius = size.width / 2
        val height = size.height

        // Draw a hexagon
        moveTo(center, 0f)
        lineTo(center + radius, height * 0.25f)
        lineTo(center + radius, height * 0.75f)
        lineTo(center, height)
        lineTo(center - radius, height * 0.75f)
        lineTo(center - radius, height * 0.25f)
        close()
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = hexagonalShape
            )
            .clip(hexagonalShape)
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.Yellow else borderColor,
                shape = hexagonalShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Fortification icon
        FortificationTypeIcon(
            fortType = fortification.fortType,
            modifier = Modifier.size(40.dp)
        )

        // Attack value for towers (only show if it has attack)
        if (fortification.fortType == FortificationType.TOWER) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomStart)
                    .offset((-6).dp, (6).dp)
                    .zIndex(1f)
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xFFFF9800), CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fortification.attack.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Health value
        val healthColor = when {
            fortification.health <= fortification.maxHealth / 3 -> Color.Red
            fortification.health <= fortification.maxHealth * 2 / 3 -> Color(0xFFFFA500) // Orange
            else -> Color.Green
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.BottomEnd)
                .offset((6).dp, (6).dp)
                .zIndex(1f)
                .shadow(4.dp, CircleShape)
                .background(healthColor, CircleShape)
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fortification.health.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        // Attack indicator for towers that can attack
        if (fortification.fortType == FortificationType.TOWER && canAttack) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .size(12.dp)
                    .background(Color.Red, CircleShape)
                    .border(0.5.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun FortificationTypeIcon(
    fortType: FortificationType,
    modifier: Modifier = Modifier
) {
    val imageRes: Int = when (fortType) {
        FortificationType.WALL -> R.drawable.fortification_wall // Create these drawables
        FortificationType.TOWER -> R.drawable.fortification_tower
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = fortType.name,
        modifier = modifier
    )
}
