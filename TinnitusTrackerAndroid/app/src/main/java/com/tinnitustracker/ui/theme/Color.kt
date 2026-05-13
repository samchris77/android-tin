package com.tinnitustracker.ui.theme

import androidx.compose.ui.graphics.Color

// Palette mirrors the wiki/app mockup — calm, calibrated, trustworthy.

val Bg          = Color(0xFFF5F1EA) // warm off-white
val Surface     = Color(0xFFFFFFFF)
val Ink         = Color(0xFF1A2E2C) // primary text
val Ink2        = Color(0xFF4A5C5A) // secondary text
val Muted       = Color(0xFF8B9694)
val Line        = Color(0xFFE5DED2)

val Teal        = Color(0xFF2D5F5D) // primary accent
val Teal2       = Color(0xFF3E7A77)
val TealSoft    = Color(0xFFE8F0EE)

val Coral       = Color(0xFFD67C5C) // active intervention
val CoralSoft   = Color(0xFFF8E4D9)

val Warn        = Color(0xFFC44536)
val WarnSoft    = Color(0xFFF7DDD8)

// Heatmap intensity ramp for the Records calendar — 0-state + 4 steps.
// Heat0 is a touch darker than `Bg` so empty day cells still read as cells.
val Heat0       = Color(0xFFEFEAE0)
val Heat1       = Color(0xFFDDE8E5)
val Heat2       = Color(0xFFB8D2CD)
val Heat3       = Color(0xFF6E9C97)
val Heat4       = Teal

// Dark studio theme + orange accent (FrequencyMatchingScreen)
val DarkBg        = Color(0xFF0D0D0F)
val DarkCard      = Color(0xFF1A1A1D)
val CreamPanel    = Color(0xFFECE5D8)
val CreamPanel2   = Color(0xFFDDD5C5)
val InkCream      = Color(0xFF3B3A36)
val OrangeAccent  = Color(0xFFFF7A1A)
val OrangeAccent2 = Color(0xFFFF8C33)
val TextPrimary   = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA8A8A8)
val TextTertiary  = Color(0xFF6B6B6B)
