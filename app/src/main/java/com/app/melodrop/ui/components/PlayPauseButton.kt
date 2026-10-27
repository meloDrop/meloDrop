package com.app.melodrop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Round lavender button with a play or pause sign drawn by hand
@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 52.dp,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(buttonSize)
            // Read aloud by TalkBack, since the button has no text
            .semantics { contentDescription = if (isPlaying) "Pause" else "Écouter l'extrait" },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
    ) {
        val iconColor = MaterialTheme.colorScheme.onPrimary

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // The sign takes the middle third of the button
                .padding(buttonSize / 3)
        ) {
            if (isPlaying) {
                // Pause: two rounded bars
                val barWidth = size.width * 0.32f
                val corner = CornerRadius(2.dp.toPx())
                drawRoundRect(iconColor, Offset.Zero, Size(barWidth, size.height), corner)
                drawRoundRect(
                    iconColor,
                    Offset(size.width - barWidth, 0f),
                    Size(barWidth, size.height),
                    corner,
                )
            } else {
                // Play: a triangle pointing right, nudged right to look centered
                val triangle = Path().apply {
                    moveTo(size.width * 0.15f, 0f)
                    lineTo(size.width, size.height / 2)
                    lineTo(size.width * 0.15f, size.height)
                    close()
                }
                drawPath(triangle, iconColor)
            }
        }
    }
}
