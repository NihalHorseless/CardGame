package com.example.cardgame.ui.theme

import androidx.compose.foundation.shape.GenericShape

val kiteShieldShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height

    // Start at top center
    moveTo(width / 2, 0f)

    // Top right curve
    quadraticTo(
        width * 0.9f, height * 0.1f,  // control point
        width * 0.9f, height * 0.3f   // end point
    )

    // Right side - gradually tapering inward
    quadraticTo(
        width * 0.9f, height * 0.7f,  // control point
        width * 0.5f, height          // end point (bottom center)
    )

    // Left side - mirror of right side
    quadraticTo(
        width * 0.1f, height * 0.7f,  // control point
        width * 0.1f, height * 0.3f   // end point
    )

    // Top left curve
    quadraticTo(
        width * 0.1f, height * 0.1f,  // control point
        width / 2, 0f                 // end point (back to top)
    )

    close()
}

// Thicker Sword Shape Definition
val thickSwordShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height

    // Sword dimensions - massive blade proportions like the Dragon Slayer
    val bladeWidth = width * 0.65f      // Extremely wide blade
    val tipWidth = width * 0.35f        // Narrower at the tip
    val bladeHeight = height * 0.7f     // Blade is longer than regular swords
    val guardWidth = width * 0.8f       // Wide guard
    val guardHeight = height * 0.08f    // Thin guard
    val handleWidth = width * 0.25f     // Thicker handle for such a large sword
    val handleLength = height * 0.22f   // Longer handle for two-handed grip

    // Start at top (blade tip)
    moveTo(width / 2, 0f)

    // Right side of the blade tip at a slight angle
    lineTo(width / 2 + tipWidth / 2, height * 0.1f)

    // Right edge of the blade - expand outward slightly
    lineTo(width / 2 + bladeWidth / 2, height * 0.2f)

    // Right edge continues straight down
    lineTo(width / 2 + bladeWidth / 2, bladeHeight)

    // Right guard edge (slightly wider than blade)
    lineTo(width / 2 + guardWidth / 2, bladeHeight)
    lineTo(width / 2 + guardWidth / 2, bladeHeight + guardHeight)

    // Right handle edge
    lineTo(width / 2 + handleWidth / 2, bladeHeight + guardHeight)
    lineTo(width / 2 + handleWidth / 2, bladeHeight + guardHeight + handleLength)

    // Bottom of handle (pommel)
    lineTo(width / 2 - handleWidth / 2, bladeHeight + guardHeight + handleLength)

    // Left handle edge
    lineTo(width / 2 - handleWidth / 2, bladeHeight + guardHeight)

    // Left guard edge
    lineTo(width / 2 - guardWidth / 2, bladeHeight + guardHeight)
    lineTo(width / 2 - guardWidth / 2, bladeHeight)

    // Left edge of blade
    lineTo(width / 2 - bladeWidth / 2, bladeHeight)

    // Left edge of the blade - match the right side
    lineTo(width / 2 - bladeWidth / 2, height * 0.2f)

    // Left side of the tip
    lineTo(width / 2 - tipWidth / 2, height * 0.1f)

    // Close path back to tip
    close()
}