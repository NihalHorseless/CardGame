package com.example.cardgame.ui.components.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.model.formation.Formation

/**
 * Enhanced version that can track actual positions of slots using composition locals
 */
@Composable
fun BoardWithFormationTracking(
    units: List<UnitCard?>,
    selectedUnit: Int,
    isPlayerBoard: Boolean,
    onUnitClick: (Int) -> Unit,
    activeFormations: List<Formation>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val slotPositions = mutableMapOf<Int, Offset>()

    Box(modifier = modifier) {
        // Regular board layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            units.forEachIndexed { index, unit ->
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            // Save the center position of this slot
                            val position = coordinates.positionInParent()
                            val size = coordinates.size
                            slotPositions[index] = Offset(
                                position.x + size.width / 2,
                                position.y + size.height / 2
                            )
                        }
                ) {
                    UnitSlot(
                        unit = unit,
                        isSelected = index == selectedUnit,
                        isPlayerUnit = isPlayerBoard,
                        onClick = { onUnitClick(index) }
                    )
                }
            }
        }

        // Formation overlay using actual positions
        FormationOverlay(
            activeFormations = activeFormations,
            boardUnits = units,
            slotPositions = slotPositions
        )
    }
}