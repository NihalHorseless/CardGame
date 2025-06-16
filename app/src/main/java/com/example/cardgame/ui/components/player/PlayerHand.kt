package com.example.cardgame.ui.components.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cardgame.R
import com.example.cardgame.data.enum.FortificationType
import com.example.cardgame.data.enum.TacticCardType
import com.example.cardgame.data.enum.UnitEra
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.ui.components.board.FortificationTypeIcon
import com.example.cardgame.ui.components.board.TacticCardItem
import com.example.cardgame.ui.components.board.TacticTypeIcon
import com.example.cardgame.ui.components.board.UnitTypeIcon
import com.example.cardgame.ui.theme.bloodDropShape
import com.example.cardgame.ui.theme.kiteShieldShape
import com.example.cardgame.ui.theme.libreFont
import com.example.cardgame.ui.theme.slenderSwordShape
import com.example.cardgame.ui.theme.thickSwordShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerHand(
    cards: List<Card>,
    playerMana: Int,
    selectedCardIndex: Int? = null,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // State to track which card's details are being shown
    var showingCardDetails by remember { mutableStateOf<Card?>(null) }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy((-20).dp)
    ) {
        itemsIndexed(cards) { index, card ->
            val isPlayable = card.manaCost <= playerMana
            val isSelected = index == selectedCardIndex

            var isHovered by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isHovered || isSelected) 1.1f else 1f,
                animationSpec = tween(200)
            )
            val elevationDp by animateFloatAsState(
                targetValue = if (isHovered || isSelected) 16f else 4f,
                animationSpec = tween(200)
            )

            Box(
                modifier = Modifier
                    .height(160.dp)
                    .scale(scale)
                    .padding(vertical = 8.dp)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Enter -> isHovered = true
                                    PointerEventType.Exit -> isHovered = false
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                when (card) {
                    is UnitCard -> {
                        HandCard(
                            card = card,
                            isPlayable = isPlayable,
                            isSelected = isSelected,
                            elevation = elevationDp.dp,
                            onClick = { onCardClick(index) },
                            onLongClick = { showingCardDetails = card }
                        )
                    }

                    is TacticCard -> {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .combinedClickable(
                                    onClick = { onCardClick(index) },
                                    onLongClick = { showingCardDetails = card }
                                )
                        ) {
                            TacticCardItem(
                                card = card,
                                isPlayable = isPlayable,
                                isSelected = isSelected,
                                onClick = { onCardClick(index) },
                                onLongClick = { showingCardDetails = card}
                            )
                        }
                    }

                    is FortificationCard -> {
                        HandCard(
                            card = card,
                            isPlayable = isPlayable,
                            isSelected = isSelected,
                            elevation = elevationDp.dp,
                            onClick = { onCardClick(index) },
                            onLongClick = { showingCardDetails = card }
                        )
                    }

                    else -> {
                        GenericCardItem(
                            card = card,
                            isPlayable = isPlayable,
                            isSelected = isSelected,
                            onClick = { onCardClick(index) },
                            onLongClick = { showingCardDetails = card }
                        )
                    }
                }
            }
        }
    }

    // Show card details dialog if a card is long-pressed
    showingCardDetails?.let { card ->
        CardDetailDialog(
            card = card,
            onDismiss = { showingCardDetails = null }
        )
    }
}


