package io.github.nihalhorseless.eternalglory.ui.components.board

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nihalhorseless.eternalglory.R
import io.github.nihalhorseless.eternalglory.ui.theme.libreFont

@Composable
fun BattlefieldBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Main battlefield image
        Image(
            painter = painterResource(id = R.drawable.battlefield_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Optional: Add a semi-transparent overlay to ensure game elements are visible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color(0xFF2D3250).copy(alpha = 0.25f)
                )
        )
    }
}

@Composable
fun StatusMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message.isNotEmpty(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xDD000000) // Semi-transparent black
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = when {
                            message.contains("Cannot", ignoreCase = true) ||
                                    message.contains("Not enough", ignoreCase = true) ||
                                    message.contains("failed", ignoreCase = true) -> R.drawable.baseline_error_24
                            message.contains("successfully", ignoreCase = true) ||
                                    message.contains("completed", ignoreCase = true) -> R.drawable.baseline_check_circle_24
                            else -> R.drawable.baseline_info_24
                        }
                    ),
                    contentDescription = null,
                    tint = when {
                        message.contains("Cannot", ignoreCase = true) ||
                                message.contains("Not enough", ignoreCase = true) ||
                                message.contains("failed", ignoreCase = true) -> Color(0xFFFF6B6B)
                        message.contains("successfully", ignoreCase = true) ||
                                message.contains("completed", ignoreCase = true) -> Color(0xFF51CF66)
                        else -> Color(0xFF74B9FF)
                    },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = libreFont,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}