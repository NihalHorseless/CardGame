package io.github.nihalhorseless.eternalglory.ui.components.board

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.github.nihalhorseless.eternalglory.R
import io.github.nihalhorseless.eternalglory.data.enum.UnitEra
import io.github.nihalhorseless.eternalglory.data.enum.UnitType
import io.github.nihalhorseless.eternalglory.data.model.card.UnitCard
import io.github.nihalhorseless.eternalglory.ui.theme.EnemyColor
import io.github.nihalhorseless.eternalglory.ui.theme.kiteShieldShape
import io.github.nihalhorseless.eternalglory.ui.theme.thickSwordShape

@Composable
fun UnitSlot(
    unit: UnitCard?,
    visualHealth: Int? = null,
    isSelected: Boolean,
    isPlayerUnit: Boolean,
    isDying: Boolean = false,
    onAttachBayonet: (() -> Unit)? = null,
    canAttack: Boolean = false,
    canMove: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.Yellow
            isPlayerUnit && canAttack -> Color.Red.copy(alpha = 0.7f)
            isPlayerUnit && canMove -> Color.Blue.copy(alpha = 0.7f)
            isPlayerUnit -> Color.Green
            else -> Color.Gray
        }
    )

    val borderWidth = if (isSelected) 3.dp else 1.dp


    // Selection animation
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(200)
    )

    // Fade-out animation for dying units
    val alpha by animateFloatAsState(
        targetValue = if (isDying) 0f else 1f,
        animationSpec = tween(300)  // Fast fade-out
    )

    // Card color based on era
    val cardColor = when (unit?.unitEra) {
        UnitEra.ANCIENT -> Color(0xFF8D6E63)    // Brown
        UnitEra.ROMAN -> Color(0xFFB71C1C)      // Dark Red
        UnitEra.MEDIEVAL -> Color(0xFF1A237E)   // Dark Blue
        UnitEra.NAPOLEONIC -> Color(0xFF4A148C) // Purple
        null -> Color(0xFF424242) // Gray for empty slot
    }

    // This outer box contains both the card and the indicators
    Box(
        modifier = modifier
            .size(65.dp, 80.dp)
            .scale(scale)
            .alpha(alpha)
    ) {
        // The main card with oval shape
        Card(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isSelected) 12.dp else 4.dp,
                    shape = GenericShape { size, _ ->
                        addOval(Rect(0f, 0f, size.width, size.height))
                    }
                )
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = GenericShape { size, _ ->
                        addOval(Rect(0f, 0f, size.width, size.height))
                    }
                )
                .clickable(enabled = unit != null) { onClick() },
            shape = GenericShape { size, _ ->
                addOval(Rect(0f, 0f, size.width, size.height))
            }, colors = CardDefaults.cardColors(containerColor = if(isPlayerUnit) cardColor else EnemyColor)
        ) {
            if (unit != null) {
                // Card with unit
                Box(modifier = Modifier.fillMaxSize()) {
                    // Unit Type Icon
                    if(!isDying){
                        UnitTypeIcon(
                            unitType = unit.unitType,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                        )
                    }


                }
            } else {
                // Empty slot
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    // Empty slot indicator (optional)
                }
            }
        }

        // Only add the stat indicators if there's a unit in the slot
        if (unit != null) {
            // Attack Value - positioned outside the oval at bottom left
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterStart)
                    .offset((-3).dp, (12).dp)
                    .zIndex(1f)
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xFFFF9800), thickSwordShape)
                    .border(1.dp, Color.White, thickSwordShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.attack.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.offset(y = (-3).dp)
                )
            }


            // Health Value - positioned outside the oval at bottom right
            val displayHealth = visualHealth ?: unit.health
            val healthColor = when {
                displayHealth <= unit.maxHealth / 3 -> Color.Red
                displayHealth <= unit.maxHealth * 2 / 3 -> Color(0xFFFFA500) // Orange
                else -> Color.Green
            }


            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterEnd)
                    .offset((3).dp, (12).dp)
                    .zIndex(1f)
                    .shadow(4.dp, CircleShape)
                    .background(healthColor, kiteShieldShape)
                    .border(1.dp, Color.White, kiteShieldShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayHealth.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .offset(y = (-2).dp)
                )
            }


            // Taunt indicator
            if (unit.hasTaunt) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(Color(0xFF795548), CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_shield_24),
                        contentDescription = "Taunt",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Charge indicator
            if (unit.hasCharge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(18.dp)
                        .background(Color(0xFFFFC107), CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.charge_icon),
                        contentDescription = "Charge",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            if (isSelected && unit.unitType == UnitType.MUSKET && isPlayerUnit && (canMove || canAttack)) {
                // Bayonet button in top-end corner
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .offset(4.dp, (-4).dp)
                        .zIndex(2f)
                        .shadow(4.dp, CircleShape)
                        .background(Color(0xFF795548), CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                        .clickable { onAttachBayonet?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    // Bayonet icon - using a simple sword icon as placeholder
                    Icon(
                        painter = painterResource(R.drawable.bayonet),
                        contentDescription = "Attach Bayonet",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Display an icon based on unit type
 */
@Composable
fun UnitTypeIcon(
    unitType: UnitType,
    modifier: Modifier = Modifier
) {

    val imageRes: Int = when (unitType) {
        UnitType.INFANTRY -> R.drawable.rifle_with_bayonet
        UnitType.CAVALRY -> R.drawable.unit_type_cavalry
        UnitType.ARTILLERY -> R.drawable.unit_type_cannon
        UnitType.MISSILE -> R.drawable.unit_type_missile
        UnitType.MUSKET -> R.drawable.unit_type_musket
    }

    Image(
        painter = painterResource(imageRes),
        contentDescription = unitType.name,
        modifier = modifier
    )
}