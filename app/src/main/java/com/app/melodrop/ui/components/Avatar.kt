package com.app.melodrop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
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
import com.app.melodrop.ui.theme.TextPrimary

// One choice of the avatar: its name in the database and its label in the app
data class AvatarChoice(val name: String, val label: String)

// The 5 background colors, same names as the backend (AvatarColor, ADR-0020)
val AVATAR_COLORS = listOf(
    AvatarChoice("lavender", "Lavande"),
    AvatarChoice("peach", "Pêche"),
    AvatarChoice("butter", "Jaune"),
    AvatarChoice("mint", "Menthe"),
    AvatarChoice("rose", "Rose"),
)

// The 7 drop styles, same names as the backend (AvatarStyle, ADR-0020)
val AVATAR_STYLES = listOf(
    AvatarChoice("simple", "Simple"),
    AvatarChoice("rock", "Rock"),
    AvatarChoice("rap", "Rap"),
    AvatarChoice("pop", "Pop"),
    AvatarChoice("classical", "Classique"),
    AvatarChoice("reggae", "Reggae"),
    AvatarChoice("electro", "Électro"),
)

// The three colors of the reggae locks
private val ReggaeRed = Color(0xFFE2574C)
private val ReggaeYellow = Color(0xFFF4C542)
private val ReggaeGreen = Color(0xFF3FA34D)

// Pastel background of an avatar, from the name stored in the database
fun avatarColor(name: String): Color = when (name) {
    "peach" -> Peach
    "butter" -> Butter
    "mint" -> Mint
    "rose" -> Rose
    // "lavender", and any name the app does not know yet
    else -> Lavender
}

// A round avatar: a pastel background with a drop in the middle,
// and the accessory of its style. An unknown style shows the simple drop
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
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Everything below is drawn on a 100 x 100 grid, whatever the size
            // of the avatar: (0, 0) is the top left corner, (100, 100) the bottom right
            scale(scale = size.width / 100f, pivot = Offset.Zero) {
                // The locks hang behind the drop: drawn first
                if (style == "reggae") drawLocks()

                drawPath(dropPath(), Night)

                // No reflection with the bolt: the drop would look too busy
                if (style != "rock") drawReflection(background)

                when (style) {
                    "rock" -> drawBolt(background)
                    "rap" -> drawCap()
                    "pop" -> drawSparkles()
                    "classical" -> drawBowTie()
                    "electro" -> drawHeadphones()
                }
            }
        }
    }
}

// A drop: pointed at the top, round at the bottom, in the middle of the grid
// internal: also used by CoverImage for the sad drop
internal fun dropPath() = Path().apply {
    moveTo(50f, 22f)
    cubicTo(55.24f, 33.2f, 71.84f, 45.52f, 71.84f, 58.96f)
    cubicTo(71.84f, 70.16f, 62.23f, 78f, 50f, 78f)
    cubicTo(37.77f, 78f, 28.16f, 70.16f, 28.16f, 58.96f)
    cubicTo(28.16f, 45.52f, 44.76f, 33.2f, 50f, 22f)
    close()
}

// Thin reflection on the left side of the drop, in the background color
private fun DrawScope.drawReflection(background: Color) {
    drawOval(
        color = background.copy(alpha = 0.55f),
        topLeft = Offset(36.9f, 51.12f),
        size = Size(6.12f, 15.68f),
    )
}

// Rock: a lightning bolt inside the drop
private fun DrawScope.drawBolt(background: Color) {
    val bolt = Path().apply {
        moveTo(56f, 46f)
        lineTo(47f, 62f)
        lineTo(53f, 62f)
        lineTo(50f, 75f)
        lineTo(61f, 57f)
        lineTo(55f, 57f)
        lineTo(59f, 46f)
        close()
    }
    drawPath(bolt, background)
}

