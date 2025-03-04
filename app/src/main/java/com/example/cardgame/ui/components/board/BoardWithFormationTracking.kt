package com.example.cardgame.ui.components.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
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
    registerPositions: (index: Int, x: Float, y: Float) -> Unit,
    activeFormations: List<Formation>,
    modifier: Modifier = Modifier
) {
    val slotPositions = mutableMapOf<Int, Offset>()

    Box(
        modifier = modifier
    ) {
        // Board layout with units
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 5) {
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            // Calculate the center of this slot in window coordinates
                            // This gives us absolute screen position
                            val bounds = coordinates.boundsInWindow()
                            val centerX = bounds.center.x
                            val centerY = bounds.center.y

                            slotPositions[i] = Offset(centerX, centerY)

                            // Register position with ViewModel for animations
                            registerPositions(i, centerX, centerY)
                        }
                ) {
                    // The actual unit slot
                    UnitSlot(
                        unit = if (i < units.size) units[i] else null,
                        isSelected = i == selectedUnit,
                        isPlayerUnit = isPlayerBoard,
                        onClick = { onUnitClick(i) }
                    )
                }
            }
        }

        // Formation overlay (if needed)
        if (activeFormations.isNotEmpty()) {
            FormationOverlay(
                activeFormations = activeFormations,
                boardUnits = units,
                slotPositions = slotPositions
            )
        }
    }
}