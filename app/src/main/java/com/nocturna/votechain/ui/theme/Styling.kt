package com.nocturna.votechain.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

data class CandidateDetailStyling(
    // Section Title Styles
    val sectionTitleStyle: TextStyle = AppTypography.heading6SemiBold,
    val sectionTitleColor: Color = PrimaryColors.Primary60,

    // Personal Info Styles
    val personalInfoLabelStyle: TextStyle = AppTypography.paragraphRegular,
    val personalInfoLabelColor: Color = NeutralColors.Neutral80,
    val personalInfoValueStyle: TextStyle = AppTypography.paragraphRegular,
    val personalInfoValueColor: Color = NeutralColors.Neutral80,

    // Table Header Styles
    val tableHeaderBackground: Color = NeutralColors.Neutral20,
    val tableHeaderTextStyle: TextStyle = AppTypography.paragraphSemiBold,
    val tableHeaderTextColor: Color = NeutralColors.Neutral80,

    // Education Table Row Styles
    val educationInstitutionStyle: TextStyle = AppTypography.paragraphRegular,
    val educationInstitutionColor: Color = NeutralColors.Neutral80,
    val educationPeriodStyle: TextStyle = AppTypography.paragraphRegular,
    val educationPeriodColor: Color = NeutralColors.Neutral80,

    // Work Table Row Styles
    val workInstitutionStyle: TextStyle = AppTypography.paragraphRegular,
    val workInstitutionColor: Color = NeutralColors.Neutral80,
    val workPositionStyle: TextStyle = AppTypography.paragraphRegular,
    val workPositionColor: Color = NeutralColors.Neutral80,
    val workPeriodStyle: TextStyle = AppTypography.paragraphRegular,
    val workPeriodColor: Color = NeutralColors.Neutral80
)