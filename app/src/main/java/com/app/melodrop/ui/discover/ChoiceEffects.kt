package com.app.melodrop.ui.discover

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Night
import com.app.melodrop.ui.theme.Peach
import com.app.melodrop.ui.theme.Rose
import com.app.melodrop.ui.theme.TextPrimary
import com.app.melodrop.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

// Effects played over Discover when a card is skipped, liked or picked.
// A pick is rare: it gets the big waves, a star, a flash and the word "PICK".
// A like is frequent: two waves and some hearts. A skip stays discreet.
// Sizes are in dp, times in ms, speeds in dp per second, gravity in dp per second²

// How long the yellow flash of a pick lasts (ms)
private const val FLASH_MS = 440f

// How long the screen shakes (ms)
private const val SHAKE_MS = 340f

// How long the word "PICK" stays on screen (ms)
private const val WORD_MS = 900f

// The word "PICK": in the font of the meloDrop title
private val WordStyle = TextStyle(
    fontFamily = Unbounded,
    fontWeight = FontWeight.Bold,
    fontSize = 56.sp,
)

// Shapes of the particles
private enum class Shape { HEART, STAR, DOT, STREAK }

// A wave: a circle that grows from a point and fades out
private class Ring(
    val center: Offset,
    val maxRadius: Float,
    val color: Color,
    val width: Float,
    val delayMs: Float,
    val durationMs: Float,
)

// A small shape thrown away from a point, pulled down by gravity
private class Particle(
    val start: Offset,
    val speedX: Float,
    val speedY: Float,
    val gravity: Float,
    val shape: Shape,
    val color: Color,
    val size: Float,
    // Rotation at the start (degrees), and how fast it turns (degrees per second)
    val angle: Float,
    val spin: Float,
    val delayMs: Float,
    val lifeMs: Float,
)

// A big heart or star that pops in the middle of the card
private class Pop(
    val center: Offset,
    val shape: Shape,
    val color: Color,
    val size: Float,
    val delayMs: Float,
    val durationMs: Float,
)

// Everything one choice plays. Positions are in pixels, from the top left corner
// of the Discover screen. elapsed: time since the choice (ms)
private class Burst(
    val durationMs: Float,
    val rings: List<Ring> = emptyList(),
    val particles: List<Particle> = emptyList(),
    val pop: Pop? = null,
    // Pick only: a flash over the screen
    val flashColor: Color? = null,
    val flashAlpha: Float = 0f,
    val flashDelayMs: Float = 0f,
    // How far the screen shakes (dp), 0 = no shake
    val shake: Float = 0f,
    val shakeDelayMs: Float = 0f,
    // Pick only: where the word "PICK" lands, null = no word
    val wordCenter: Offset? = null,
    val wordDelayMs: Float = 0f,
) {
    val elapsed = Animatable(0f)
}

// Kept by the Discover screen: where the buttons and the card are, and the effects playing
@Stable
internal class ChoiceEffectsState(
    private val scope: CoroutineScope,
    // The word "PICK", measured once
    private val word: TextLayoutResult,
) {
    // Effects playing now. A new choice can start before the last effect ends
    private val bursts = mutableStateListOf<Burst>()

    // Top left corner of the screen, then the middle of the Passer button, of the
    // J'aime button and of the card at rest. All in pixels, in the window
    var origin = Offset.Zero
    var skipButton = Offset.Zero
    var likeButton = Offset.Zero
    var card = Offset.Zero

    // Plays the effect of a choice, then forgets it.
    // density: pixels in one dp, to place the effects
    fun play(choice: Choice, density: Float) {
        val burst = when (choice) {
            Choice.SKIP -> skipBurst(skipButton - origin, card - origin, density)
            Choice.LIKE -> likeBurst(likeButton - origin, card - origin, density)
            Choice.PICK -> pickBurst(card - origin)
        }
        bursts.add(burst)
        scope.launch {
            // From 0 to the end, at a steady pace: each frame draws the effect at this time
            burst.elapsed.animateTo(
                targetValue = burst.durationMs,
                animationSpec = tween(burst.durationMs.toInt(), easing = LinearEasing),
            )
            bursts.remove(burst)
        }
    }

    // Shift of the screen while it shakes, in pixels: a few quick moves that calm down
    fun shake(density: Float): Offset {
        var x = 0f
        for (burst in bursts) {
            val progress = (burst.elapsed.value - burst.shakeDelayMs) / SHAKE_MS
            if (burst.shake > 0f && progress > 0f && progress < 1f) {
                // sin goes back and forth twice, (1 - progress) makes it smaller and smaller
                x += sin(progress * 4f * PI.toFloat()) * (1f - progress) * burst.shake * density
            }
        }
        return Offset(x, -x / 2f)
    }

    // Draws every effect playing, over the screen
    fun draw(drawScope: DrawScope) {
        bursts.forEach { burst -> drawScope.drawBurst(burst, word) }
    }
}

