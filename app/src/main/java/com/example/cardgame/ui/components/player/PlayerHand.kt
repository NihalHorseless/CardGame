package com.example.cardgame.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.ui.components.effects.CardWithHoverEffect

@Composable
fun PlayerHand(
    cards: List<Card>,
    playerMana: Int,
    onCardClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(4.dp)
    ) {
        items(cards.size) { index ->
            val card = cards[index]
            val canPlay = card.manaCost <= playerMana

            // Use the hover effect component
            CardWithHoverEffect(
                modifier = Modifier.padding(horizontal = 4.dp),
                content = {
                    // Card content
                    val cardColor = if (canPlay) Color(0xFF1E3C72) else Color(0xFF555555)

                    Card(
                        modifier = Modifier
                            .height(140.dp)
                            .width(100.dp)
                            .clickable(enabled = canPlay) { onCardClick(index) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Card cost circle in top-left
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Blue, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = card.manaCost.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Card name
                            Text(
                                text = card.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Card stats for unit cards
                            if (card is UnitCard) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Attack value
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color.Red, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = card.attack.toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Health value
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color.Green, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = card.health.toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}