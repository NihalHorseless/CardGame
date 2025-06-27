package com.example.cardgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.data.model.msc.CreditCategory
import com.example.cardgame.data.model.msc.CreditItem
import com.example.cardgame.data.model.msc.GameCredits
import com.example.cardgame.ui.theme.libreFont

@Composable
fun CreditsScreen(
    onBackPressed: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
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
                text = "Credits",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = libreFont,
                color = Color.White
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        // Credits List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game Title
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF343861)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Eternal Glory",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = libreFont,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "A Strategic Card Battle Game",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Credit Categories
            items(GameCredits.credits) { category ->
                CreditCategoryCard(
                    category = category,
                    onUrlClick = { url ->
                        url?.let { uriHandler.openUri(it) }
                    }
                )
            }

            // License Notice
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4A4A4A)
                    )
                ) {
                    Text(
                        text = "This game uses various assets under different licenses. " +
                                "Click on links to view original sources and full license terms.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CreditCategoryCard(
    category: CreditCategory,
    onUrlClick: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF343861)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Category Title
            Text(
                text = category.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5271FF),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Credit Items
            category.items.forEach { item ->
                CreditItemRow(
                    item = item,
                    onUrlClick = onUrlClick
                )
                if (item != category.items.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun CreditItemRow(
    item: CreditItem,
    onUrlClick: (String?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Asset Name
        Text(
            text = item.assetName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        // Author and Source
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "by ${item.author}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            if (item.source.isNotEmpty()) {
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )

                Text(
                    text = item.source,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // License
        if (item.license.isNotEmpty()) {
            Text(
                text = item.license,
                fontSize = 11.sp,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // URL (if available)
        item.url?.let { url ->
            Text(
                text = "View Source ↗",
                fontSize = 11.sp,
                color = Color(0xFF64B5F6),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { onUrlClick(url) }
            )
        }
    }
}