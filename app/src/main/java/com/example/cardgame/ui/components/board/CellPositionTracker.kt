package com.example.cardgame.ui.components.board

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * A composable that tracks the position of a board cell and registers it with the ViewModel.
 * This is used for animations that need to know the exact screen position of cells.
 */
@Composable
fun CellPositionTracker(
    row: Int,
    col: Int,
    registerPosition: (row: Int, col: Int, x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                val centerX = bounds.center.x
                val centerY = bounds.center.y
                registerPosition(row, col, centerX, centerY)
            }
    ) {
        content()
    }
}