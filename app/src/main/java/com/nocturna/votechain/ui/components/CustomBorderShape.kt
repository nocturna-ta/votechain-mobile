package com.nocturna.votechain.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Custom shape class to create borders only on specific sides
 * @param top Show top border
 * @param bottom Show bottom border
 * @param start Show start (left in LTR layouts) border
 * @param end Show end (right in LTR layouts) border
 */
class CustomBorderShape(
    val top: Boolean = false,
    val bottom: Boolean = false,
    val start: Boolean = false,
    val end: Boolean = false
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()

        // Draw top border if needed
        if (top) {
            path.moveTo(0f, 0f)
            path.lineTo(size.width, 0f)
        }

        // Draw end (right) border if needed
        if (end) {
            path.moveTo(size.width, 0f)
            path.lineTo(size.width, size.height)
        }

        // Draw bottom border if needed
        if (bottom) {
            path.moveTo(size.width, size.height)
            path.lineTo(0f, size.height)
        }

        // Draw start (left) border if needed
        if (start) {
            path.moveTo(0f, size.height)
            path.lineTo(0f, 0f)
        }

        return Outline.Generic(path)
    }
}