package com.example.foodbanklocator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Shared neutrals
val Cream         = Color(0xFFFFF4EC)   // Lightest background
val WarmBlush     = Color(0xFFF5C4B3)   // Cards, highlights
val BurntOrange   = Color(0xFFE8874A)   // Primary buttons, CTAs
val Terracotta    = Color(0xFF993C1D)   // Accents, icons
val DeepBrown     = Color(0xFF7A3B0F)   // Headings, strong text
val DarkBark      = Color(0xFF4A1B0C)   // Dark surfaces
val Mahogany      = Color(0xFF712B13)   // Borders, dividers

// Dark mode specific
val Espresso      = Color(0xFF2C1A0E)   // Dark background
val Ember         = Color(0xFFD85A30)   // Dark mode primary button
val SoftCoral     = Color(0xFFF0997B)   // Dark mode headings

// Semantic extras
val ErrorRed      = Color(0xFFB00020)
val OnErrorWhite  = Color(0xFFFFFFFF)

// -------------------------------------------------------
// Light Color Scheme
// -------------------------------------------------------
private val LightColorScheme = lightColorScheme(

    // Brand
    primary              = BurntOrange,
    onPrimary            = Cream,
    primaryContainer     = WarmBlush,
    onPrimaryContainer   = DeepBrown,

    // Secondary
    secondary            = Terracotta,
    onSecondary          = Cream,
    secondaryContainer   = WarmBlush,
    onSecondaryContainer = DarkBark,

    // Backgrounds
    background           = Cream,
    onBackground         = DeepBrown,

    // Surfaces (cards, dialogs, bottom sheets)
    surface              = Cream,
    onSurface            = DeepBrown,
    surfaceVariant       = WarmBlush,
    onSurfaceVariant     = Terracotta,

    // Outlines
    outline              = Mahogany,
    outlineVariant       = WarmBlush,

    // Error
    error                = ErrorRed,
    onError              = OnErrorWhite,
)

// -------------------------------------------------------
// Dark Color Scheme
// -------------------------------------------------------
private val DarkColorScheme = darkColorScheme(

    // Brand
    primary              = Ember,
    onPrimary            = Cream,
    primaryContainer     = DarkBark,
    onPrimaryContainer   = WarmBlush,

    // Secondary
    secondary            = SoftCoral,
    onSecondary          = Espresso,
    secondaryContainer   = DarkBark,
    onSecondaryContainer = WarmBlush,

    // Backgrounds
    background           = Espresso,
    onBackground         = WarmBlush,

    // Surfaces
    surface              = Espresso,
    onSurface            = WarmBlush,
    surfaceVariant       = DarkBark,
    onSurfaceVariant     = SoftCoral,

    // Outlines
    outline              = Mahogany,
    outlineVariant       = DarkBark,

    // Error
    error                = ErrorRed,
    onError              = OnErrorWhite,
)

@Composable
fun KitchenFreshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = KitchenFreshTypography,
        content     = content
    )
}
