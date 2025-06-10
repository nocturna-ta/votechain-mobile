package com.nocturna.votechain.ui.theme

import androidx.compose.ui.graphics.Color

// Main Colors
object MainColors {
    val Primary1 = Color(0xFF41716B)
    val Primary2 = Color(0xFFE13F14)
}

// Primary Colors
object PrimaryColors {
    val Primary10 = Color(0xFF9CCCCC)
    val Primary20 = Color(0xFF8BBDC0)
    val Primary30 = Color(0xFF7AADB2)
    val Primary40 = Color(0xFF699E9F)
    val Primary50 = Color(0xFF547F7E)
    val Primary60 = Color(0xFF456C6C)
    val Primary70 = Color(0xFF365A5F)
    val Primary80 = Color(0xFF2E4E53)
    val Primary90 = Color(0xFF192E37)
    val Primary100 = Color(0xFF0B2427)
}

// Secondary Colors
object SecondaryColors {
    val Secondary10 = Color(0xFFF6C4C1)
    val Secondary20 = Color(0xFFF6A79B)
    val Secondary30 = Color(0xFFF68056)
    val Secondary40 = Color(0xFFEE453F)
    val Secondary50 = Color(0xFFE13F14)
    val Secondary60 = Color(0xFFCA3B12)
    val Secondary70 = Color(0xFFAD352F)
    val Secondary80 = Color(0xFF9B3308)
    val Secondary90 = Color(0xFF7F1908)
    val Secondary100 = Color(0xFF640705)
}

// Neutral Colors
object NeutralColors {
    val Neutral10 = Color(0xFFFFFFFF)
    val Neutral20 = Color(0xFFE5E5E5)
    val Neutral30 = Color(0xFFD5D5D5)
    val Neutral40 = Color(0xFFA3A3A3)
    val Neutral50 = Color(0xFF737373)
    val Neutral60 = Color(0xFF525252)
    val Neutral70 = Color(0xFF404040)
    val Neutral80 = Color(0xFF262626)
    val Neutral90 = Color(0xFF171717)
    val Neutral100 = Color(0xFF000000)
}

// Danger Colors
object DangerColors {
    val Danger10 = Color(0xFFFEE5E5)
    val Danger20 = Color(0xFFFCBCC2)
    val Danger30 = Color(0xFFE76F76)
    val Danger40 = Color(0xFFED4C4C)
    val Danger50 = Color(0xFFDC2626)
    val Danger60 = Color(0xFFB52626)
    val Danger70 = Color(0xFF9B1C1C)
    val Danger80 = Color(0xFF881515)
    val Danger90 = Color(0xFF7F0000)
    val Danger100 = Color(0xFF640000)
}

// Warning Colors
object WarningColors {
    val Warning10 = Color(0xFFFEF3C7)
    val Warning20 = Color(0xFFFFE699)
    val Warning30 = Color(0xFFFCD34D)
    val Warning40 = Color(0xFFFACC15)
    val Warning50 = Color(0xFFF59E0B)
    val Warning60 = Color(0xFFEF8305)
    val Warning70 = Color(0xFFD97706)
    val Warning80 = Color(0xFFB45309)
    val Warning90 = Color(0xFF92400E)
    val Warning100 = Color(0xFF7C2D12)
}

// Success Colors
object SuccessColors {
    val Success10 = Color(0xFFE1F9EC)
    val Success20 = Color(0xFFC1E8CF)
    val Success30 = Color(0xFF8BD0A7)
    val Success40 = Color(0xFF4CB88B)
    val Success50 = Color(0xFF16A34A)
    val Success60 = Color(0xFF15803D)
    val Success70 = Color(0xFF166534)
    val Success80 = Color(0xFF14532D)
    val Success90 = Color(0xFF052E16)
    val Success100 = Color(0xFF052E16)
}

// Info Colors
object InfoColors {
    val Info10 = Color(0xFFE0F2FE)
    val Info20 = Color(0xFFBAE6FD)
    val Info30 = Color(0xFF7DD3FC)
    val Info40 = Color(0xFF38BDF8)
    val Info50 = Color(0xFF0EA5E9)
    val Info60 = Color(0xFF0284C7)
    val Info70 = Color(0xFF0369A1)
    val Info80 = Color(0xFF075985)
    val Info90 = Color(0xFF0C4A6E)
    val Info100 = Color(0xFF082F49)
}

//Additional
object AdditionalColors {
    val strokeColor = Color(0xFF4E4E4E)
}

// Semantic Colors
object ThemeColor {
    val Background = NeutralColors.Neutral10
    val Surface = NeutralColors.Neutral20
    val OnSurface = NeutralColors.Neutral90
    val Primary = MainColors.Primary1
    val PrimaryVariant = PrimaryColors.Primary60
    val Secondary = MainColors.Primary2
    val SecondaryVariant = SecondaryColors.Secondary70
    val Error = DangerColors.Danger50
    val OnPrimary = NeutralColors.Neutral10
    val OnSecondary = NeutralColors.Neutral10
    val OnBackground = NeutralColors.Neutral90
    val OnError = NeutralColors.Neutral10

    // Additional
    val Success = SuccessColors.Success50
    val Warning = WarningColors.Warning50
    val Info = InfoColors.Info50
    val OnSuccess = NeutralColors.Neutral10
    val OnWarning = NeutralColors.Neutral90
    val OnInfo = NeutralColors.Neutral10
}