package com.app.melodrop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Orange, // Main accent: buttons, highlights
    onPrimary = Night, // Text and icons on orange
    background = Night, // Screen background
    onBackground = TextPrimary, // Text on the screen background
    surface = Card, // Background of Surface components
    onSurface = TextPrimary, // Text and icons on surfaces
    onSurfaceVariant = TextSecondary, // Secondary text: labels, hints
    outline = Outline, // Borders of outlined fields and buttons
    error = Danger // Errors and destructive actions
)

@Composable
fun MeloDropTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}