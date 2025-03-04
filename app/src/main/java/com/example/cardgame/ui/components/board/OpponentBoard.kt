package com.example.cardgame.ui.components.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cardgame.data.model.card.UnitCard

@Composable
fun OpponentBoard(
    units: List<UnitCard>,
    selectedUnit: Int,
    onUnitClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        units.forEachIndexed { index, unit ->
            UnitSlot(
                unit = unit,
                isSelected = false,
                isPlayerUnit = false,
                onClick = { onUnitClick(index) }
            )
        }
    }
}