// Rap: a white cap, seen from the side, the visor to the right
private fun DrawScope.drawCap() {
    val crown = Path().apply {
        moveTo(35f, 40.5f)
        cubicTo(35f, 24f, 44f, 18f, 51f, 18f)
        cubicTo(59f, 18f, 66f, 25f, 66f, 40.5f)
        close()
    }
    val visor = Path().apply {
        moveTo(61f, 35f)
        lineTo(80f, 36.5f)
        quadraticTo(83.5f, 38.5f, 80f, 40.5f)
        lineTo(61f, 40.5f)
        close()
    }
    drawPath(crown, TextPrimary)
    drawPath(visor, TextPrimary)
    // Small button on top of the cap
    drawCircle(TextPrimary, radius = 2f, center = Offset(51f, 18.5f))
}

// Pop: three shining stars around the drop
private fun DrawScope.drawSparkles() {
    drawStar(24f, 33f, 9f)
    drawStar(77f, 29f, 6f)
    drawStar(79f, 70f, 5f)
}

// A star with 4 thin branches, centered on (x, y)
private fun DrawScope.drawStar(x: Float, y: Float, radius: Float) {
    val star = Path().apply {
        moveTo(x, y - radius)
        quadraticTo(x, y, x + radius, y)
        quadraticTo(x, y, x, y + radius)
        quadraticTo(x, y, x - radius, y)
        quadraticTo(x, y, x, y - radius)
        close()
    }
    drawPath(star, Night)
}

// Classical: a bow tie under the drop
private fun DrawScope.drawBowTie() {
    val leftWing = Path().apply {
        moveTo(50f, 85f)
        lineTo(37f, 78f)
        lineTo(37f, 92f)
        close()
    }
    val rightWing = Path().apply {
        moveTo(50f, 85f)
        lineTo(63f, 78f)
        lineTo(63f, 92f)
        close()
    }
    drawPath(leftWing, Night)
    drawPath(rightWing, Night)
    // The knot in the middle
    drawCircle(Night, radius = 3.6f, center = Offset(50f, 85f))
}

// Reggae: 4 locks falling on each side, with a red, yellow or green bead
private fun DrawScope.drawLocks() {
    drawLock(Offset(45f, 30f), Offset(24f, 40f), Offset(19f, 62f), ReggaeRed)
    drawLock(Offset(47f, 31f), Offset(30f, 48f), Offset(26f, 72f), ReggaeYellow)
    drawLock(Offset(53f, 31f), Offset(70f, 48f), Offset(74f, 72f), ReggaeGreen)
    drawLock(Offset(55f, 30f), Offset(76f, 40f), Offset(81f, 62f), ReggaeRed)
}

// One lock: a curve from the top of the drop (start) to its bead (end)
private fun DrawScope.drawLock(start: Offset, bend: Offset, end: Offset, bead: Color) {
    val lock = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(bend.x, bend.y, end.x, end.y)
    }
    drawPath(lock, Night, style = Stroke(width = 4.6f, cap = StrokeCap.Round))
    drawCircle(bead, radius = 3f, center = Offset(end.x, end.y + 1f))
}

// Electro: headphones, the band above the drop and a cup on each side
private fun DrawScope.drawHeadphones() {
    val band = Path().apply {
        moveTo(22.75f, 51f)
        cubicTo(22.75f, 8f, 77.25f, 8f, 77.25f, 51f)
    }
    drawPath(band, Night, style = Stroke(width = 4f, cap = StrokeCap.Round))
    drawRoundRect(
        color = Night,
        topLeft = Offset(18f, 48f),
        size = Size(9.5f, 19f),
        cornerRadius = CornerRadius(3.5f),
    )
    drawRoundRect(
        color = Night,
        topLeft = Offset(72.5f, 48f),
        size = Size(9.5f, 19f),
        cornerRadius = CornerRadius(3.5f),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun AvatarPreview() {
    MeloDropTheme {
        // One line per style, on the 5 colors
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AVATAR_STYLES.forEach { style ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AVATAR_COLORS.forEach { color ->
                        Avatar(color = color.name, style = style.name, avatarSize = 48.dp)
                    }
                }
            }
        }
    }
}