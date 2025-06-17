package com.example.cardgame.ui.components.board

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.ui.screens.getOpponentPortrait
import com.example.cardgame.ui.theme.libreFont
import kotlinx.coroutines.delay

/**
 * Player portrait composable with health display and attack target functionality
 */
@Composable
fun PlayerPortrait(
    playerName: String,
    health: Int,
    maxHealth: Int,
    visualHealth: Int? = null,
    isCurrentPlayer: Boolean,
    isTargetable: Boolean,
    onPortraitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use visualHealth for display if available, otherwise use actual health
    val displayHealth = visualHealth ?: health
    // Animation for when portrait is targetable
    val targetablePulse = rememberInfiniteTransition()
    val borderWidth by targetablePulse.animateFloat(
        initialValue = 2f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val borderColor = when {
        isTargetable -> Color(0xFFFFD700) // Gold for targetable
        isCurrentPlayer -> Color(0xFF77DD77) // Green for current player
        else -> Color(0xFFFF6961) // Red for opponent
    }

    val healthColor = when {
        health <= maxHealth / 4 -> Color(0xFFFF6961) // Red when low health
        health <= maxHealth / 2 -> Color(0xFFFFB347) // Orange when medium health
        else -> Color(0xFF77DD77) // Green when high health
    }

    // Shake effect when taking damage
    val isBeingDamaged = visualHealth != null && visualHealth < health
    var offsetX by remember { mutableFloatStateOf(0f) }

    // Apply a shake effect when taking damage
    LaunchedEffect(isBeingDamaged) {
        if (isBeingDamaged) {
            // Create a shake pattern
            val pattern = listOf(3f, -5f, 4f, -3f, 2f, -1f, 0f)
            for (offset in pattern) {
                offsetX = offset
                delay(45)
            }
            offsetX = 0f
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        // Portrait frame
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape
                )
                .border(
                    width = if (isTargetable) borderWidth.dp else 2.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298)),
                        radius = 150f
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable(enabled = isTargetable) { onPortraitClick() },
            contentAlignment = Alignment.Center
        ) {
            if(playerName == "Mediocre Bot") {
                // You would use an actual image here for player avatars
                // For now we'll use a placeholder with the player's initial
                Text(
                    text = playerName.first().toString(),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            else {
                Image(
                    painter = painterResource(id = getOpponentPortrait(playerName)),
                    contentDescription = playerName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // Health display at bottom of portrait
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0x80000000)),
                contentAlignment = Alignment.Center
            ) {
                // Animate text scale when health changes
                val textScale by animateFloatAsState(
                    targetValue = if (isBeingDamaged) 1.3f else 1.0f,
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = FastOutSlowInEasing
                    )
                )
                Text(
                    text = "$displayHealth",
                    color = healthColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.scale(textScale)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Player name
        Text(
            text = playerName,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = libreFont,
            textAlign = TextAlign.Center
        )
    }
}