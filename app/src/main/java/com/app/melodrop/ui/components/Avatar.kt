package com.app.melodrop.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// One choice of the avatar: its name in the database, its label in the app,
// and true when it needs the subscription once the cosmetics are locked
data class AvatarChoice(val name: String, val label: String, val isPremium: Boolean = false)

// The 5 background colors, same names as the backend (AvatarColor, ADR-0020)
val AVATAR_COLORS = listOf(
    AvatarChoice("lavender", "Lavande"),
    AvatarChoice("peach", "Pêche"),
    AvatarChoice("butter", "Jaune"),
    AvatarChoice("mint", "Menthe"),
    AvatarChoice("rose", "Rose"),
)

// The 9 drop styles, same names as the backend (AvatarStyle)
val AVATAR_STYLES = listOf(
    AvatarChoice("simple", "Simple"),
    AvatarChoice("rock", "Rock"),
    AvatarChoice("rap", "Rap"),
    AvatarChoice("pop", "Pop"),
    AvatarChoice("classical", "Classique"),
    AvatarChoice("reggae", "Reggae"),
    AvatarChoice("electro", "Électro"),
    AvatarChoice("jazz", "Jazz"),
    AvatarChoice("lofi", "Lo-fi"),
)

// The 11 drop colors (AvatarDrop): the free ones first, then the premium ones
val AVATAR_DROPS = listOf(
    AvatarChoice("night", "Nuit"),
    AvatarChoice("cream", "Crème"),
    AvatarChoice("lavender", "Lavande"),
    AvatarChoice("peach", "Pêche"),
    AvatarChoice("butter", "Jaune"),
    AvatarChoice("mint", "Menthe"),
    AvatarChoice("rose", "Rose"),
    AvatarChoice("ocean", "Océan", isPremium = true),
    AvatarChoice("plum", "Prune", isPremium = true),
    AvatarChoice("gold", "Or", isPremium = true),
    AvatarChoice("spectre", "Spectre", isPremium = true),
)

// What is drawn on the background, behind the drop (AvatarDeco)
val AVATAR_DECOS = listOf(
    AvatarChoice("plain", "Uni"),
    AvatarChoice("gradient", "Dégradé"),
    AvatarChoice("waves", "Ondes"),
    AvatarChoice("notes", "Notes"),
)

// Face drawn on the drop (AvatarFace)
val AVATAR_FACES = listOf(
    AvatarChoice("none", "Aucune"),
    AvatarChoice("smile", "Sourire"),
    AvatarChoice("wink", "Clin d'œil"),
    AvatarChoice("shades", "Lunettes"),
    AvatarChoice("closed", "Yeux fermés"),
    AvatarChoice("stars", "Yeux étoiles"),
)

// Animated aura around the avatar, on the profiles (AvatarAura). Every aura is premium
val AVATAR_AURAS = listOf(
    AvatarChoice("none", "Aucune"),
    AvatarChoice("halo", "Halo", isPremium = true),
    AvatarChoice("bolts", "Éclairs", isPremium = true),
    AvatarChoice("chain", "Chaîne en or", isPremium = true),
    AvatarChoice("sparkles", "Paillettes", isPremium = true),
    AvatarChoice("staff", "Portée", isPremium = true),
    AvatarChoice("rasta", "Anneau rasta", isPremium = true),
    AvatarChoice("equalizer", "Égaliseur", isPremium = true),
    AvatarChoice("smoke", "Volutes", isPremium = true),
    AvatarChoice("rain", "Pluie", isPremium = true),
)

// The aura goes past the round avatar: the whole box is 1.44 times bigger
const val AURA_SCALE = 1.44f

// The animations of the auras loop every hour: their own cycles are much shorter
private const val AURA_LOOP_S = 3600f

// Time used for an aura that does not move (small previews)
private const val STILL_TIME = 1.3f

// Colors of the accessories and of the premium drops
private val ReggaeRed = Color(0xFFE2574C)
private val ReggaeYellow = Color(0xFFF4C542)
private val ReggaeGreen = Color(0xFF3FA34D)
private val Gold = Color(0xFFF4C542)
private val GoldLight = Color(0xFFFFF1B8)
private val GoldDark = Color(0xFFC98F1E)
private val GoldSoft = Color(0xFFF7D774)
private val Ocean = Color(0xFF24356B)
private val Plum = Color(0xFF4E2A63)
private val Graphite = Color(0xFF24242E)
private val Steel = Color(0xFF3A3A46)
private val Slate = Color(0xFF5A5A68)
private val Silver = Color(0xFF8E8E9A)
private val Mist = Color(0xFFC9C9D6)

// Stripes of the Spectre drop, in the order of the stripes of the logo
private val SpectreColors = listOf(Lavender, Rose, Peach, Butter, Mint)

// The 5 pastels, for the confetti and the stars of the auras
private val Pastels = listOf(Butter, Lavender, Rose, Mint, Peach)

// Pastel background of an avatar, from the name stored in the database
fun avatarColor(name: String): Color = when (name) {
    "peach" -> Peach
    "butter" -> Butter
    "mint" -> Mint
    "rose" -> Rose
    // "lavender", and any name the app does not know yet
    else -> Lavender
}

// True when the drop color needs the subscription once the cosmetics are locked
fun isPremiumDrop(name: String): Boolean = AVATAR_DROPS.any { it.name == name && it.isPremium }

// True when the aura needs the subscription: all of them but "none"
fun isPremiumAura(name: String): Boolean = name != "none"

// The choices of one avatar, with their default values
private data class AvatarLook(
    val color: String,
    val style: String,
    val drop: String,
    val deco: String,
    val face: String,
)

