package com.example.cardgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.R
import com.example.cardgame.data.model.campaign.Campaign
import com.example.cardgame.data.model.campaign.CampaignLevel
import com.example.cardgame.data.model.campaign.Difficulty
import com.example.cardgame.ui.theme.libreFont

@Composable
fun LevelSelectionScreen(
    campaign: Campaign,
    playerDecks: List<String>,
    selectedDeck: String?,
    onDeckSelected: (String) -> Unit,
    onLevelScroll: () -> Unit,
    onLevelSelected: (CampaignLevel) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentLevelIndex by remember { mutableIntStateOf(0) }
    val levels = campaign.levels
    val currentLevel = levels.getOrNull(currentLevelIndex) ?: return

    // Determine if current level is locked
    val isLevelLocked = if (currentLevelIndex == 0) {
        false // First level is always unlocked
    } else {
        // A level is locked if the previous level is not completed
        val previousLevel = levels.getOrNull(currentLevelIndex - 1)
        previousLevel?.isCompleted == false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F2233),
                        Color(0xFF2D3250)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with campaign name and back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = campaign.name.uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = libreFont,
                color = Color.White
            )

            // Empty box for alignment
            Box(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main content: opponent portrait and level navigation
        Box(
            modifier = Modifier.weight(1f)
        ) {
            // Level content with portrait
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Opponent portrait
                OpponentPortrait(
                    opponentName = currentLevel.opponentName,
                    isCompleted = currentLevel.isCompleted,
                    isLocked = isLevelLocked,
                    modifier = Modifier.size(180.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Level details
                LevelDetailsCard(
                    level = currentLevel,
                    isLocked = isLevelLocked
                )
            }

            // Left/Right navigation arrows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .offset(y = (-72).dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left arrow
                IconButton(
                    onClick = {
                        if (currentLevelIndex > 0) {
                            currentLevelIndex--
                            onLevelScroll()
                        }
                    },
                    enabled = currentLevelIndex > 0,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (currentLevelIndex > 0)
                                Color(0xFF5271FF) else Color(0xFF2D3250),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Level",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Right arrow
                IconButton(
                    onClick = {
                        if (currentLevelIndex < levels.size - 1) {
                            currentLevelIndex++
                            onLevelScroll()
                        }
                    },
                    enabled = currentLevelIndex < levels.size - 1,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (currentLevelIndex < levels.size - 1)
                                Color(0xFF5271FF) else Color(0xFF2D3250),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Level",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Deck selection at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF1A1C2A),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = "Select Your Deck",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(playerDecks) { deckName ->
                    DeckSelectionItem(
                        deckName = deckName,
                        isSelected = deckName == selectedDeck,
                        onClick = { onDeckSelected(deckName) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start battle button
        Button(
            onClick = { onLevelSelected(currentLevel) },
            enabled = !isLevelLocked && selectedDeck != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isLevelLocked) Color(0xFF5271FF) else Color(0xFF4E4E4E)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isLevelLocked) "LEVEL LOCKED" else "START BATTLE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OpponentPortrait(
    opponentName: String,
    isCompleted: Boolean,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    // Container for the entire portrait including badge
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Portrait circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompleted) 6.dp else 0.dp) // Add padding for the badge
                .background(
                    color = when {
                        isLocked -> Color(0xFF4E4E4E)
                        isCompleted -> Color(0xFF3E8C50)
                        else -> Color(0xFF343861)
                    },
                    shape = CircleShape
                )
                .border(
                    width = 3.dp,
                    color = when {
                        isLocked -> Color(0xFF757575)
                        isCompleted -> Color(0xFF4CAF50)
                        else -> Color(0xFF5271FF)
                    },
                    shape = CircleShape
                )
                .padding(4.dp)
        ) {
            // Get portrait image based on opponent name
            val portraitRes = getOpponentPortrait(opponentName)

            if (isLocked) {
                // Show locked overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0x88000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // Show opponent image
                Image(
                    painter = painterResource(id = portraitRes),
                    contentDescription = opponentName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Victory badge - positioned as an overlay outside the main circle
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFF4CAF50), CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun LevelDetailsCard(
    level: CampaignLevel,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1A1C2A),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = when {
                    level.isCompleted -> Color(0xFF4CAF50)
                    isLocked -> Color(0xFF757575)
                    else -> Color(0xFF5271FF)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = level.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = libreFont,
            color = Color.White
        )


        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isLocked) "Complete the previous level to unlock" else level.description,
            fontSize = 14.sp,
            fontFamily = libreFont,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Difficulty indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = "Difficulty: ",
                fontSize = 14.sp,
                color = Color.White
            )

            DifficultyStars(difficulty = level.difficulty)
        }

        if (level.reward != null && !isLocked) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Reward: ${level.reward}",
                    fontSize = 14.sp,
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}

@Composable
fun DifficultyStars(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val stars = when(difficulty) {
        Difficulty.EASY -> 1
        Difficulty.MEDIUM -> 2
        Difficulty.HARD -> 3
        Difficulty.LEGENDARY -> 4
    }

    Row(modifier = modifier) {
        repeat(stars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(20.dp)
            )
        }

        repeat(4 - stars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFF757575),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
@Composable
fun DeckSelectionItem(
    deckName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(120.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFF5271FF) else Color(0xFF343861)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color.White else Color(0xFF3D4160),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Deck icon
            Image(
                painter = painterResource(R.drawable.eagle_standard),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Deck name
            Text(
                text = deckName,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )

            // Selection indicator
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

// Helper function to get opponent portrait resource ID
fun getOpponentPortrait(opponentName: String): Int {
    return when (opponentName) {
        "Marshal Ponitowski" -> R.drawable.ponitowski_avatar
        "Marshal Davout" -> R.drawable.davout_avatar_two
        "Marshal Ney" -> R.drawable.ney_portrait
        "Marshal Marmont" -> R.drawable.marmont_avatar
        "Napoleon Bonaparte" -> R.drawable.napoleon_avatar
        "Player " -> R.drawable.player_avatar
        else -> R.drawable.player_avatar // Fallback
    }
}