package com.app.melodrop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    onPrimary = Night,
    background = Night,
    onBackground = TextPrimary,
    surface = Card,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = Outline,
    error = Danger
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