// A round avatar: a pastel background with a drop in the middle, the accessory of its
// style, and an animated aura around it when showAura is true. An unknown name shows the default.
// drop, deco, face and aura: null when the backend does not send them yet
@Composable
fun Avatar(
    color: String,
    style: String,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 72.dp,
    drop: String? = null,
    deco: String? = null,
    face: String? = null,
    aura: String? = null,
    // True on the profiles, in the user lists, on the Discover cards and in the editor:
    // the aura shows around the avatar
    showAura: Boolean = false,
    // False: the aura stays still (the small previews of the editor)
    animateAura: Boolean = true,
    // True: the box keeps the room of the aura even without one, so the lines of a list
    // and the cards of Discover stay aligned, with or without an aura
    reserveAuraRoom: Boolean = false,
) {
    val look = AvatarLook(
        color = color,
        style = style,
        drop = drop ?: "night",
        deco = deco ?: "plain",
        face = face ?: "none",
    )
    val auraName = aura ?: "none"
    val hasAura = showAura && auraName != "none"

    // With an aura, the box is bigger and the avatar stays in its middle
    Box(
        modifier = modifier.size(if (hasAura || reserveAuraRoom) avatarSize * AURA_SCALE else avatarSize),
        contentAlignment = Alignment.Center,
    ) {
        if (hasAura) {
            AuraLayer(
                aura = auraName,
                background = avatarColor(color),
                animate = animateAura,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                // Read aloud by TalkBack, since the avatar has no text
                .semantics { contentDescription = "Avatar" },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Everything below is drawn on a 100 x 100 grid, whatever the size
                // of the avatar: (0, 0) is the top left corner, (100, 100) the bottom right
                scale(scale = size.width / 100f, pivot = Offset.Zero) {
                    drawAvatar(look)
                }
            }
        }
    }
}

// ---------- The round avatar ----------

private fun DrawScope.drawAvatar(look: AvatarLook) {
    val background = avatarColor(look.color)
    drawBackground(look, background)
    // The locks hang behind the drop: drawn first
    if (look.style == "reggae") drawLocks()
    drawDrop(look, background)
    drawAccessory(look, background)
    drawFace(look, background)
}

// Pastel background, plain or with a decoration
private fun DrawScope.drawBackground(look: AvatarLook, background: Color) {
    if (look.deco == "gradient") {
        // From the chosen pastel to the one two steps further in the list
        val index = AVATAR_COLORS.indexOfFirst { it.name == look.color }.coerceAtLeast(0)
        val other = avatarColor(AVATAR_COLORS[(index + 2) % AVATAR_COLORS.size].name)
        drawRect(
            brush = Brush.linearGradient(listOf(background, other), start = Offset.Zero, end = Offset(100f, 100f)),
            topLeft = Offset.Zero,
            size = Size(100f, 100f),
        )
    } else {
        drawRect(background, topLeft = Offset.Zero, size = Size(100f, 100f))
    }

    when (look.deco) {
        // Three waves under the drop, like water seen from the side
        "waves" -> listOf(18f, 29f, 40f).forEachIndexed { index, radius ->
            drawOval(
                color = Color.White.copy(alpha = 0.6f - index * 0.15f),
                topLeft = Offset(50f - radius, 80f - radius * 0.32f),
                size = Size(radius * 2f, radius * 0.64f),
                style = Stroke(width = 1.8f),
            )
        }
        // Three music notes, see-through
        "notes" -> {
            drawNote(16f, 32f)
            drawNote(83f, 80f)
            drawNote(19f, 84f)
        }
    }
}

