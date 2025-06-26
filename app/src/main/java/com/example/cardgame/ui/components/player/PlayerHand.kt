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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
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
import com.example.cardgame.data.enum.UnitEra
import com.example.cardgame.data.enum.UnitType
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.ui.components.board.FortificationTypeIcon
import com.example.cardgame.ui.components.board.TacticCardItem
import com.example.cardgame.ui.components.board.TacticTypeIcon
import com.example.cardgame.ui.components.board.UnitTypeIcon
import com.example.cardgame.ui.theme.TurkishRed
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
        horizontalArrangement = Arrangement.spacedBy(5.dp)
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
                                onLongClick = { showingCardDetails = card }
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
                shape = RoundedCornerShape(4.dp)
            )
            // Add a border if selected
            .border(
                width = if (isSelected) 3.dp else 2.dp,
                color = if (isSelected) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                shape = RoundedCornerShape(4.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick // Add long click support
            ),
        shape = RoundedCornerShape(4.dp)
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
                    .width(280.dp)
                    .wrapContentHeight()
                    .clickable { },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2C1810),
                                    Color(0xFF1A0F08)
                                )
                            )
                        )
                        .border(
                            width = 4.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFD4AF37),
                                    Color(0xFF8B6914)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Card Name with ornate styling
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF1A0F08),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFD4AF37),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            Text(
                                text = card.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37),
                                fontFamily = libreFont,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        ManaCost(manaOfCard = card.manaCost)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Card Icon with background
                        Box(
                            modifier = Modifier
                                .size(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (card) {
                                is UnitCard -> UnitTypeIcon(
                                    unitType = card.unitType,
                                    modifier = Modifier.size(90.dp)
                                )

                                is FortificationCard -> FortificationTypeIcon(
                                    fortType = card.fortType,
                                    modifier = Modifier.size(90.dp)
                                )

                                is TacticCard -> TacticTypeIcon(
                                    tacticCardType = card.cardType,
                                    modifier = Modifier.size(100.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Card Description in a styled box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF0D0604).copy(alpha = 0.7f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF8B6914),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = card.description,
                                fontSize = 14.sp,
                                color = Color(0xFFE8D5B7),
                                fontFamily = libreFont,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {


                            when (card) {
                                is UnitCard -> {
                                    // Attack
                                    StatDisplay(
                                        icon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(45.dp)
                                                    .background(
                                                        Color(0xFFFF9800),
                                                        slenderSwordShape
                                                    )
                                                    .border(
                                                        2.dp,
                                                        Color(0xFFD4AF37),
                                                        slenderSwordShape
                                                    ),
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
                                        },
                                        label = "Attack"
                                    )

                                    // Health
                                    StatDisplay(
                                        icon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(45.dp)
                                                    .background(Color(0xFF4CAF50), kiteShieldShape)
                                                    .border(
                                                        2.dp,
                                                        Color(0xFFD4AF37),
                                                        kiteShieldShape
                                                    ),
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
                                        },
                                        label = "Health"
                                    )
                                }

                                is FortificationCard -> {
                                    // Attack (if tower)
                                    if (card.fortType == FortificationType.TOWER) {
                                        StatDisplay(
                                            icon = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(50.dp)
                                                        .background(
                                                            Color(0xFFFF9800),
                                                            slenderSwordShape
                                                        )
                                                        .border(
                                                            2.dp,
                                                            Color(0xFFD4AF37),
                                                            slenderSwordShape
                                                        ),
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
                                            },
                                            label = "Attack"
                                        )
                                    }

                                    // Health
                                    StatDisplay(
                                        icon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .background(Color(0xFF4CAF50), kiteShieldShape)
                                                    .border(
                                                        2.dp,
                                                        Color(0xFFD4AF37),
                                                        kiteShieldShape
                                                    ),
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
                                        },
                                        label = "Health"
                                    )
                                }
                            }
                        }

                        // Special abilities
                        when (card) {
                            is UnitCard -> {
                                Spacer(modifier = Modifier.height(20.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color(0xFF0D0604).copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFF8B6914),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.player_hand_special_abilities),
                                            fontSize = 14.sp,
                                            color = Color(0xFFD4AF37),
                                            fontWeight = FontWeight.Bold
                                        )
                                        when (card.unitType) {

                                            UnitType.INFANTRY ->
                                                SpecialStatDisplay(
                                                    stringResource(R.string.player_hand_counters_cavalry),
                                                    R.drawable.counter_icon
                                                )

                                            UnitType.CAVALRY -> Column(
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                SpecialStatDisplay(
                                                    stringResource(R.string.player_hand_charge),
                                                    R.drawable.charge_icon
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                SpecialStatDisplay(
                                                    stringResource(R.string.player_hand_counters_musket_artillery),
                                                    R.drawable.counter_icon
                                                )

                                            }

                                            UnitType.ARTILLERY ->
                                                SpecialStatDisplay(
                                                    stringResource(R.string.player_hand_shell),
                                                    R.drawable.attack_artillery
                                                )

                                            UnitType.MISSILE -> TODO()

                                            UnitType.MUSKET -> SpecialStatDisplay(
                                                stringResource(R.string.player_hand_bayonet),
                                                R.drawable.bayonet,
                                                Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dismiss hint
                        Text(
                            text = stringResource(R.string.player_hand_dismiss),
                            fontSize = 12.sp,
                            color = Color(0xFF8B6914),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManaCost(
    manaOfCard: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        repeat(manaOfCard) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shadow(
                        elevation = 1.dp,
                        shape = bloodDropShape
                    )
                    .background(TurkishRed, bloodDropShape)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = bloodDropShape
                    )
            )
        }
    }
}

@Composable
private fun StatDisplay(
    icon: @Composable () -> Unit,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFFD4AF37),
            fontFamily = libreFont
        )
    }
}

@Composable
private fun SpecialStatDisplay(
    specialStatString: String,
    specialStatIcon: Int,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(specialStatIcon),
            contentDescription = stringResource(R.string.player_hand_special_icon),
            tint = Color(0xFFD4AF37),
            modifier = modifier
        )
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = specialStatString,
            fontSize = 12.sp,
            color = Color(0xFFE8D5B7),
            fontWeight = FontWeight.Medium
        )
    }
}