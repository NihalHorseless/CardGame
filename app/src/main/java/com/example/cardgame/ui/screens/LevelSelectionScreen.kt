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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.cardgame.R
import com.example.cardgame.data.model.campaign.Campaign
import com.example.cardgame.data.model.campaign.CampaignLevel
import com.example.cardgame.data.model.campaign.Difficulty
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.ui.theme.libreFont

@Composable
fun LevelSelectionScreen(
    campaign: Campaign,
    playerDecks: List<String>,
    playerDeckNames: List<String>,
    selectedDeck: String?,
    onDeckSelected: (String) -> Unit,
    onLevelScroll: () -> Unit,
    onLevelSelected: (CampaignLevel) -> Unit,
    onBackPressed: () -> Unit,
    onInitial: () -> Unit,
    onLeaveGame: () -> Unit,
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
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        onInitial()
    }

    // Stop and Resume Track
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onInitial()
            } else if (event == Lifecycle.Event.ON_STOP) {
                onLeaveGame()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // Main layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F2233),
                        Color(0xFF2D3250)
                    )
                )
            )
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.campaign_menu_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = modifier
                .fillMaxSize()

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
                                    Color(0xFF946125) else Color(0xFFAA7A39),
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
                                    Color(0xFF946125) else Color(0xFFAA7A39),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Deck selection at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Transparent,
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
                    itemsIndexed(playerDecks) { index, originalDeckName ->
                        val formattedDeckName = playerDeckNames.getOrNull(index) ?: "Default Deck Name" // Handle potential index out of bounds

                        DeckSelectionItem(
                            deckName = formattedDeckName,
                            isSelected = originalDeckName == selectedDeck, // Still compare with the original for selection
                            onClick = { onDeckSelected(originalDeckName) } // Pass the original for selection logic
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
                    containerColor = Color.Transparent
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
                        else -> Color(0xFFB07F3F)
                    },
                    shape = CircleShape
                )
                .border(
                    width = 3.dp,
                    color = when {
                        isLocked -> Color(0xFF757575)
                        else -> Color(0xFF814F16)
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
                        .clip(CircleShape)
                        .alpha(0.8f),
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
                    .background(Color(0xFF558B2F), CircleShape)
                    .border(2.dp, Color(0xFF814F16), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.french_medal),
                    contentDescription = "Level Completed",
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(0.9f)
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
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = level.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = libreFont,
            color = Color.White.copy(0.9f)
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
                color = Color.White.copy(alpha = 0.8f)
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
                    tint = Color(0xFFFFD700).copy(0.8f),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Reward: ${level.reward}",
                    fontSize = 14.sp,
                    color = Color(0xFFFFD700).copy(0.8f)
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
    val stars = when (difficulty) {
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
                tint = Color(0xFFFFC107).copy(0.8f),
                modifier = Modifier.size(20.dp)
            )
        }

        repeat(4 - stars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFF804D15),
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
                if (isSelected) Color(0xFF8A571D) else Color(0xFFA77738)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF6A3C0C),
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
                painter = painterResource(R.drawable.deck_icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
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