// A small see-through music note of the background, its head on (x, y)
private fun DrawScope.drawNote(x: Float, y: Float) {
    val color = Night.copy(alpha = 0.28f)
    withTransform({ rotate(-20f, pivot = Offset(x, y)) }) {
        drawOval(color, topLeft = Offset(x - 3.2f, y - 2.4f), size = Size(6.4f, 4.8f))
    }
    val stem = Path().apply {
        moveTo(x + 2.8f, y)
        lineTo(x + 2.8f, y - 11f)
        quadraticTo(x + 6f, y - 9f, x + 7f, y - 6f)
    }
    drawPath(stem, color, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
}

// The drop: its color, some volume, reflections, and an outline on the light colors
private fun DrawScope.drawDrop(look: AvatarLook, background: Color) {
    val dark = isDarkDrop(look.drop)

    // Soft shadow on the background, under the drop
    drawOval(Night.copy(alpha = 0.22f), topLeft = Offset(35f, 77.8f), size = Size(30f, 5.6f))

    when (look.drop) {
        // The stripes of the logo, cut to the shape of the drop
        "spectre" -> clipPath(DropPath) {
            SpectreColors.forEachIndexed { index, color ->
                drawRect(color, topLeft = Offset(20f, 22f + index * 11.2f), size = Size(60f, 11.5f))
            }
        }
        // A golden gradient, light at the top left
        "gold" -> drawPath(
            DropPath,
            Brush.linearGradient(
                0f to GoldLight, 0.38f to Gold, 0.72f to GoldDark, 1f to GoldSoft,
                start = Offset(28.16f, 22f),
                end = Offset(71.84f, 78f),
            ),
        )
        else -> drawPath(DropPath, dropColor(look.drop))
    }

    // Volume: lighter at the top left, darker on the edges
    drawPath(
        DropPath,
        Brush.radialGradient(
            0f to Color.White.copy(alpha = if (dark) 0.2f else 0.35f),
            0.5f to Color.White.copy(alpha = 0f),
            0.78f to Color.Black.copy(alpha = 0f),
            1f to Color.Black.copy(alpha = if (dark) 0.3f else 0.18f),
            center = Offset(43.9f, 43.3f),
            radius = 40f,
        ),
    )

    // Light of the background, bounced on the bottom right of the drop
    drawPath(
        RimPath,
        color = (if (dark) background else Color.White).copy(alpha = 0.6f),
        style = Stroke(width = 1.7f, cap = StrokeCap.Round),
    )

    // Shiny reflection on the left. Not with the bolt of the rock style, it would look too busy
    if (look.style != "rock" || look.face != "none") {
        drawPath(CrescentPath, Color.White.copy(alpha = if (dark) 0.55f else 0.8f))
    }
    drawCircle(Color.White.copy(alpha = 0.85f), radius = 1.6f, center = Offset(55.4f, 38.6f))
    drawCircle(Color.White.copy(alpha = 0.7f), radius = 0.8f, center = Offset(57.6f, 42.4f))

    // Outline: thick on the Spectre drop, whose stripes have the colors of the backgrounds
    if (look.drop == "spectre") {
        drawPath(DropPath, Night, style = Stroke(width = 2.8f, join = StrokeJoin.Round))
        drawPath(InnerDropPath, Color.White.copy(alpha = 0.55f), style = Stroke(width = 0.8f))
    } else if (!dark) {
        drawPath(DropPath, Night, style = Stroke(width = 1.8f, join = StrokeJoin.Round))
    }
}

// Dark drops: no outline, and the face and the reflections take the background color
private fun isDarkDrop(name: String): Boolean = name == "night" || name == "ocean" || name == "plum"

// Plain color of a drop. Gold and Spectre are drawn apart
private fun dropColor(name: String): Color = when (name) {
    "cream" -> TextPrimary
    "lavender" -> Lavender
    "peach" -> Peach
    "butter" -> Butter
    "mint" -> Mint
    "rose" -> Rose
    "ocean" -> Ocean
    "plum" -> Plum
    // "night", and any name the app does not know yet
    else -> Night
}

// ---------- Accessories ----------

private fun DrawScope.drawAccessory(look: AvatarLook, background: Color) {
    when (look.style) {
        "rock" -> drawRock(hasFace = look.face != "none")
        "rap" -> drawRap(background)
        "pop" -> drawPop()
        "classical" -> drawClassical()
        "reggae" -> drawTam()
        "electro" -> drawHeadphones()
        "jazz" -> drawMicrophone()
        "lofi" -> drawBeanie()
    }
}

// Fills a shape, then draws its dark outline
private fun DrawScope.drawOutlined(path: Path, fill: Color, width: Float) {
    drawPath(path, fill)
    drawPath(path, Night, style = Stroke(width = width, join = StrokeJoin.Round))
}

// Rock: a yellow bolt inside the drop. With a face, two bolts around the drop instead
private fun DrawScope.drawRock(hasFace: Boolean) {
    if (hasFace) {
        drawOutlined(BoltRightPath, Butter, 1f)
        drawOutlined(BoltLeftPath, Butter, 0.9f)
    } else {
        drawOutlined(BoltPath, Butter, 1.3f)
        drawLine(
            Color.White.copy(alpha = 0.8f),
            start = Offset(55.4f, 48f),
            end = Offset(49.2f, 60.2f),
            strokeWidth = 0.9f,
            cap = StrokeCap.Round,
        )
    }
}

// Rap: a gold chain with a drop pendant, and a cap with its seams and a small logo
private fun DrawScope.drawRap(background: Color) {
    // 13 beads on a curve from (37, 66) to (63, 66), pulled down to (50, 80)
    for (index in 0..12) {
        val t = index / 12f
        val x = (1 - t) * (1 - t) * 37f + 2 * (1 - t) * t * 50f + t * t * 63f
        val y = (1 - t) * (1 - t) * 66f + 2 * (1 - t) * t * 80f + t * t * 66f
        drawCircle(Gold, radius = 1.25f, center = Offset(x, y))
        drawCircle(Night, radius = 1.25f, center = Offset(x, y), style = Stroke(width = 0.4f))
    }
    drawMiniDrop(Offset(50f, 76.6f), 0.85f, Gold, 0.6f)

    drawOutlined(CapCrownPath, TextPrimary, 1.2f)
    drawPath(CapSidePath, Color(0xFFD9D9E3))
    drawPath(CapSeamsPath, Color(0xFFB8B8C6), style = Stroke(width = 0.9f))
    drawRect(Color(0xFFCFCFDA), topLeft = Offset(35.6f, 37.6f), size = Size(30f, 2.6f))
    drawMiniDrop(Offset(55.6f, 30f), 1.15f, background, 0.5f)
    drawOutlined(CapVisorPath, TextPrimary, 1.2f)
    drawPath(CapVisorShadePath, Color(0xFFB8B8C6))
    drawCircle(TextPrimary, radius = 2.2f, center = Offset(51f, 18.4f))
    drawCircle(Night, radius = 2.2f, center = Offset(51f, 18.4f), style = Stroke(width = 0.9f))
}

// Pop: three shining pastel stars and some small dots around the drop
private fun DrawScope.drawPop() {
    listOf(
        Triple(Offset(24f, 33f), 9f, Rose),
        Triple(Offset(77f, 29f), 6f, Butter),
        Triple(Offset(79f, 70f), 5f, Lavender),
    ).forEach { (center, radius, color) ->
        drawOutlined(starPath(center.x, center.y, radius), color, 0.8f)
        drawPath(starPath(center.x, center.y, radius * 0.42f), Color.White)
    }
    listOf(
        Triple(Offset(31f, 20f), 1.4f, Mint),
        Triple(Offset(85f, 47f), 1.2f, Rose),
        Triple(Offset(18f, 52f), 1.3f, Butter),
        Triple(Offset(70f, 16f), 1f, Lavender),
    ).forEach { (center, radius, color) ->
        drawCircle(color, radius = radius, center = center)
        drawCircle(Night, radius = radius, center = center, style = Stroke(width = 0.5f))
    }
}

// Classical: a shirt collar and a bow tie with its folds
private fun DrawScope.drawClassical() {
    drawOutlined(CollarLeftPath, TextPrimary, 0.8f)
    drawOutlined(CollarRightPath, TextPrimary, 0.8f)
    drawPath(BowLeftPath, Night)
    drawPath(BowRightPath, Night)
    drawPath(BowFoldsPath, Color(0xFF4A4A58), style = Stroke(width = 1f, cap = StrokeCap.Round))
    val knot = Path().apply {
        addRoundRect(RoundRect(46.6f, 81.4f, 53.4f, 88.6f, CornerRadius(2f)))
    }
    drawOutlined(knot, Graphite, 0.8f)
    drawLine(Slate, Offset(48f, 83f), Offset(48f, 87f), strokeWidth = 0.9f, cap = StrokeCap.Round)
}

// Reggae, behind the drop: 4 textured locks with a red, yellow or green bead
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
    drawPath(lock, Night, style = Stroke(width = 5.4f, cap = StrokeCap.Round))
    // Small bumps along the lock
    drawPath(
        lock,
        Steel,
        style = Stroke(
            width = 3f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.6f, 2.2f)),
        ),
    )
    val beadCenter = Offset(end.x, end.y + 1.5f)
    drawCircle(bead, radius = 3.3f, center = beadCenter)
    drawCircle(Night, radius = 3.3f, center = beadCenter, style = Stroke(width = 0.8f))
    drawCircle(Color.White.copy(alpha = 0.7f), radius = 0.9f, center = Offset(end.x - 1f, end.y + 0.5f))
}

