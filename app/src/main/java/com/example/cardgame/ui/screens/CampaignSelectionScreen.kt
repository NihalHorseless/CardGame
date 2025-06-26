package com.example.cardgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardgame.R
import com.example.cardgame.data.model.campaign.Campaign
import com.example.cardgame.ui.theme.libreFont

@Composable
fun CampaignSelectionScreen(
    campaigns: List<Campaign>,
    onCampaignSelected: (Campaign) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_icon),
                    tint = Color.White
                )
            }

            Text(
                text = stringResource(R.string.campaign_selection_header),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Empty box for alignment
            Box(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Campaign list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(campaigns) { campaign ->
                CampaignCard(
                    campaign = campaign,
                    onClick = { onCampaignSelected(campaign) }
                )
            }
        }
    }
}

@Composable
fun CampaignCard(
    campaign: Campaign,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedLevels = campaign.levels.count { it.isCompleted }
    val totalLevels = campaign.levels.size
    val progressPercentage = if (totalLevels > 0) completedLevels.toFloat() / totalLevels else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF343861)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Campaign image/background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E3C72),
                                Color(0xFF2A5298)
                            )
                        )
                    )
            )
            {
                Image(painter = painterResource(R.drawable.napoleon_hat), contentDescription = "Campaign Image", modifier = Modifier.fillMaxSize())
            }

            // Overlay with campaign details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Campaign name and details
                Column {
                    Text(
                        text = campaign.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = libreFont,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = campaign.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Progress indicators
                Column {
                    Text(
                        text = "Progress: $completedLevels/$totalLevels levels completed",
                        fontSize = 14.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progressPercentage },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}