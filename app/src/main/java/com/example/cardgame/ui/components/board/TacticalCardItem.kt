package com.example.cardgame.ui.components.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.R
import com.example.cardgame.data.enum.TacticCardType
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.ui.theme.bloodDropShape
import com.example.cardgame.ui.theme.libreFont

/**
 * UI component for displaying a Tactic Card in the player's hand or collection
 */
@Composable
fun TacticCardItem(
    card: TacticCard,
    isSelected: Boolean = false,
    isPlayable: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine card background color based on card type
    val cardColor = when (card.cardType) {
        TacticCardType.DIRECT_DAMAGE -> Color(0xFFB71C1C)  // Dark red
        TacticCardType.AREA_EFFECT -> Color(0xFFFF5722)    // Deep orange
        TacticCardType.BUFF -> Color(0xFF4CAF50)           // Green
        TacticCardType.DEBUFF -> Color(0xFF7B1FA2)         // Purple
        TacticCardType.SPECIAL -> Color(0xFF1976D2)        // Blue
    }


    Card(
        modifier = modifier
            .width(100.dp)
            .height(140.dp)
            .shadow(
                elevation = if (isSelected) 8.dp else 4.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF4CAF50) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isPlayable) { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            cardColor,
                            cardColor.copy(alpha = 0.7f)
                        )
                    )
                )
        ) {
            // Card contents

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
                    fontSize = 16.sp
                )
            }

            // Card name
            Text(
                text = card.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = libreFont,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .fillMaxWidth()
            )

            // Card icon/image
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center)
                    .offset(y = (-6).dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                TacticTypeIcon(tacticCardType = card.cardType)
            }

            // Card description
            Text(
                text = card.description,
                color = Color.White,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                fontFamily = libreFont,
                maxLines = 2,
                lineHeight = 12.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(4.dp)
                    .offset(y = (-24).dp)
            )
        }
    }
}
@Composable
fun TacticTypeIcon(
    tacticCardType: TacticCardType,
    modifier: Modifier = Modifier
) {
    val imageRes: Int = when (tacticCardType) {
        TacticCardType.DIRECT_DAMAGE -> R.drawable.tactic_card_direct
        TacticCardType.AREA_EFFECT -> R.drawable.tactic_card_area_effect
        TacticCardType.BUFF -> R.drawable.buff_effect
        TacticCardType.DEBUFF -> R.drawable.tactic_efffect_debuff
        TacticCardType.SPECIAL -> R.drawable.magic_effect_icon
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = tacticCardType.name,
        modifier = modifier
    )
}