package com.app.melodrop.ui.theme

import androidx.compose.ui.graphics.Color

// Base
val Night = Color(0xFF0C0C10)

val Card = Color(0xFF18181F)

val Outline = Color(0xFF2A2A33)


val TextPrimary = Color(0xFFF2F2F5)

val TextSecondary = Color(0xFF8E8E9A)

val Danger = Color(0xFFF04848)

// Pastels
val Lavender = Color(0xFFB4A7F5)

val Peach = Color(0xFFFFB38A)

val Butter = Color(0xFFFFE08A)

val Mint = Color(0xFF9EE0B8)

val Rose = Color(0xFFF5A8C8)



// Genres: each one always gets the same pastel, on every screen
private val GenrePalette = listOf(Lavender, Peach, Butter, Mint, Rose)

// Same pastel for the same genre name, on every screen (ADR-0017)
fun genreColor(genre: String): Color = GenrePalette[genre.hashCode().mod(GenrePalette.size)]