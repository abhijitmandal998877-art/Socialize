package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Socialize Modern Slate-Midnight Color Palette
val SlatePrimary = Color(0xFF6366F1)       // Vibrant Indigo
val SlatePrimaryVariant = Color(0xFF4F46E5) // Darker Indigo
val SlateSecondary = Color(0xFF3B82F6)     // Electric Blue
val SlateAccent = Color(0xFFEC4899)        // Accent Rose Pink

val Slate900 = Color(0xFF0F172A)           // Very Dark Slate
val Slate800 = Color(0xFF1E293B)           // Card/Surface Slate
val Slate700 = Color(0xFF334155)           // Borders/Secondary Slate
val Slate400 = Color(0xFF94A3B8)           // Secondary grey text
val Slate100 = Color(0xFFF1F5F9)           // White/slate text primary

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = SlatePrimary,
    secondary = SlateSecondary,
    tertiary = SlateAccent,
    background = Slate900,
    surface = Slate800,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Slate100,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate400
)

val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = SlatePrimary,
    secondary = SlateSecondary,
    tertiary = SlateAccent,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Slate700
)