// Reggae, over the drop: a knitted tam with red, yellow and green stripes
private fun DrawScope.drawTam() {
    clipPath(TamPath) {
        drawRect(ReggaeRed, topLeft = Offset(25f, 10f), size = Size(50f, 11.5f))
        drawRect(ReggaeYellow, topLeft = Offset(25f, 21.5f), size = Size(50f, 8f))
        drawRect(ReggaeGreen, topLeft = Offset(25f, 29.5f), size = Size(50f, 12f))
        // The knit: thin lines from top to bottom
        var x = 34f
        while (x <= 68f) {
            drawLine(Night.copy(alpha = 0.14f), Offset(x, 12f), Offset(x + 1f, 40f), strokeWidth = 0.8f)
            x += 3.4f
        }
    }
    drawPath(TamPath, Night, style = Stroke(width = 1.2f, join = StrokeJoin.Round))
    drawPath(TamBandPath, Night.copy(alpha = 0.35f), style = Stroke(width = 1f))
}

// Electro: headphones with a padded band, cushions and a mint light on each cup
private fun DrawScope.drawHeadphones() {
    drawPath(HeadbandPath, Night, style = Stroke(width = 5f, cap = StrokeCap.Round))
    drawPath(HeadbandInnerPath, Steel, style = Stroke(width = 1.2f, cap = StrokeCap.Round))
    drawPath(HeadbandPadPath, Graphite, style = Stroke(width = 6.5f, cap = StrokeCap.Round))
    drawPath(
        HeadbandStitchPath,
        Slate,
        style = Stroke(width = 0.7f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.4f, 1.4f))),
    )
    drawRoundRect(Silver, topLeft = Offset(21.6f, 43.5f), size = Size(2.4f, 6f), cornerRadius = CornerRadius(0.8f))
    drawRoundRect(Silver, topLeft = Offset(76f, 43.5f), size = Size(2.4f, 6f), cornerRadius = CornerRadius(0.8f))
    drawCup(cupX = 18f, cushionX = 24.6f, lightX = 18.9f)
    drawCup(cupX = 72.5f, cushionX = 71.8f, lightX = 79.3f)
}

// One cup of the headphones: the shell, the cushion against the drop, and the light outside
private fun DrawScope.drawCup(cupX: Float, cushionX: Float, lightX: Float) {
    val cup = Path().apply {
        addRoundRect(RoundRect(cupX, 48f, cupX + 9.5f, 67f, CornerRadius(3.5f)))
    }
    drawOutlined(cup, Color(0xFF1E1E26), 1f)
    drawRoundRect(Steel, topLeft = Offset(cushionX, 49.6f), size = Size(3.6f, 15.8f), cornerRadius = CornerRadius(1.8f))
    drawRoundRect(
        Mint.copy(alpha = 0.95f),
        topLeft = Offset(lightX, 52f),
        size = Size(1.8f, 11f),
        cornerRadius = CornerRadius(0.9f),
    )
}

// Jazz: a vintage microphone on its stand, on the left of the drop
private fun DrawScope.drawMicrophone() {
    // The stand and its foot
    drawLine(Night, Offset(24f, 66f), Offset(24f, 92f), strokeWidth = 3.4f, cap = StrokeCap.Round)
    drawLine(Silver, Offset(24f, 66f), Offset(24f, 92f), strokeWidth = 1.6f, cap = StrokeCap.Round)
    drawLine(Night, Offset(17f, 92.6f), Offset(31f, 92.6f), strokeWidth = 3f, cap = StrokeCap.Round)
    // The fork that holds the microphone
    drawPath(MicYokePath, Night, style = Stroke(width = 1.6f))
    // The microphone, with its grille and a yellow band
    val capsule = Path().apply {
        addRoundRect(RoundRect(17.4f, 40f, 30.6f, 59f, CornerRadius(6.6f)))
    }
    drawOutlined(capsule, Mist, 1.1f)
    listOf(44f, 47f, 50f, 53f, 56f).forEach { y ->
        drawLine(Color(0xFF7A7A88), Offset(19.2f, y), Offset(28.8f, y), strokeWidth = 0.9f, cap = StrokeCap.Round)
    }
    val band = Path().apply {
        addRect(Rect(17.4f, 48.6f, 30.6f, 51.2f))
    }
    drawOutlined(band, Butter, 0.6f)
    drawLine(
        Color.White.copy(alpha = 0.7f),
        Offset(19.6f, 42.6f),
        Offset(19.6f, 55.6f),
        strokeWidth = 1f,
        cap = StrokeCap.Round,
    )
}

// Lo-fi: a peach beanie with ribs on its cuff and a fluffy pompom
private fun DrawScope.drawBeanie() {
    drawOutlined(BeanieDomePath, Peach, 1.1f)
    drawPath(BeanieKnitPath, Color(0xFFE08E66), style = Stroke(width = 0.9f))
    val cuff = Path().apply {
        addRoundRect(RoundRect(32.5f, 34.5f, 67.5f, 41.5f, CornerRadius(3.2f)))
    }
    drawPath(cuff, Color(0xFFF6A27A))
    clipPath(cuff) {
        var x = 34.2f
        while (x < 67f) {
            drawLine(Color(0xFFD3805C), Offset(x, 34.5f), Offset(x, 41.5f), strokeWidth = 0.8f)
            x += 2.5f
        }
    }
    drawPath(cuff, Night, style = Stroke(width = 1.1f))
    drawCircle(Color(0xFFFFCDB0), radius = 4.8f, center = Offset(50f, 20.6f))
    drawCircle(Night, radius = 4.8f, center = Offset(50f, 20.6f), style = Stroke(width = 1f))
    // Small curls on the pompom
    listOf(Offset(48f, 19.4f), Offset(51.6f, 19f), Offset(49.4f, 22f), Offset(52.2f, 21.8f), Offset(50.4f, 17.6f))
        .forEach { point ->
            val curl = Path().apply {
                moveTo(point.x - 1f, point.y)
                quadraticTo(point.x, point.y - 1.2f, point.x + 1f, point.y)
            }
            drawPath(curl, Color(0xFFE8A07C), style = Stroke(width = 0.7f, cap = StrokeCap.Round))
        }
}

// A small drop (pendant, logo of the cap), centered on center and enlarged by zoom
private fun DrawScope.drawMiniDrop(center: Offset, zoom: Float, fill: Color, outline: Float) {
    withTransform({
        translate(center.x, center.y)
        scale(zoom, zoom, pivot = Offset.Zero)
    }) {
        drawOutlined(MiniDropPath, fill, outline)
    }
}

// A star with 4 thin branches, centered on (x, y)
private fun starPath(x: Float, y: Float, radius: Float) = Path().apply {
    moveTo(x, y - radius)
    quadraticTo(x, y, x + radius, y)
    quadraticTo(x, y, x, y + radius)
    quadraticTo(x, y, x - radius, y)
    quadraticTo(x, y, x, y - radius)
    close()
}