// Creates the state once, with the word "PICK" measured in the font of the title
@Composable
internal fun rememberChoiceEffects(): ChoiceEffectsState {
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    return remember { ChoiceEffectsState(scope, textMeasurer.measure("PICK", WordStyle)) }
}

// Draws the effects over the content, and shakes it a little on a like or a pick.
// Put before the padding, so the effects can use the whole screen
internal fun Modifier.choiceEffects(state: ChoiceEffectsState): Modifier = this
    // Where the screen starts in the window: the effects are drawn from there
    .onGloballyPositioned { state.origin = it.positionInRoot() }
    .graphicsLayer {
        val shift = state.shake(density)
        translationX = shift.x
        translationY = shift.y
    }
    .drawWithContent {
        drawContent()
        state.draw(this)
    }

// Passer: a grey wave from the button, and a few streaks that leave to the left
private fun skipBurst(button: Offset, card: Offset, density: Float): Burst {
    val streakColors = listOf(TextSecondary, TextPrimary, Lavender)
    val streaks = List(10) { index ->
        // Toward the left, a little up or down
        val angle = PI.toFloat() + (Random.nextFloat() - 0.5f) * 0.5f
        val speed = 800f * (0.55f + Random.nextFloat() * 0.6f)
        Particle(
            // Spread on the height of the card
            start = Offset(card.x - 40f * density, card.y + (Random.nextFloat() - 0.5f) * 200f * density),
            speedX = cos(angle) * speed,
            speedY = sin(angle) * speed,
            gravity = 0f,
            shape = Shape.STREAK,
            color = streakColors[index % streakColors.size],
            size = 12f + Random.nextFloat() * 8f,
            angle = 0f,
            spin = 0f,
            delayMs = 30f,
            lifeMs = 800f + Random.nextFloat() * 500f,
        )
    }
    return Burst(
        durationMs = 1400f,
        rings = listOf(Ring(button, 160f, TextSecondary, 5f, 0f, 700f)),
        particles = streaks,
    )
}

// J'aime: two waves from the button, a heart in the middle of the card, hearts that fly up
private fun likeBurst(button: Offset, card: Offset, density: Float): Burst {
    val shapes = listOf(Shape.HEART, Shape.HEART, Shape.DOT)
    val colors = listOf(Mint, Rose, Color.White)
    val hearts = List(20) { index ->
        // Up, more or less to the left or to the right
        val angle = -PI.toFloat() / 2f + (Random.nextFloat() - 0.5f) * PI.toFloat() * 1.4f
        val speed = 470f * (0.55f + Random.nextFloat() * 0.6f)
        Particle(
            start = Offset(button.x, button.y - 10f * density),
            speedX = cos(angle) * speed,
            speedY = sin(angle) * speed,
            gravity = 680f,
            shape = shapes[index % shapes.size],
            color = colors[index % colors.size],
            size = 9f + Random.nextFloat() * 11f,
            angle = Random.nextFloat() * 360f,
            spin = (Random.nextFloat() - 0.5f) * 500f,
            delayMs = 0f,
            lifeMs = 800f + Random.nextFloat() * 500f,
        )
    }
    return Burst(
        durationMs = 1400f,
        rings = listOf(
            Ring(button, 340f, Mint, 8f, 0f, 900f),
            Ring(button, 280f, Rose, 5f, 120f, 850f),
        ),
        particles = hearts,
        pop = Pop(card, Shape.HEART, Mint, 95f, 80f, 800f),
        shake = 2f,
    )
}

