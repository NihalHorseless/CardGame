package io.github.nihalhorseless.eternalglory.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CardGameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF5271FF),
            secondary = Color(0xFFFF5252),
            background = Color(0xFF1F2233),
            surface = Color(0xFF2D3250),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        typography = Typography(
            headlineSmall = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            ),
            headlineMedium = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            ),
            headlineLarge = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            bodySmall = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )
        ),
        shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(12.dp)
        ),
        content = content
    )
}