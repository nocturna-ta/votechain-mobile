package com.nocturna.votechain.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nocturna.votechain.R

// Define the Manrope font family with its various weights
private val Manrope = FontFamily(
    Font(R.font.manrope_reguler, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

object AppTypography {
    // Heading 1
    val heading1Regular = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 30.sp
    )
    val heading1Medium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 30.sp
    )
    val heading1SemiBold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    )
    val heading1Bold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    )

    // Heading 2
    val heading2Regular = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 30.sp
    )
    val heading2Medium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 30.sp
    )
    val heading2SemiBold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 30.sp
    )
    val heading2Bold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 30.sp
    )

    // Heading 3
    val heading3Regular = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 30.sp
    )
    val heading3Medium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 30.sp
    )
    val heading3SemiBold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 30.sp
    )
    val heading3Bold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 30.sp
    )

    // Heading 4
    val heading4Regular = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 30.sp
    )
    val heading4Medium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 30.sp
    )
    val heading4SemiBold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 30.sp
    )
    val heading4Bold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 30.sp
    )

    // Heading 5
    val heading5Regular = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 30.sp
    )
    val heading5Medium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 30.sp
    )
    val heading5SemiBold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 30.sp
    )
    val heading5Bold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 30.sp
    )

    // Heading 6
    val heading6Regular = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 30.sp
    )
    val heading6Medium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 30.sp
    )
    val heading6SemiBold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 30.sp
    )
    val heading6Bold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 30.sp
    )

    // Paragraph
    val paragraphRegular = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 30.sp
    )
    val paragraphMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 30.sp
    )
    val paragraphSemiBold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 30.sp
    )
    val paragraphBold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 30.sp
    )

    // Small Paragraph
    val smallParagraphRegular = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        lineHeight = 30.sp
    )
    val smallParagraphMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 8.sp,
        lineHeight = 30.sp
    )
    val smallParagraphSemiBold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 8.sp,
        lineHeight = 30.sp
    )
    val smallParagraphBold = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 8.sp,
        lineHeight = 30.sp
    )
}

// Extension function to convert dp to sp for lineHeight
private fun Int.dpToSp() = (this / 16f).sp

// Default Material3 Typography with our custom text styles
val Typography = Typography(
    displayLarge = AppTypography.heading1Bold,
    displayMedium = AppTypography.heading1SemiBold,
    displaySmall = AppTypography.heading1Medium,

    headlineLarge = AppTypography.heading2Bold,
    headlineMedium = AppTypography.heading2SemiBold,
    headlineSmall = AppTypography.heading2Medium,

    titleLarge = AppTypography.heading3SemiBold,
    titleMedium = AppTypography.heading4SemiBold,
    titleSmall = AppTypography.heading5SemiBold,

    bodyLarge = AppTypography.heading4Regular,
    bodyMedium = AppTypography.heading5Regular,
    bodySmall = AppTypography.heading6Regular,

    labelLarge = AppTypography.paragraphMedium,
    labelMedium = AppTypography.paragraphRegular,
    labelSmall = AppTypography.smallParagraphRegular
)