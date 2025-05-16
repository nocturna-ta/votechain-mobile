package com.nocturna.votechain.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(16.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(48.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

object AppShapes {
    val extraSmall = RoundedCornerShape(2.dp)
    val extraLarge = RoundedCornerShape(16.dp)
    val pill = RoundedCornerShape(50)
}