package com.example.cardgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.data.model.msc.GameMechanic
import com.example.cardgame.ui.theme.libreFont
import com.example.cardgame.util.MiscellaneousData

@Composable
fun GuideScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State to track the selected mechanic
    var selectedMechanic by remember { mutableStateOf<GameMechanic?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F2233),
                        Color(0xFF2D3250)
                    )
                )
            )
    ) {
        if (selectedMechanic == null) {
            // Show mechanic selection grid
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "GAME GUIDE",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = libreFont,
                        color = Color.White
                    )

                    // Spacer for alignment
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Grid of mechanics
                Text(
                    text = "Select a game mechanic to learn more",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MiscellaneousData.mechanics) { mechanic ->
                        MechanicItem(
                            mechanic = mechanic,
                            onClick = { selectedMechanic = mechanic }
                        )
                    }
                }
            }
        } else {
            // Show mechanic detail screen
            MechanicDetailScreen(
                mechanic = selectedMechanic!!,
                onBackPressed = { selectedMechanic = null }
            )
        }
    }
}

@Composable
fun MechanicItem(
    mechanic: GameMechanic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF343861))
            .border(1.dp, Color(0xFF5271FF), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // Mechanic icon
        Image(
            painter = painterResource(id = mechanic.iconResId),
            contentDescription = mechanic.title,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Mechanic title
        Text(
            text = mechanic.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MechanicDetailScreen(
    mechanic: GameMechanic,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = mechanic.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = libreFont,
                color = Color.White
            )
        }

        // Mechanic icon and description
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Mechanic icon
            Image(
                painter = painterResource(id = mechanic.iconResId),
                contentDescription = mechanic.title,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF343861), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF5271FF), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = mechanic.description,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }

            // Additional explanation or example if available
            if (mechanic.example.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF343861), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF5271FF), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Example: ${mechanic.example}",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}