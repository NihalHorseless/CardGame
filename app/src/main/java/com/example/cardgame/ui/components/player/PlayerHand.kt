package com.example.cardgame.ui.components.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.R
import com.example.cardgame.data.enum.UnitEra
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.ui.components.board.FortificationTypeIcon
import com.example.cardgame.ui.components.board.UnitTypeIcon
import com.example.cardgame.ui.theme.kiteShieldShape
import com.example.cardgame.ui.theme.thickSwordShape

@Composable
fun PlayerHand(
    cards: List<Card>,
    playerMana: Int,
    selectedCardIndex: Int? = null,  // Added parameter for selected card
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy((-20).dp) // Overlapping cards
    ) {
        itemsIndexed(cards) { index, card ->
            val isPlayable = card.manaCost <= playerMana
            val isSelected = index == selectedCardIndex

            // Card hover effect
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
                // The card
                HandCard(
                    card = card,
                    isPlayable = isPlayable,
                    isSelected = isSelected,  // Pass selection state
                    elevation = elevationDp.dp,
                    onClick = { onCardClick(index) }
                )
            }
        }
    }
}

/**
 * Individual card in the player's hand
 */
@Composable
fun HandCard(
    card: Card,
    isPlayable: Boolean,
    isSelected: Boolean = false,  // Added parameter for selection state
    elevation: Dp = 4.dp,
    onClick: () -> Unit
) {
    val cardColor = if (card is UnitCard) {
        when (card.unitEra) {
            UnitEra.ANCIENT -> Color(0xFF8D6E63)    // Brown
            UnitEra.ROMAN -> Color(0xFFB71C1C)      // Dark Red
            UnitEra.MEDIEVAL -> Color(0xFF1A237E)   // Dark Blue
            UnitEra.MODERN -> Color(0xFF212121)     // Dark Gray
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
            .clickable(enabled = isPlayable) { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Card Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                finalCardColor,
                                finalCardColor.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Mana Cost (top left)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomCenter)
                    .padding(4.dp)
                    .background(Color(0xFF2196F3), CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.manaCost.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Card Name
            Text(
                text = card.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
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
                    is TacticCard -> Image(
                        painter = painterResource(R.drawable.magic_effect_icon),
                        contentDescription = "Tactic Card",
                        modifier = Modifier.size(40.dp)
                    )
                    else -> Image(
                        painter = painterResource(R.drawable.magic_effect_icon),
                        contentDescription = "Tactic Card",
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