// ---------- Face ----------

// Eyes and mouth: in the background color on a dark drop, dark on a light one
private fun DrawScope.drawFace(look: AvatarLook, background: Color) {
    if (look.face == "none") return
    val color = if (isDarkDrop(look.drop)) background else Night
    val left = Offset(45.5f, 57f)
    val right = Offset(54.5f, 57f)
    val line = Stroke(width = 1.6f, cap = StrokeCap.Round)

    when (look.face) {
        "smile" -> {
            drawCircle(color, radius = 2.3f, center = left)
            drawCircle(color, radius = 2.3f, center = right)
            drawPath(SmilePath, color, style = line)
        }
        "wink" -> {
            drawPath(WinkPath, color, style = line)
            drawCircle(color, radius = 2.3f, center = right)
            drawPath(SmilePath, color, style = line)
        }
        "closed" -> {
            drawPath(ClosedEyesPath, color, style = line)
            drawPath(SmilePath, color, style = line)
        }
        "stars" -> {
            drawPath(starPath(left.x, left.y, 3.4f), color)
            drawPath(starPath(right.x, right.y, 3.4f), color)
            drawPath(OpenMouthPath, color)
        }
        "shades" -> {
            val lens = if (isDarkDrop(look.drop)) Steel else Night
            listOf(41.2f, 50.8f).forEach { x ->
                drawRoundRect(lens, topLeft = Offset(x, 54.2f), size = Size(8f, 5.4f), cornerRadius = CornerRadius(2.2f))
                drawRoundRect(
                    color,
                    topLeft = Offset(x, 54.2f),
                    size = Size(8f, 5.4f),
                    cornerRadius = CornerRadius(2.2f),
                    style = Stroke(width = 1.1f),
                )
            }
            drawLine(color, Offset(49.2f, 55.6f), Offset(50.8f, 55.6f), strokeWidth = 1.1f, cap = StrokeCap.Round)
            drawPath(CoolMouthPath, color, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
        }
    }
}

// ---------- Auras ----------

// The aura around the avatar. Its canvas is 144 x 144 on the grid of the avatar:
// from -22 to 122, so the round avatar (0 to 100) sits in its middle
@Composable
private fun AuraLayer(aura: String, background: Color, animate: Boolean, modifier: Modifier) {
    // Time in seconds, read while drawing: only the canvas is redrawn, not the screen
    val time: State<Float> = if (animate) {
        val transition = rememberInfiniteTransition(label = "aura")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = AURA_LOOP_S,
            animationSpec = infiniteRepeatable(tween((AURA_LOOP_S * 1000).toInt(), easing = LinearEasing)),
            label = "aura time",
        )
    } else {
        remember { mutableFloatStateOf(STILL_TIME) }
    }

    Canvas(modifier = modifier) {
        val unit = size.width / 144f
        withTransform({
            scale(unit, unit, pivot = Offset.Zero)
            translate(22f, 22f)
        }) {
            drawAura(aura, background, time.value)
        }
    }
}

private val Center = Offset(50f, 50f)

private fun DrawScope.drawAura(aura: String, background: Color, time: Float) {
    when (aura) {
        "halo" -> drawHalo(background, time)
        "bolts" -> drawBolts(time)
        "chain" -> drawChain(time)
        "sparkles" -> drawSparkles(time)
        "staff" -> drawStaff(time)
        "rasta" -> drawRasta(time)
        "equalizer" -> drawEqualizer(time)
        "smoke" -> drawSmoke(time)
        "rain" -> drawRain(time)
    }
}

// Where an animation of the given period is, from 0 to 1. offset: its start (s)
private fun cycle(time: Float, period: Float, offset: Float = 0f): Float = ((time - offset) / period).mod(1f)

// Value between the steps of an animation, spread evenly over the cycle (progress from 0 to 1),
// or at the given times
private fun steps(progress: Float, values: FloatArray, times: FloatArray? = null): Float {
    val at = times ?: FloatArray(values.size) { it / (values.size - 1f) }
    for (index in 1 until values.size) {
        if (progress <= at[index]) {
            val part = (progress - at[index - 1]) / (at[index] - at[index - 1])
            return values[index - 1] + (values[index] - values[index - 1]) * part
        }
    }
    return values.last()
}

// Point at this angle (degrees, 0 = right, clockwise) and this distance from the middle
private fun around(angle: Float, radius: Float): Offset {
    val radians = angle * PI.toFloat() / 180f
    return Offset(50f + radius * cos(radians), 50f + radius * sin(radians))
}

// Square around the middle of the avatar, for the arcs
private fun ring(radius: Float) = Rect(Center, radius)

// Simple: a breathing glow, a reflection that goes around, and a crown of dew
private fun DrawScope.drawHalo(background: Color, time: Float) {
    // Grows from 66 to 76 and back, every 3 s
    val radius = 71f - 5f * cos(2f * PI.toFloat() * time / 3f)
    drawCircle(
        Brush.radialGradient(
            0.66f to background.copy(alpha = 0.9f),
            1f to background.copy(alpha = 0f),
            center = Center,
            radius = radius,
        ),
        radius = radius,
        center = Center,
    )
    drawCircle(Color.White.copy(alpha = 0.35f), radius = 57.5f, center = Center, style = Stroke(width = 1f))

    val spin = 360f * cycle(time, 4f)
    val rect = ring(57.5f)
    drawArc(
        Color.White.copy(alpha = 0.9f), spin, 45.8f, false,
        topLeft = rect.topLeft, size = rect.size,
        style = Stroke(width = 2.4f, cap = StrokeCap.Round),
    )
    drawArc(
        Color.White.copy(alpha = 0.6f), spin + 179f, 14f, false,
        topLeft = rect.topLeft, size = rect.size,
        style = Stroke(width = 1.4f, cap = StrokeCap.Round),
    )

    // 12 drops of dew turning the other way, each one twinkling at its own pace
    val back = -360f * cycle(time, 20f)
    HaloDew.forEachIndexed { index, dew ->
        val point = around(index * 30f + back, 64.5f)
        val twinkle = 1f - abs(2f * cycle(time, dew.period, dew.offset) - 1f)
        withTransform({
            translate(point.x, point.y)
            rotate(back, pivot = Offset.Zero)
            scale(dew.size, dew.size, pivot = Offset.Zero)
        }) {
            drawPath(MiniDropPath, Color.White.copy(alpha = 0.15f + 0.85f * twinkle))
        }
    }
}

