package com.example.cardgame.ui.components.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cardgame.data.model.card.UnitCard

@Composable
fun GameBoard(
    units: List<UnitCard?>,
    selectedUnit: Int,
    isPlayerBoard: Boolean,
    onUnitClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if(isPlayerBoard) listOf(
                        Color(0xFF171C26),
                        Color(0xFF413973)
                    )
                    else listOf(
                        Color(0xFF171C26),
                        Color(0xFFF24141)
                    )
                )
            )
    )
    {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Create 5 slots for units
            for (i in 0 until 5) {
                val unit = if (i < units.size) units[i] else null

                UnitSlot(
                    unit = unit,
                    isSelected = i == selectedUnit,
                    isPlayerUnit = isPlayerBoard,
                    onClick = { onUnitClick(i) }
                )
            }
        }
    }
}