/**
 * Individual card in the player's hand
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HandCard(
    card: Card,
    isPlayable: Boolean,
    isSelected: Boolean = false,
    elevation: Dp = 4.dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {} // Add this parameter
) {
    val cardColor = if (card is UnitCard) {
        when (card.unitEra) {
            UnitEra.ANCIENT -> Color(0xFF8D6E63)    // Brown
            UnitEra.ROMAN -> Color(0xFFB71C1C)      // Dark Red
            UnitEra.NAPOLEONIC -> Color(0xFF000091)   // Dark Blue
            else -> Color(0xFFFFFFFF) // White for Default
        }
    } else {
        Color(0xFF4527A0)
    }

    // Dim the card if it's not playable
    val finalCardColor = if (isPlayable) cardColor else cardColor.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .width(100.dp)
            .height(140.dp)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(8.dp)
            )
            // Add a border if selected
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF4CAF50) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick // Add long click support
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Card Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        finalCardColor
                    )
            )

            // Mana Cost (top left)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomCenter)
                    .padding(4.dp)
                    .background(Color(0xFFC41E3A), bloodDropShape)
                    .border(1.dp, Color.White, bloodDropShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.manaCost.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // Card Name
            Text(
                text = card.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = libreFont,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp, start = 8.dp, end = 4.dp)
            )

            // Card Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 30.dp, bottom = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                when (card) {
                    is UnitCard -> UnitTypeIcon(
                        unitType = card.unitType,
                        modifier = Modifier.size(40.dp)
                    )

                    is FortificationCard -> FortificationTypeIcon(
                        fortType = card.fortType,
                        modifier = Modifier.size(40.dp)
                    )

                    is TacticCard -> TacticTypeIcon(
                        tacticCardType = card.cardType,
                        modifier = Modifier.size(40.dp)
                    )

                    else -> Image(
                        painter = painterResource(R.drawable.magic_effect_icon),
                        contentDescription = "Default Card Image",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Stats for Unit Cards
            if (card is UnitCard || card is FortificationCard) {
                val attack: String = when (card) {
                    is UnitCard -> card.attack.toString()
                    is FortificationCard -> card.attack.toString()
                    else -> "Null"
                }
                val health: String = when (card) {
                    is UnitCard -> card.health.toString()
                    is FortificationCard -> card.health.toString()
                    else -> "Null"
                }
                // Attack Value (bottom left)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .background(Color(0xFFFF9800), thickSwordShape)
                        .border(1.dp, Color.White, thickSwordShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = attack,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.offset(y = (-2).dp)
                    )
                }

                // Health Value (bottom right)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Green, kiteShieldShape)
                        .border(1.dp, Color.White, kiteShieldShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = health,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                }
            }

            // Selection indicator
            if (isSelected) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw a glowing effect around the card
                    drawCircle(
                        color = Color(0x334CAF50), // Semi-transparent green
                        radius = 100f,
                        center = center,
                        blendMode = BlendMode.SrcOver
                    )
                }
            }
        }
    }
}

/**
 * Fallback card rendering for unknown card types
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GenericCardItem(
    card: Card,
    isPlayable: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        HandCard(
            card = card,
            isPlayable = isPlayable,
            isSelected = isSelected,
            onClick = onClick,
            onLongClick = onLongClick
        )
    }
}

@Composable
fun OpponentHand(
    cardCount: Int,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        items(cardCount) { index ->
            // Card back display
            CardBack(index = index)
            }
        }
    }

@Composable
fun CardBack(index: Int) {
    // Card back display
    Box(
        modifier = Modifier
            .size(45.dp, 70.dp)
            .offset(x = if (index > 0) (-15 * index).dp else 0.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(2.dp)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF000099),
                        Color(0xFF2962FF)
                    )
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 2.dp,
                color = Color(0xFFFFD700),
                shape = RoundedCornerShape(1.dp)
            )
    ) {
        // Card back pattern
        Image(
            painter = painterResource(id = R.drawable.card_back),
            contentDescription = "Opponent Card Background",
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        )

    }
}
@Composable
fun CardDetailDialog(
    card: Card,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .wrapContentHeight()
                    .clickable { /* Prevent clicks from passing through */ },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (card) {
                        is UnitCard -> when (card.unitEra) {
                            UnitEra.ANCIENT -> Color(0xFF8D6E63)
                            UnitEra.ROMAN -> Color(0xFFB71C1C)
                            UnitEra.MEDIEVAL -> Color(0xFF1A237E)
                            UnitEra.NAPOLEONIC -> Color(0xFF4A148C)
                        }
                        is TacticCard -> when (card.cardType) {
                            TacticCardType.DIRECT_DAMAGE -> Color(0xFFB71C1C)
                            TacticCardType.AREA_EFFECT -> Color(0xFFFF5722)
                            TacticCardType.BUFF -> Color(0xFF4CAF50)
                            TacticCardType.DEBUFF -> Color(0xFF7B1FA2)
                            TacticCardType.SPECIAL -> Color(0xFF1976D2)
                        }
                        is FortificationCard -> Color(0xFF795548)
                        else -> Color(0xFF424242)
                    }.copy(0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Card Name
                    Text(
                        text = card.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = libreFont,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card Icon
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (card) {
                            is UnitCard -> UnitTypeIcon(
                                unitType = card.unitType,
                                modifier = Modifier.size(80.dp)
                            )
                            is FortificationCard -> FortificationTypeIcon(
                                fortType = card.fortType,
                                modifier = Modifier.size(80.dp)
                            )
                            is TacticCard -> TacticTypeIcon(
                                tacticCardType = card.cardType,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card Description
                    Text(
                        text = card.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = libreFont,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Mana Cost
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFFC41E3A), bloodDropShape)
                                .border(2.dp, Color.Gray.copy(0.5f), bloodDropShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = card.manaCost.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        when (card) {
                            is UnitCard -> {
                                // Attack
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color(0xFFFF9800), slenderSwordShape)
                                        .border(2.dp, Color.Gray.copy(0.5f), slenderSwordShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = card.attack.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        modifier = Modifier.offset(y = (-4).dp)
                                    )
                                }

                                // Health
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color(0xFF4CAF50), kiteShieldShape)
                                        .border(2.dp, Color.Gray.copy(0.5f), kiteShieldShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = card.health.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        modifier = Modifier.offset(y = (-2).dp)
                                    )
                                }
                            }
                            is FortificationCard -> {
                                // Attack (if tower)
                                if (card.fortType == FortificationType.TOWER) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(Color(0xFFFF9800), slenderSwordShape)
                                            .border(2.dp, Color.Gray.copy(0.5f), slenderSwordShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = card.attack.toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            modifier = Modifier.offset(y = (-4).dp)
                                        )
                                    }
                                }

                                // Health
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color(0xFF4CAF50), kiteShieldShape)
                                        .border(2.dp, Color.Gray.copy(0.5f), kiteShieldShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = card.health.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        modifier = Modifier.offset(y = (-2).dp)
                                    )
                                }
                            }
                        }
                    }

                    // Special abilities
                    when (card) {
                        is UnitCard -> {
                            if (card.hasCharge || card.hasTaunt || card.abilities.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (card.hasCharge) {
                                        Text(
                                            text = "• Charge: Can attack immediately",
                                            fontSize = 12.sp,
                                            color = Color(0xFFFFC107),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (card.hasTaunt) {
                                        Text(
                                            text = "• Taunt: Enemies must attack this unit",
                                            fontSize = 12.sp,
                                            color = Color(0xFF795548),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap anywhere to close",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}