package com.esh.offlineai.ui.theme

import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────
// 🌊  MindSync "Liquid" Palette  v1.2.0
// ──────────────────────────────────────────────

// Core Liquid Blues (Deep Oceanic)
val LiquidDeepBlue = Color(0xFF0A0E27)      // Abyss background
val LiquidNavyBlue = Color(0xFF101935)       // Surface layer 1
val LiquidDarkBlue = Color(0xFF162044)       // Surface layer 2 (cards)
val LiquidMidBlue = Color(0xFF1B2B5C)        // Elevated surface

// Luminous Accent Colors (Liquid Glow)
val LiquidCyan = Color(0xFF00E5FF)           // Primary accent
val LiquidCyanLight = Color(0xFF6EFFFF)      // Light cyan variant
val LiquidCyanDark = Color(0xFF00B8D4)       // Darker cyan
val LiquidElectricBlue = Color(0xFF448AFF)   // Electric blue
val LiquidSkyBlue = Color(0xFF40C4FF)        // Sky blue
val LiquidAqua = Color(0xFF18FFFF)           // Bright aqua

// Aurora / Liquid Purple Accents
val LiquidPurple = Color(0xFF7C4DFF)         // Deep violet
val LiquidPurpleLight = Color(0xFFB388FF)    // Soft lavender
val LiquidMagenta = Color(0xFFE040FB)        // Vivid magenta
val LiquidPink = Color(0xFFFF80AB)           // Soft pink glow

// Warm Liquid Highlights
val LiquidAmber = Color(0xFFFFAB40)          // Warm amber accent
val LiquidGold = Color(0xFFFFD740)           // Gold shimmer
val LiquidMint = Color(0xFF64FFDA)           // Mint highlight

// Surface Colors
val LiquidSurface1 = Color(0xFF0F1429)       // Base surface
val LiquidSurface2 = Color(0xFF151C36)       // Card surface
val LiquidSurface3 = Color(0xFF1C2545)       // Elevated card
val LiquidGlass = Color(0x2600E5FF)          // Glassmorphism fill
val LiquidGlassStrong = Color(0x4D00E5FF)    // Stronger glass fill
val LiquidGlassBorder = Color(0x3300E5FF)    // Glass border

// Text Colors
val LiquidTextPrimary = Color(0xFFF0F4FF)    // Crisp white-blue text
val LiquidTextSecondary = Color(0xFFB0BFE6)  // Muted blue-grey text
val LiquidTextMuted = Color(0xFF6B7DAA)      // Dim text

// Gradient Start/End Colors
val GradientDeepStart = Color(0xFF080D20)
val GradientDeepEnd = Color(0xFF0D1B3E)
val GradientLiquidStart = Color(0xFF0A1628)
val GradientLiquidMid = Color(0xFF0E1F42)
val GradientLiquidEnd = Color(0xFF15295A)
val GradientAuroraStart = Color(0xFF7C4DFF)
val GradientAuroraEnd = Color(0xFF00E5FF)

// Status Colors
val LiquidSuccess = Color(0xFF69F0AE)
val LiquidWarning = Color(0xFFFFD54F)
val LiquidError = Color(0xFFFF5252)
val LiquidErrorDark = Color(0xFFFF1744)

// Legacy compat aliases (used across theme)
val Purple80 = LiquidPurpleLight
val PurpleGrey80 = LiquidTextSecondary
val Pink80 = LiquidPink

val PrimaryDark = LiquidElectricBlue
val SecondaryDark = LiquidCyan
val BackgroundDark = LiquidDeepBlue
val SurfaceDark = LiquidSurface2
val TextPrimaryDark = LiquidTextPrimary
val TextSecondaryDark = LiquidTextSecondary