// Rock: two electric rings that crackle, bolts that strike, sparks that fly away
private fun DrawScope.drawBolts(time: Float) {
    val jitter = steps(cycle(time, 0.6f), floatArrayOf(0f, 5f, -4f, 2f, 0f))
    withTransform({ rotate(jitter, pivot = Center) }) {
        val first = steps(cycle(time, 0.9f), floatArrayOf(1f, 0.2f, 0.9f, 0f, 1f))
        drawPath(ZigzagPathA, Butter.copy(alpha = first), style = Stroke(width = 1.7f, join = StrokeJoin.Round))
        val second = steps(cycle(time, 0.9f, 0.45f), floatArrayOf(0f, 0.9f, 0.1f, 1f, 0f))
        drawPath(ZigzagPathB, Color.White.copy(alpha = second), style = Stroke(width = 1.1f, join = StrokeJoin.Round))
    }

    listOf(-70f, 0f, 70f, 140f, 210f, 280f).forEachIndexed { index, angle ->
        val alpha = steps(
            cycle(time, 1.8f, index * 0.3f),
            floatArrayOf(0f, 1f, 0.15f, 1f, 0f, 0f),
            floatArrayOf(0f, 0.06f, 0.12f, 0.2f, 0.34f, 1f),
        )
        val point = around(angle, 66f)
        withTransform({
            translate(point.x, point.y)
            rotate(angle + 90f, pivot = Offset.Zero)
            scale(1.9f, 1.9f, pivot = Offset.Zero)
        }) {
            drawPath(SmallBoltPath, Butter.copy(alpha = alpha))
            drawPath(SmallBoltPath, Night.copy(alpha = alpha), style = Stroke(width = 0.5f, join = StrokeJoin.Round))
        }
    }

    BoltSparks.forEachIndexed { index, spark ->
        val progress = cycle(time, 0.7f, spark.offset)
        val from = around(spark.angle, 59f)
        val to = around(spark.endAngle, 73f)
        val point = Offset(from.x + (to.x - from.x) * progress, from.y + (to.y - from.y) * progress)
        val color = if (index % 2 == 1) Peach else Color.White
        drawCircle(color.copy(alpha = 1f - progress), radius = 1.3f, center = point)
    }
}

// Rap: a gold chain turning slowly, beads and rings one after the other
private fun DrawScope.drawChain(time: Float) {
    val spin = 360f * cycle(time, 14f)
    for (index in 0 until 22) {
        val angle = index * 360f / 22f + spin
        val point = around(angle, 58f)
        if (index % 2 == 1) {
            drawCircle(Gold.copy(alpha = 0.85f), radius = 2.2f, center = point)
        } else {
            withTransform({ rotate(angle + 90f, pivot = point) }) {
                drawOval(
                    Gold,
                    topLeft = Offset(point.x - 3.4f, point.y - 2.4f),
                    size = Size(6.8f, 4.8f),
                    style = Stroke(width = 1.6f),
                )
            }
        }
    }
}

// Pop: confetti one way, twinkling stars the other way, and flashes in the corners
private fun DrawScope.drawSparkles(time: Float) {
    val back = -360f * cycle(time, 10f)
    SparkleConfetti.forEachIndexed { index, tilt ->
        val angle = index * 360f / 14f + back
        val point = around(angle, 55.5f)
        withTransform({ rotate(index * 360f / 14f + tilt + back, pivot = point) }) {
            drawRoundRect(
                Pastels[index % Pastels.size],
                topLeft = Offset(point.x - 1.2f, point.y - 2.6f),
                size = Size(2.4f, 5.2f),
                cornerRadius = CornerRadius(0.8f),
            )
        }
    }

    val spin = 360f * cycle(time, 14f)
    SparkleStars.forEachIndexed { index, star ->
        val point = around(star.angle + spin, 64f)
        val alpha = steps(cycle(time, star.period, index * 0.25f), floatArrayOf(1f, 0.3f, 1f))
        drawPath(starPath(point.x, point.y, star.size), Pastels[index % Pastels.size].copy(alpha = alpha))
    }

    SparkleFlashes.forEachIndexed { index, flash ->
        val alpha = steps(
            cycle(time, 2.4f, index * 0.48f),
            floatArrayOf(0f, 1f, 0f, 0f),
            floatArrayOf(0f, 0.15f, 0.35f, 1f),
        )
        drawPath(starPath(flash.x, flash.y, flash.size), Color.White.copy(alpha = alpha))
    }
}

// Classical: a staff of 5 lines around the avatar, with notes riding on it
private fun DrawScope.drawStaff(time: Float) {
    listOf(55.5f, 58f, 60.5f, 63f, 65.5f).forEach { radius ->
        drawCircle(TextPrimary.copy(alpha = 0.3f), radius = radius, center = Center, style = Stroke(width = 0.6f))
    }
    val spin = 360f * cycle(time, 18f)
    StaffNotes.forEachIndexed { index, note ->
        val point = around(note.angle + spin, note.radius)
        val color = if (index % 2 == 1) Mint else TextPrimary
        // The notes stay upright while they go around
        withTransform({ translate(point.x, point.y) }) {
            drawNoteGlyph(note.kind, color)
        }
    }
}

// A note of the staff, its head on (0, 0): 'q' quarter, 'e' eighth, 'b' two beamed eighths
private fun DrawScope.drawNoteGlyph(kind: Char, color: Color) {
    val stem = Stroke(width = 1.1f, cap = StrokeCap.Round)
    if (kind == 'b') {
        listOf(Offset(-3.5f, 1f), Offset(3.5f, -1f)).forEach { head ->
            withTransform({ rotate(-20f, pivot = head) }) {
                drawOval(color, topLeft = Offset(head.x - 2.5f, head.y - 1.8f), size = Size(5f, 3.6f))
            }
        }
        drawLine(color, Offset(-1.4f, 0.4f), Offset(-1.4f, -8f), strokeWidth = 1.1f)
        drawLine(color, Offset(5.6f, -1.6f), Offset(5.6f, -10f), strokeWidth = 1.1f)
        drawLine(color, Offset(-1.4f, -8f), Offset(5.6f, -10f), strokeWidth = 2.2f)
        return
    }
    withTransform({ rotate(-20f, pivot = Offset.Zero) }) {
        drawOval(color, topLeft = Offset(-2.7f, -1.9f), size = Size(5.4f, 3.8f))
    }
    drawLine(color, Offset(2.3f, -0.6f), Offset(2.3f, -9f), strokeWidth = 1.1f)
    if (kind == 'e') {
        val flag = Path().apply {
            moveTo(2.3f, -9f)
            quadraticTo(5.6f, -7f, 6.2f, -3.6f)
        }
        drawPath(flag, color, style = stem)
    }
}

