package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val OutfitFontFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal)
)

val CormorantGaramondFontFamily = FontFamily(
    Font(R.font.cormorant_garamond, FontWeight.Normal)
)

val DotGothicFontFamily = FontFamily(
    Font(R.font.dotgothic16, FontWeight.Normal)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 62.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
