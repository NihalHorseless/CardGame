package io.github.nihalhorseless.eternalglory.ui.theme

import androidx.compose.foundation.shape.GenericShape
import kotlin.math.cos
import kotlin.math.sin

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
val bloodDropShape = GenericShape { size, _ ->
    // Blood drop base shape parameters
    val width = size.width
    val height = size.height

    // Start at the top middle (the point of the drop)
    moveTo(width / 2, 0f)

    // Create right curve of drop
    cubicTo(
        width * 0.8f, height * 0.3f,  // control point 1
        width, height * 0.5f,         // control point 2
        width * 0.8f, height * 0.8f   // end point
    )

    // Create bottom curve
    quadraticTo(
        width / 2, height,     // control point
        width * 0.2f, height * 0.8f  // end point
    )

    // Create left curve, back to top
    cubicTo(
        0f, height * 0.5f,     // control point 1
        width * 0.2f, height * 0.3f,  // control point 2
        width / 2, 0f          // back to start point
    )

    close()
}
val slenderSwordShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height

    // Sword dimensions - longer and thinner blade
    val bladeWidth = width * 0.4f       // Reduced from 0.65f for a thinner blade
    val tipWidth = width * 0.25f        // Reduced from 0.35f for a slimmer tip
    val bladeHeight = height * 0.8f     // Increased from 0.7f for a longer blade
    val guardWidth = width * 0.8f       // Same wide guard
    val guardHeight = height * 0.08f    // Same thin guard
    val handleWidth = width * 0.25f     // Same handle width
    val handleLength = height * 0.22f   // Same handle length

    // Start at top (blade tip)
    moveTo(width / 2, 0f)

    // Right side of the blade tip at a slight angle
    lineTo(width / 2 + tipWidth / 2, height * 0.1f)

    // Right edge of the blade - expand outward slightly
    lineTo(width / 2 + bladeWidth / 2, height * 0.15f)

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
    lineTo(width / 2 - bladeWidth / 2, height * 0.15f)

    // Left side of the tip
    lineTo(width / 2 - tipWidth / 2, height * 0.1f)

    // Close path back to tip
    close()
}
val scallopedCircleShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height

    val centerX = width / 2
    val centerY = height / 2
    val radius = minOf(width, height) / 2

    // Number of scallops around the circle
    val scallops = 12

    // Scallop depth (how deep each scallop cuts into the circle)
    val scallopDepth = radius * 0.15f

    // Inner radius (where the scallops reach inward)
    val innerRadius = radius - scallopDepth

    // Starting point
    val startAngle = 0f
    val angleStep = 360f / scallops

    // Start at the outer point of the first scallop
    val startX = centerX + radius * cos(Math.toRadians(startAngle.toDouble())).toFloat()
    val startY = centerY + radius * sin(Math.toRadians(startAngle.toDouble())).toFloat()
    moveTo(startX, startY)

    // Draw each scallop
    for (i in 0 until scallops) {
        val innerAngle = startAngle + (i + 0.5f) * angleStep
        val outerAngle2 = startAngle + (i + 1) * angleStep

        // Inner point
        val innerX = centerX + innerRadius * cos(Math.toRadians(innerAngle.toDouble())).toFloat()
        val innerY = centerY + innerRadius * sin(Math.toRadians(innerAngle.toDouble())).toFloat()

        // Outer point 2
        val outerX2 = centerX + radius * cos(Math.toRadians(outerAngle2.toDouble())).toFloat()
        val outerY2 = centerY + radius * sin(Math.toRadians(outerAngle2.toDouble())).toFloat()

        // Draw the scallop curve through these three points
        quadraticTo(
            innerX, innerY,
            outerX2, outerY2
        )
    }

    // Close the path
    close()
}
// Add this to your Shapes.kt file or create a new shape file
val diamondShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height

    // Start at top center
    moveTo(width / 2, 0f)

    // Move to right corner
    lineTo(width, height / 2)

    // Move to bottom center
    lineTo(width / 2, height)

    // Move to left corner
    lineTo(0f, height / 2)

    // Close the path back to top
    close()
}