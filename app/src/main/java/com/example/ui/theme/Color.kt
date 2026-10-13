package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Light / Reference Palette
val NotesGradientTop = Color(0xFF9DB8D9)
val NotesGradientMiddle = Color(0xFFA9CBE8)
val NotesGradientBottom = Color(0xFFBFE0FA)

val DetailGradientTop = Color(0xFF9FA8D6)
val DetailGradientMiddle = Color(0xFFD2B4EC)
val DetailGradientBottom = Color(0xFF9DB5DE)

val HomeGradientTopLeft = Color(0xFFA3B4DA)
val HomeGradientCenter = Color(0xFFF5C0B8)
val HomeGradientRight = Color(0xFFF59BD0)
val HomeGradientBottom = Color(0xFFBDE0F7)

val ChipSelectedFill = Color(0xFFF6F58A)
val ChipSelectedText = Color(0xFF1B1B1B)

val MintCardStart = Color(0xFF78C7AE)
val MintCardEnd = Color(0xFF9ED8C0)

val LavenderCard = Color(0xFFC3C6F5)

val CreamCard = Color(0xFFE4E8B8)
val CreamCardText = Color(0xFFB58A55)

val OrchidCard = Color(0xFFE5C4EC)
val OrchidCardText = Color(0xFFB0286E)

val AccentBlue = Color(0xFF2468AE)
val BodyText = Color(0xFF1F2430)
val BodySubtle = Color(0xFF6B7280)

val PillRing = Color(0xD946526B) // #46526B at 85% alpha
val PillMicCircle = Color(0xFF7D7287) // muted-mauve circle for mic

val GlassBackground = Color(0x33FFFFFF) // ~20% translucent white
val GlassBackgroundElevated = Color(0x59FFFFFF) // ~35% translucent white
val GlassBorder = Color(0x66FFFFFF) // 40% white border
val GlassHighlight = Color(0x40FFFFFF)

val CoralCardAccent = Color(0xFFF28F79)

// Dark Theme Variants (deepened navy, indigo, plum)
val DarkNotesGradientTop = Color(0xFF1A2634)
val DarkNotesGradientMiddle = Color(0xFF223548)
val DarkNotesGradientBottom = Color(0xFF2E465E)

val DarkDetailGradientTop = Color(0xFF232545)
val DarkDetailGradientMiddle = Color(0xFF3B2E4E)
val DarkDetailGradientBottom = Color(0xFF242C44)

val DarkHomeGradientTopLeft = Color(0xFF20263E)
val DarkHomeGradientCenter = Color(0xFF452E38)
val DarkHomeGradientRight = Color(0xFF43263E)
val DarkHomeGradientBottom = Color(0xFF1B2C3C)

val DarkGlassBackground = Color(0x33101828)
val DarkGlassBorder = Color(0x44FFFFFF)
val DarkBodyText = Color(0xFFF0F4F8)
val DarkAccentBlue = Color(0xFF6AA7EB)

fun notesGradient(isDark: Boolean = false) = Brush.verticalGradient(
    colors = if (!isDark) listOf(NotesGradientTop, NotesGradientMiddle, NotesGradientBottom)
    else listOf(DarkNotesGradientTop, DarkNotesGradientMiddle, DarkNotesGradientBottom)
)

fun detailGradient(isDark: Boolean = false) = Brush.verticalGradient(
    colors = if (!isDark) listOf(DetailGradientTop, DetailGradientMiddle, DetailGradientBottom)
    else listOf(DarkDetailGradientTop, DarkDetailGradientMiddle, DarkDetailGradientBottom)
)

fun homeGradient(isDark: Boolean = false) = Brush.linearGradient(
    colors = if (!isDark) listOf(HomeGradientTopLeft, HomeGradientCenter, HomeGradientRight, HomeGradientBottom)
    else listOf(DarkHomeGradientTopLeft, DarkHomeGradientCenter, DarkHomeGradientRight, DarkHomeGradientBottom)
)
