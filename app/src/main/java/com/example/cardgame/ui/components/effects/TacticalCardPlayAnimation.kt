package com.example.cardgame.ui.components.effects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.data.enum.TacticCardType
import com.example.cardgame.data.model.card.EnhancedTacticCard
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// Add animations to tactical card play
@Composable
fun TacticalCardPlayAnimation(
    tacticCard: EnhancedTacticCard?,
    isVisible: Boolean,
    onAnimationComplete: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible && tacticCard != null,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        if (tacticCard != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Card appearance animation
                val cardScale by animateFloatAsState(
                    targetValue = 1.2f,
                    animationSpec = tween(300, easing = EaseOutBack)
                )

                // Effect color based on card type
                val effectColor = when(tacticCard.cardType) {
                    TacticCardType.DIRECT_DAMAGE -> Color.Red
                    TacticCardType.AREA_EFFECT -> Color(0xFFFFA500) // Orange
                    TacticCardType.BUFF -> Color.Green
                    TacticCardType.DEBUFF -> Color.Red
                    TacticCardType.SPECIAL -> Color(0xFF00AAFF) // Blue
                }

                // Background glow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = effectColor,
                        radius = 300f,
                        center = center,
                        alpha = 0.2f
                    )
                }

                // Card visualization
                Card(
                    modifier = Modifier
                        .scale(cardScale)
                        .width(120.dp)
                        .height(180.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Card cost
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(32.dp)
                                .background(Color.Blue, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tacticCard.manaCost.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Card name
                        Text(
                            text = tacticCard.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Card description
                        Text(
                            text = tacticCard.description,
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Card type indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .background(effectColor.copy(alpha = 0.7f), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tacticCard.cardType.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Rays emanating from card
                val infiniteTransition = rememberInfiniteTransition()
                val rayAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2

                    for (angle in 0 until 360 step 30) {
                        val radians = angle * Math.PI / 180
                        val endX = centerX + 300 * cos(radians).toFloat()
                        val endY = centerY + 300 * sin(radians).toFloat()

                        drawLine(
                            color = effectColor,
                            start = Offset(centerX, centerY),
                            end = Offset(endX, endY),
                            strokeWidth = 3f,
                            alpha = rayAlpha
                        )
                    }
                }

                // Complete animation after delay
                LaunchedEffect(key1 = isVisible) {
                    if (isVisible) {
                        delay(1500)
                        onAnimationComplete()
                    }
                }
            }
        }
    }
}