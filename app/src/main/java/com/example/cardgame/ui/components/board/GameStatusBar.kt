package com.example.cardgame.ui.components.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GameStatusBar(
    playerMana: Int,
    playerMaxMana: Int,
    isPlayerTurn: Boolean,
    onEndTurn: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {


        // Mana display
            Text(
                text = "$playerMana/$playerMaxMana",
                color = Color(0xFF3498DB),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onEndTurn,
                enabled = isPlayerTurn,
                modifier = Modifier.padding(4.dp)
            ) {
                Text("End Turn")
            }

    }
}