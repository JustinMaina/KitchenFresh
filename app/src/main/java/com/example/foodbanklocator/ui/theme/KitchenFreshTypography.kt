package com.example.foodbanklocator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.foodbanklocator.R

// -------------------------------------------------------
// Raleway Font Family
// -------------------------------------------------------

val RalewayFontFamily = FontFamily(
    Font(R.font.raleway_semibold, FontWeight.SemiBold),
    Font(R.font.raleway_bold, FontWeight.Bold)
)

// -------------------------------------------------------
// KitchenFresh Typography
// -------------------------------------------------------

/**
 * A clean, readable type scale suited for users who may
 * be in urgent need — clear hierarchy, generous sizing.
 *
 * Raleway is used for the app name / hero title only.
 * All other text uses the system default sans-serif.
 */
val KitchenFreshTypography = Typography(

    // App name / hero title (Landing screen) — Raleway
    displayLarge = TextStyle(
        fontFamily    = RalewayFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = (-0.5).sp
    ),

    // Screen headings
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp,
        lineHeight = 32.sp
    ),

    // Card titles / food bank names
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,
        lineHeight = 28.sp
    ),

    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),

    // Body text — addresses, descriptions
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),

    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),

    // Labels, chips, captions
    labelLarge = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),

    labelSmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    )
)
