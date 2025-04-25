package com.example.cardgame.ui.components.board

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Player portrait composable with health display and attack target functionality
 */
@Composable
fun PlayerPortrait(
    playerName: String,
    health: Int,
    maxHealth: Int,
    isCurrentPlayer: Boolean,
    isTargetable: Boolean,
    onPortraitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            if(playerName == "Opponent") {
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
                Text(
                    text = "$health",
                    color = healthColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Player name
        Text(
            text = playerName,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Portrait with more hero-specific details, useful for games with hero powers
 */
@Composable
fun HeroPortrait(
    playerName: String,
    heroClass: String,
    health: Int,
    maxHealth: Int,
    isCurrentPlayer: Boolean,
    isTargetable: Boolean,
    onPortraitClick: () -> Unit,
    onHeroPowerClick: () -> Unit,
    heroPowerEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        // Main portrait (reusing the basic PlayerPortrait)
        PlayerPortrait(
            playerName = playerName,
            health = health,
            maxHealth = maxHealth,
            isCurrentPlayer = isCurrentPlayer,
            isTargetable = isTargetable,
            onPortraitClick = onPortraitClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Hero power button
        Box(
            modifier = Modifier
                .size(50.dp)
                .shadow(4.dp, CircleShape)
                .background(
                    if (heroPowerEnabled) Color(0xFF5271FF) else Color(0xFF555555),
                    CircleShape
                )
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .clickable(enabled = heroPowerEnabled) { onHeroPowerClick() },
            contentAlignment = Alignment.Center
        ) {
            // Hero power icon placeholder
            Text(
                text = "HP",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Hero class
        Text(
            text = heroClass,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}