// Pick: big waves from the card, like in the video, a star, a flash, stars and the word "PICK"
private fun pickBurst(card: Offset): Burst {
    val ringColors = listOf(Butter, Lavender, Butter, Peach)
    val ringDelays = listOf(0f, 110f, 220f, 340f)
    val rings = List(4) { index ->
        // Each wave a little thinner than the one before
        Ring(card, 500f, ringColors[index], 11f - index * 2f, 150f + ringDelays[index], 1200f)
    }

    val shapes = listOf(Shape.STAR, Shape.STAR, Shape.DOT)
    val colors = listOf(Butter, Peach, Lavender, Color.White)
    val stars = List(32) { index ->
        // In every direction
        val angle = Random.nextFloat() * 2f * PI.toFloat()
        val speed = 560f * (0.55f + Random.nextFloat() * 0.6f)
        Particle(
            start = card,
            speedX = cos(angle) * speed,
            speedY = sin(angle) * speed,
            gravity = 400f,
            shape = shapes[index % shapes.size],
            color = colors[index % colors.size],
            size = 9f + Random.nextFloat() * 11f,
            angle = Random.nextFloat() * 360f,
            spin = (Random.nextFloat() - 0.5f) * 500f,
            delayMs = 170f,
            lifeMs = 800f + Random.nextFloat() * 500f,
        )
    }

    return Burst(
        durationMs = 1700f,
        rings = rings,
        particles = stars,
        pop = Pop(card, Shape.STAR, Butter, 140f, 170f, 1000f),
        flashColor = Butter,
        flashAlpha = 0.24f,
        flashDelayMs = 150f,
        shake = 5f,
        shakeDelayMs = 150f,
        wordCenter = card,
        wordDelayMs = 230f,
    )
}

// Draws one effect at its current time
private fun DrawScope.drawBurst(burst: Burst, word: TextLayoutResult) {
    val time = burst.elapsed.value

    burst.flashColor?.let { color ->
        val progress = (time - burst.flashDelayMs) / FLASH_MS
        if (progress > 0f && progress < 1f) {
            // Strongest in the middle, then back to nothing
            val strength = if (progress < 0.5f) progress * 2f else (1f - progress) * 2f
            drawRect(color.copy(alpha = burst.flashAlpha * strength))
        }
    }

    burst.rings.forEach { ring -> drawRing(ring, time) }
    burst.pop?.let { pop -> drawPop(pop, time) }
    burst.particles.forEach { particle -> drawParticle(particle, time) }
    burst.wordCenter?.let { center -> drawWord(word, center, time - burst.wordDelayMs) }
}

// A wave: fast at first, then slower, like a ripple on water. Thinner and paler as it grows
private fun DrawScope.drawRing(ring: Ring, time: Float) {
    val progress = (time - ring.delayMs) / ring.durationMs
    if (progress <= 0f || progress >= 1f) return

    val eased = 1f - (1f - progress).pow(3)
    drawCircle(
        color = ring.color.copy(alpha = 1f - progress),
        radius = ring.maxRadius * density * eased,
        center = ring.center,
        style = Stroke(width = (ring.width * (1f - progress) + 0.5f) * density),
    )
}

// The big heart or star: grows past its size, comes back, then grows slowly while it fades
private fun DrawScope.drawPop(pop: Pop, time: Float) {
    val progress = (time - pop.delayMs) / pop.durationMs
    if (progress <= 0f || progress >= 1f) return

    val scale = when {
        progress < 0.22f -> 1.3f * (1f - (1f - progress / 0.22f).pow(3))
        progress < 0.38f -> 1.3f - 0.3f * (progress - 0.22f) / 0.16f
        else -> 1f + (progress - 0.38f) * 0.35f
    }
    val alpha = if (progress < 0.5f) 1f else 1f - (progress - 0.5f) / 0.5f
    drawShape(pop.shape, pop.color.copy(alpha = alpha), pop.center, pop.size * scale * density, 0f)
}

