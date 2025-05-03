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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.cardgame.R
import com.example.cardgame.data.enum.FortificationType
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.ui.theme.kiteShieldShape
import com.example.cardgame.ui.theme.thickSwordShape

@Composable
fun FortificationSlot(
    fortification: FortificationCard,
    isSelected: Boolean,
    isPlayerFortification: Boolean,
    canAttack: Boolean = false,
    visualHealth: Int? = null,
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
    val displayHealth = visualHealth ?: fortification.health

    Box(
        modifier = modifier
            .size(65.dp,80.dp) // Adjust size to fit the shield shape
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main fortification with shield shape
        Box(
            modifier = Modifier
                .size(65.dp,80.dp)
                .shadow(
                    elevation = if (isSelected) 8.dp else 2.dp,
                    shape = RectangleShape
                )
                .clip(RectangleShape)
                .background(backgroundColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color.Yellow else borderColor,
                    shape = RectangleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            // Fortification icon
            FortificationTypeIcon(
                fortType = fortification.fortType,
                modifier = Modifier.size(40.dp)
            )

        }

        // Attack value for towers (outside the shield)
        if (fortification.fortType == FortificationType.TOWER) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomStart)
                    .offset((-8).dp, (-6).dp)
                    .zIndex(1f)
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xFFFF9800), thickSwordShape)
                    .border(1.dp, Color.White, thickSwordShape),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = fortification.attack.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = ((-3).dp))
                )
            }
        }

        // Health value (outside the shield)
        val healthColor = when {
            displayHealth <= fortification.maxHealth / 3 -> Color.Red
            displayHealth <= fortification.maxHealth * 2 / 3 -> Color(0xFFFFA500) // Orange
            else -> Color.Green
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.BottomEnd)
                .offset((8).dp, (-6).dp)
                .zIndex(1f)
                .shadow(4.dp, kiteShieldShape)
                .background(healthColor, kiteShieldShape)
                .border(1.dp, Color.White, kiteShieldShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayHealth.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-2).dp)
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