// Reggae: a red, yellow and green ring turning
private fun DrawScope.drawRasta(time: Float) {
    val spin = 360f * cycle(time, 8f)
    val rect = ring(59f)
    val colors = listOf(ReggaeRed, ReggaeYellow, ReggaeGreen)
    for (index in 0 until 6) {
        drawArc(
            colors[index % 3], spin + index * 60f, 60f, false,
            topLeft = rect.topLeft, size = rect.size,
            style = Stroke(width = 5f),
        )
    }
}

// Electro: 28 bars around the avatar, going up and down like an equalizer
private fun DrawScope.drawEqualizer(time: Float) {
    val colors = listOf(Lavender, Mint, Rose)
    EqualizerBars.forEachIndexed { index, bar ->
        val angle = index * 360f / EqualizerBars.size
        val radians = angle * PI.toFloat() / 180f
        // From 6 to 19 past the round avatar
        val length = steps(cycle(time, bar.period), bar.heights)
        val direction = Offset(sin(radians), -cos(radians))
        drawLine(
            colors[(index / (EqualizerBars.size / 3)) % 3],
            start = Center + direction * 54f,
            end = Center + direction * (50f + length),
            strokeWidth = 2.6f,
            cap = StrokeCap.Round,
        )
    }
}

// Jazz: two waving rings of smoke, turning in opposite ways
private fun DrawScope.drawSmoke(time: Float) {
    withTransform({ rotate(360f * cycle(time, 16f), pivot = Center) }) {
        drawPath(SmokePathA, Peach.copy(alpha = 0.85f), style = Stroke(width = 2f))
    }
    withTransform({ rotate(-360f * cycle(time, 22f), pivot = Center) }) {
        drawPath(SmokePathB, Peach.copy(alpha = 0.45f), style = Stroke(width = 1.2f))
    }
}

// Lo-fi: small drops falling all around, behind the avatar
private fun DrawScope.drawRain(time: Float) {
    val colors = listOf(Lavender, Mint, Rose)
    clipPath(RainClipPath) {
        RainDrops.forEachIndexed { index, drop ->
            val y = -26f + 152f * cycle(time, drop.period, drop.offset)
            withTransform({ translate(drop.x, y) }) {
                drawPath(MiniDropPath, colors[index % 3].copy(alpha = 0.75f))
            }
        }
    }
}

// ---------- Shapes, on the 100 x 100 grid ----------

// Turns an SVG path text into a Compose Path (same numbers as the mockups)
private fun svgPath(data: String): Path = PathParser().parsePathString(data).toPath()

private const val DROP_DATA =
    "M50 22 C55.24 33.2 71.84 45.52 71.84 58.96 C71.84 70.16 62.23 78 50 78 " +
        "C37.77 78 28.16 70.16 28.16 58.96 C28.16 45.52 44.76 33.2 50 22 Z"

// A drop: pointed at the top, round at the bottom, in the middle of the grid
// internal: also used by CoverImage for the sad drop
internal fun dropPath() = svgPath(DROP_DATA)

private val DropPath = svgPath(DROP_DATA)
private val InnerDropPath = svgPath(
    "M50 25 C54.7 35 69.3 46.3 69.3 58.96 C69.3 68.8 60.9 75.5 50 75.5 " +
        "C39.1 75.5 30.7 68.8 30.7 58.96 C30.7 46.3 45.3 35 50 25 Z",
)
private val RimPath = svgPath("M66.6 63.6 C65 70.4 60.4 74.6 54.4 75.8")
private val CrescentPath = svgPath("M36.6 46.6 C33.7 52 33.9 58.7 36.9 64 C36.3 58.3 36.9 52.6 39.6 47.6 C38.7 46.3 37.5 46 36.6 46.6 Z")
// Drop 1 pixel tall around 0, for the pendant, the logo, the dew and the rain
private val MiniDropPath = svgPath("M0 -4 C0.8 -2 2.4 -0.4 2.4 1.4 C2.4 3 1.3 4 0 4 C-1.3 4 -2.4 3 -2.4 1.4 C-2.4 -0.4 -0.8 -2 0 -4 Z")

private val BoltPath = svgPath("M56 46 L47 62 L53 62 L50 75 L61 57 L55 57 L59 46 Z")
private val BoltRightPath = svgPath("M80 22 L74.6 31.6 L78.2 31.6 L76.4 39.4 L83 28.6 L79.4 28.6 L81.8 22 Z")
private val BoltLeftPath = svgPath("M22 28 L18.5 34.5 L21 34.5 L19.8 39.8 L24.3 32.6 L21.8 32.6 L23.6 28 Z")
private val SmallBoltPath = svgPath("M1.5 -6 L-2 1 L0.6 1 L-1 6.5 L3 -0.5 L0.4 -0.5 L2.4 -6 Z")

private val CapCrownPath = svgPath("M35 40.5 C35 24 44 18 51 18 C59 18 66 25 66 40.5 Z")
private val CapSidePath = svgPath("M35.6 40 C35.6 26.5 41 20.4 46.4 18.8 C42.4 24.4 41.4 32 42 40 Z")
private val CapSeamsPath = svgPath("M51 18.2 C49 25 48.8 33 49.6 40 M51 18.2 C56 24 58 32 58.8 40")
private val CapVisorPath = svgPath("M61 35 L80 36.5 Q83.5 38.5 80 40.5 L61 40.5 Z")
private val CapVisorShadePath = svgPath("M61.6 39 L80.2 39.5 Q81.4 40 79.8 40 L61.6 40 Z")

private val CollarLeftPath = svgPath("M50 80 L40.6 76.2 L43.4 84.6 Z")
private val CollarRightPath = svgPath("M50 80 L59.4 76.2 L56.6 84.6 Z")
private val BowLeftPath = svgPath("M50 85 L37.4 77.4 Q35.2 85 37.4 92.6 Z")
private val BowRightPath = svgPath("M50 85 L62.6 77.4 Q64.8 85 62.6 92.6 Z")
private val BowFoldsPath = svgPath("M48 84 L39.6 80.2 M48 86 L39.6 89.8 M52 84 L60.4 80.2 M52 86 L60.4 89.8")