// A particle: goes straight at its speed, and falls more and more with the gravity
private fun DrawScope.drawParticle(particle: Particle, time: Float) {
    val age = time - particle.delayMs
    if (age <= 0f || age >= particle.lifeMs) return

    val seconds = age / 1000f
    val x = particle.start.x + particle.speedX * seconds * density
    val y = particle.start.y + (particle.speedY * seconds + 0.5f * particle.gravity * seconds * seconds) * density
    // Grows very fast when it appears, fades at the end of its life
    val size = particle.size * min(1f, seconds * 9f) * density
    val alpha = 1f - (age / particle.lifeMs).pow(2)
    drawShape(particle.shape, particle.color.copy(alpha = alpha), Offset(x, y), size, particle.angle + particle.spin * seconds)
}

// Draws one shape around its center. size and center in pixels, rotation in degrees
private fun DrawScope.drawShape(shape: Shape, color: Color, center: Offset, size: Float, rotation: Float) {
    when (shape) {
        Shape.DOT -> drawCircle(color, radius = size * 0.2f, center = center)
        Shape.STREAK -> drawRect(
            color = color,
            topLeft = Offset(center.x - size * 2f, center.y - 1.5f * density),
            size = Size(size * 4f, 3f * density),
        )
        Shape.HEART, Shape.STAR -> withTransform({
            // The paths are 1 pixel wide around 0: moved, turned, then enlarged
            translate(center.x, center.y)
            rotate(rotation, pivot = Offset.Zero)
            scale(size, size, pivot = Offset.Zero)
        }) {
            drawPath(if (shape == Shape.HEART) HeartPath else StarPath, color)
        }
    }
}

// The word "PICK": lands big, settles, then grows a little while it fades
private fun DrawScope.drawWord(word: TextLayoutResult, center: Offset, time: Float) {
    val progress = time / WORD_MS
    if (progress <= 0f || progress >= 1f) return

    val scale = when {
        progress < 0.18f -> mix(2.6f, 0.95f, progress / 0.18f)
        progress < 0.28f -> mix(0.95f, 1f, (progress - 0.18f) / 0.10f)
        progress < 0.68f -> mix(1f, 1.04f, (progress - 0.28f) / 0.40f)
        else -> mix(1.04f, 1.1f, (progress - 0.68f) / 0.32f)
    }
    val alpha = when {
        progress < 0.18f -> progress / 0.18f
        progress < 0.68f -> 1f
        else -> 1f - (progress - 0.68f) / 0.32f
    }
    val topLeft = Offset(center.x - word.size.width / 2f, center.y - word.size.height / 2f)

    withTransform({ scale(scale, scale, pivot = center) }) {
        // A dark outline first: the word stays readable on any cover
        drawText(word, color = Night, topLeft = topLeft, alpha = alpha, drawStyle = Stroke(width = 5f * density))
        drawText(word, color = Butter, topLeft = topLeft, alpha = alpha)
    }
}

// Value between start and stop: fraction 0 gives start, 1 gives stop
private fun mix(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction

// Heart 1 pixel wide, around 0: enlarged to its size when drawn
private val HeartPath = Path().apply {
    moveTo(0f, 0.4f)
    cubicTo(-0.15f, 0.25f, -0.5f, 0.05f, -0.5f, -0.18f)
    cubicTo(-0.5f, -0.48f, -0.1f, -0.52f, 0f, -0.24f)
    cubicTo(0.1f, -0.52f, 0.5f, -0.48f, 0.5f, -0.18f)
    cubicTo(0.5f, 0.05f, 0.15f, 0.25f, 0f, 0.4f)
    close()
}

// Star 1 pixel wide, around 0: ten points, one out of two closer to the center
private val StarPath = Path().apply {
    for (index in 0 until 10) {
        val radius = if (index % 2 == 0) 0.5f else 0.22f
        val angle = -PI.toFloat() / 2f + index * PI.toFloat() / 5f
        val x = cos(angle) * radius
        val y = sin(angle) * radius
        if (index == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
