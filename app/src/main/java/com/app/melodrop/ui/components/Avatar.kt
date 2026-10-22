package com.app.melodrop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Night
import com.app.melodrop.ui.theme.Peach
import com.app.melodrop.ui.theme.Rose

// Pastel background of an avatar, from the name stored in the database
fun avatarColor(name: String): Color = when (name) {
    "peach" -> Peach
    "butter" -> Butter
    "mint" -> Mint
    "rose" -> Rose
    // "lavender", and any name the app does not know yet
    else -> Lavender
}

// A round avatar: a pastel background with a drop in the middle.
// Only the "simple" drop exists for now: the other styles will come later
@Composable
fun Avatar(
    color: String,
    style: String,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 72.dp,
) {
    val background = avatarColor(color)

    Box(
        modifier = modifier
            .size(avatarSize)
            .clip(CircleShape)
            .background(background)
            // Read aloud by TalkBack, since the avatar has no text
            .semantics { contentDescription = "Avatar" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                // The drop takes a bit more than half of the height
                .fillMaxHeight(0.56f)
                .aspectRatio(0.78f)
        ) {
            val w = size.width
            val h = size.height

            // A drop: pointed at the top, round at the bottom
            val drop = Path().apply {
                moveTo(w / 2, 0f)
                cubicTo(w * 0.62f, h * 0.2f, w, h * 0.42f, w, h * 0.66f)
                cubicTo(w, h * 0.86f, w * 0.78f, h, w / 2, h)
                cubicTo(w * 0.22f, h, 0f, h * 0.86f, 0f, h * 0.66f)
                cubicTo(0f, h * 0.42f, w * 0.38f, h * 0.2f, w / 2, 0f)
                close()
            }
            drawPath(drop, Night)

            // Thin reflection on the left side, in the background color
            drawOval(
                color = background.copy(alpha = 0.55f),
                topLeft = Offset(w * 0.2f, h * 0.52f),
                size = Size(w * 0.14f, h * 0.28f),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun AvatarPreview() {
    MeloDropTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            listOf("lavender", "peach", "butter", "mint", "rose").forEach { name ->
                Avatar(color = name, style = "simple")
            }
        }
    }
}