private val TamPath = svgPath("M31.5 36.5 C29 24 41 14.5 51 14.5 C61.5 14.5 72.5 22 68.5 36 C62 39.5 40 40 31.5 36.5 Z")
private val TamBandPath = svgPath("M33 34 C42 37 59 37.4 67.6 33.4")

private val HeadbandPath = svgPath("M22.75 51 C22.75 8 77.25 8 77.25 51")
private val HeadbandInnerPath = svgPath("M24 48 C24 13 76 13 76 48")
private val HeadbandPadPath = svgPath("M37.5 17.2 Q50 11.2 62.5 17.2")
private val HeadbandStitchPath = svgPath("M38.5 16.6 Q50 11 61.5 16.6")

private val MicYokePath = svgPath("M18.6 58 Q18.6 66.4 24 66.4 Q29.4 66.4 29.4 58")

private val BeanieDomePath = svgPath("M34 38 C34 21 66 21 66 38 Z")
private val BeanieKnitPath = svgPath("M50 22.6 L50 34.5 M42.4 25.4 Q40.4 30 40.4 34.5 M57.6 25.4 Q59.6 30 59.6 34.5")

private val SmilePath = svgPath("M46 63 Q50 66.6 54 63")
private val WinkPath = svgPath("M43.3 57.4 Q45.5 55.2 47.7 57.4")
private val ClosedEyesPath = svgPath("M43.3 58 Q45.5 55.3 47.7 58 M52.3 58 Q54.5 55.3 56.7 58")
private val OpenMouthPath = svgPath("M46 62.4 Q50 68.4 54 62.4 Z")
private val CoolMouthPath = svgPath("M47.2 64 Q50 65.6 52.8 64")

// Ring with points going in and out, for the electric rings of the rock aura.
// phase: 0 or 1, so the two rings have their points at different places
private fun zigzagPath(inner: Float, outer: Float, step: Float, phase: Int) = Path().apply {
    var index = 0
    var angle = 0f
    while (angle < 360f) {
        val point = around(angle, if (index % 2 == phase) inner else outer)
        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        angle += step
        index++
    }
    close()
}

// Ring that waves: its radius goes up and down `waves` times around the avatar
private fun wavyPath(radius: Float, height: Float, waves: Int) = Path().apply {
    var angle = 0f
    while (angle <= 360f) {
        val point = around(angle, radius + height * sin(waves * angle * PI.toFloat() / 180f))
        if (angle == 0f) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        angle += 4f
    }
    close()
}

private val ZigzagPathA = zigzagPath(55.5f, 61.5f, 7.5f, 0)
private val ZigzagPathB = zigzagPath(56.5f, 62.5f, 7.5f, 1)
private val SmokePathA = wavyPath(60f, 2.6f, 7)
private val SmokePathB = wavyPath(65.5f, 2f, 5)
private val RainClipPath = Path().apply { addOval(Rect(Center, 72f)) }

// ---------- Random details of the auras, always the same ones ----------

private class Dew(val size: Float, val period: Float, val offset: Float)
private class Spark(val angle: Float, val endAngle: Float, val offset: Float)
private class SparkleStar(val angle: Float, val size: Float, val period: Float)
private class StaffNote(val angle: Float, val kind: Char, val radius: Float)
private class Flash(val x: Float, val y: Float, val size: Float)
private class EqualizerBar(val heights: FloatArray, val period: Float)
private class RainDrop(val x: Float, val period: Float, val offset: Float)

private val HaloDew = Random(11).let { random ->
    List(12) { Dew(0.55f + random.nextFloat() * 0.35f, 1.4f + random.nextFloat() * 1.2f, random.nextFloat() * 1.5f) }
}

private val BoltSparks = Random(5).let { random ->
    List(12) {
        val angle = random.nextFloat() * 360f
        Spark(angle, angle + (random.nextFloat() - 0.5f) * 20f, random.nextFloat() * 1.4f)
    }
}

private val SparkleConfetti = Random(9).let { random -> List(14) { random.nextFloat() * 60f } }

private val SparkleStars = listOf(7f, 4.5f, 6f, 4f, 7f, 4.5f, 6f, 4f).mapIndexed { index, size ->
    SparkleStar(index * 45f, size, 1.2f + (index % 4) * 0.2f)
}

// Where the flashes of the pop aura light up, and their size
private val SparkleFlashes = listOf(
    Flash(-12f, -10f, 6f),
    Flash(110f, -6f, 5f),
    Flash(114f, 104f, 6f),
    Flash(-10f, 108f, 5f),
    Flash(50f, -18f, 4f),
)

private val StaffNotes = listOf(
    StaffNote(10f, 'q', 60.5f),
    StaffNote(55f, 'e', 63f),
    StaffNote(105f, 'b', 58f),
    StaffNote(160f, 'q', 65.5f),
    StaffNote(205f, 'e', 58f),
    StaffNote(250f, 'b', 63f),
    StaffNote(305f, 'q', 55.5f),
)

private val EqualizerBars = Random(7).let { random ->
    List(28) {
        EqualizerBar(
            heights = floatArrayOf(
                6f,
                6f + random.nextFloat() * 12f,
                6f + random.nextFloat() * 6f,
                6f + random.nextFloat() * 13f,
                6f,
            ),
            period = 0.7f + random.nextFloat() * 0.6f,
        )
    }
}

private val RainDrops = Random(3).let { random ->
    List(16) { RainDrop(-18f + random.nextFloat() * 136f, 1.6f + random.nextFloat() * 0.9f, -random.nextFloat() * 2.4f) }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun AvatarPreview() {
    MeloDropTheme {
        // One line per style, on 5 drop colors, then the auras
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AVATAR_STYLES.forEachIndexed { line, style ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("night", "cream", "mint", "gold", "spectre").forEachIndexed { index, drop ->
                        Avatar(
                            color = AVATAR_COLORS[(line + index) % AVATAR_COLORS.size].name,
                            style = style.name,
                            drop = drop,
                            face = AVATAR_FACES[(line + index) % AVATAR_FACES.size].name,
                            avatarSize = 48.dp,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AVATAR_AURAS.drop(1).take(5).forEach { aura ->
                    Avatar(color = "lavender", style = "simple", aura = aura.name, showAura = true, animateAura = false, avatarSize = 40.dp)
                }
            }
        }
    }
}
