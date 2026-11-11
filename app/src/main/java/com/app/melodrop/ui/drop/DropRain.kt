package com.app.melodrop.ui.drop

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Peach
import com.app.melodrop.ui.theme.Rose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// Rain of pastel drops over the Drop screen, right after a drop is online.
// Small drops fall everywhere and make a small wave where they land. Then a big drop
// with the stripes of the logo falls in the middle, and its waves open the drop of the day.
// Sizes are in dp, times in ms, speeds in dp per second, gravity in dp per second²

// About when the big drop lands: the drop of the day appears just after it (ms)
internal const val RAIN_LANDING_MS = 1350L

// Whole duration of the rain (ms)
internal const val RAIN_MS = 3300

// Colors of the drops, in the order of the stripes of the logo
private val RainColors = listOf(Lavender, Rose, Peach, Butter, Mint)

// Number of small drops
private const val SMALL_DROPS = 40

// The drops start falling this far above the screen
private const val START_ABOVE = 30f

// Height divided by width of the waves on the ground: water seen from the side
private const val PERSPECTIVE = 0.36f

// The big drop: when it starts falling, its size, and where it lands
// (fractions of the width and of the height of the screen: on the cover of the drop)
private const val BIG_START_MS = 780f
private const val BIG_SIZE = 26f
private const val BIG_LAND_X = 0.5f
private const val BIG_LAND_Y = 0.38f

// Waves of the big drop: one every BIG_RING_DELAY_MS, each one grows for BIG_RING_MS
private const val BIG_RING_RADIUS = 175f
private const val BIG_RING_DELAY_MS = 120f
private const val BIG_RING_MS = 1400f

// Gravity on the droplets thrown up by an impact
private const val DROPLET_GRAVITY = 900f

// A droplet thrown up when a drop lands
private class Droplet(
    val speedX: Float,
    val speedY: Float,
    val color: Color,
    val radius: Float,
    val lifeMs: Float,
)

// One falling drop. x and landY: fractions of the width and of the height of the screen.
// color null: the big drop, with the stripes of the logo
private class FallingDrop(
    val x: Float,
    val landY: Float,
    val size: Float,
    val color: Color?,
    val startMs: Float,
    val speed: Float,
    val gravity: Float,
    val droplets: List<Droplet>,
)

// Kept by the Drop screen: the drops of the rain, and the time since it started
@Stable
internal class DropRainState(private val scope: CoroutineScope) {
    // Time since the start of the rain (ms). At RAIN_MS, nothing is drawn
    private val elapsed = Animatable(RAIN_MS.toFloat())
    private var drops: List<FallingDrop> = emptyList()

    // Starts a new rain, with drops placed at random
    fun start() {
        drops = newRain()
        scope.launch {
            elapsed.snapTo(0f)
            // From 0 to the end, at a steady pace: each frame draws the rain at this time
            elapsed.animateTo(RAIN_MS.toFloat(), tween(RAIN_MS, easing = LinearEasing))
        }
    }

    // Draws the rain at its current time, over the screen
    fun draw(drawScope: DrawScope) {
        val time = elapsed.value
        if (time >= RAIN_MS) return
        drops.forEach { drop -> drawScope.drawRainDrop(drop, time) }
    }
}

// Starts the rain when play becomes true, then tells the ViewModel with onStart,
// so it plays only once. The rain runs in the scope of the screen: it goes on
// when play goes back to false
@Composable
internal fun rememberDropRain(play: Boolean, onStart: () -> Unit): DropRainState {
    val scope = rememberCoroutineScope()
    val state = remember { DropRainState(scope) }
    LaunchedEffect(play) {
        if (play) {
            state.start()
            onStart()
        }
    }
    return state
}

// Draws the rain over the content. Put before the padding, so it can use the whole screen
internal fun Modifier.dropRain(state: DropRainState): Modifier = drawWithContent {
    drawContent()
    state.draw(this)
}

// 40 small drops spread over the screen and over the first second, then the big one
private fun newRain(): List<FallingDrop> {
    val small = List(SMALL_DROPS) { index ->
        val color = RainColors[index % RainColors.size]
        FallingDrop(
            x = 0.05f + Random.nextFloat() * 0.9f,
            landY = 0.2f + Random.nextFloat() * 0.74f,
            size = 5f + Random.nextFloat() * 4.5f,
            color = color,
            // One after the other, a little at random
            startMs = index * 24f + Random.nextFloat() * 140f,
            speed = 120f + Random.nextFloat() * 160f,
            gravity = 1500f,
            droplets = newDroplets(3, listOf(color), 170f),
        )
    }
    val big = FallingDrop(
        x = BIG_LAND_X,
        landY = BIG_LAND_Y,
        size = BIG_SIZE,
        color = null,
        startMs = BIG_START_MS,
        speed = 260f,
        gravity = 2100f,
        droplets = newDroplets(18, RainColors, 420f),
    )
    return small + big
}

// Droplets thrown up, a bit to the left or to the right
private fun newDroplets(count: Int, colors: List<Color>, speed: Float): List<Droplet> = List(count) { index ->
    val angle = -PI.toFloat() / 2f + (Random.nextFloat() - 0.5f) * PI.toFloat() * 1.1f
    val dropletSpeed = speed * (0.5f + Random.nextFloat() * 0.7f)
    Droplet(
        speedX = cos(angle) * dropletSpeed,
        speedY = sin(angle) * dropletSpeed,
        color = colors[index % colors.size],
        radius = 2f + Random.nextFloat() * 2f,
        lifeMs = 420f + Random.nextFloat() * 300f,
    )
}

// Time for a drop to fall a distance in dp (ms).
// distance = speed * t + gravity * t² / 2, solved for t
private fun fallMs(drop: FallingDrop, distance: Float): Float {
    val seconds = (-drop.speed + sqrt(drop.speed * drop.speed + 2f * drop.gravity * distance)) / drop.gravity
    return seconds * 1000f
}

// One drop: falling, then its impact once it reached its landing point
private fun DrawScope.drawRainDrop(drop: FallingDrop, time: Float) {
    val age = time - drop.startMs
    if (age <= 0f) return

    val x = drop.x * size.width
    val landY = drop.landY * size.height
    val landMs = fallMs(drop, landY / density + START_ABOVE)

    if (age < landMs) {
        val seconds = age / 1000f
        val fallen = drop.speed * seconds + 0.5f * drop.gravity * seconds * seconds
        // Faster and faster: the drop gets longer and thinner while it falls
        val stretch = 1f + min(0.55f, (drop.speed + drop.gravity * seconds) / 2600f)
        drawFallingDrop(drop, Offset(x, (fallen - START_ABOVE) * density), stretch)
    } else {
        drawImpact(drop, Offset(x, landY), age - landMs)
    }
}

// The drop shape, stretched while it falls. The big one has the stripes of the logo
private fun DrawScope.drawFallingDrop(drop: FallingDrop, center: Offset, stretch: Float) {
    val sizePx = drop.size * density
    withTransform({
        // The path is 1 pixel wide around 0: moved, then enlarged
        translate(center.x, center.y)
        scale(sizePx / sqrt(stretch), sizePx * stretch, pivot = Offset.Zero)
    }) {
        val color = drop.color
        if (color != null) {
            drawPath(DropPath, color)
        } else {
            // Five stripes, cut to the shape of the drop
            clipPath(DropPath) {
                val stripe = DROP_HEIGHT / RainColors.size
                RainColors.forEachIndexed { index, stripeColor ->
                    drawRect(
                        color = stripeColor,
                        topLeft = Offset(-1f, -1f + index * stripe),
                        size = Size(2f, stripe + 0.01f),
                    )
                }
            }
        }
    }
}

// Where a drop lands: waves seen from the side, and droplets that jump up
private fun DrawScope.drawImpact(drop: FallingDrop, center: Offset, age: Float) {
    if (drop.color == null) {
        // The big drop: one wave of each color, one after the other
        RainColors.forEachIndexed { index, color ->
            drawWave(center, color, BIG_RING_RADIUS, 8f - index * 1.2f, (age - index * BIG_RING_DELAY_MS) / BIG_RING_MS)
        }
    } else {
        // A small drop: two small waves of its color
        drawWave(center, drop.color, drop.size * 5.5f, 2.5f, age / 700f)
        drawWave(center, drop.color, drop.size * 3.2f, 1.5f, (age - 110f) / 600f)
    }

    drop.droplets.forEach { droplet ->
        if (age < droplet.lifeMs) {
            val seconds = age / 1000f
            val x = center.x + droplet.speedX * seconds * density
            val y = center.y + (droplet.speedY * seconds + 0.5f * DROPLET_GRAVITY * seconds * seconds - 2f) * density
            drawCircle(
                color = droplet.color.copy(alpha = 1f - age / droplet.lifeMs),
                radius = droplet.radius * density,
                center = Offset(x, y),
            )
        }
    }
}

// A wave on the ground: an ellipse that grows fast, then slower, and fades.
// maxRadius and width in dp, progress from 0 to 1
private fun DrawScope.drawWave(center: Offset, color: Color, maxRadius: Float, width: Float, progress: Float) {
    if (progress <= 0f || progress >= 1f) return

    val radius = maxRadius * density * (1f - (1f - progress).pow(3))
    drawOval(
        color = color.copy(alpha = 1f - progress),
        topLeft = Offset(center.x - radius, center.y - radius * PERSPECTIVE),
        size = Size(radius * 2f, radius * 2f * PERSPECTIVE),
        style = Stroke(width = (width * (1f - progress) + 0.5f) * density),
    )
}

// Height of the drop path: from its tip (-1) to the bottom of its round part (0.85)
private const val DROP_HEIGHT = 1.85f

// Drop 1 pixel wide around 0: the tip up, round at the bottom
private val DropPath = Path().apply {
    moveTo(0f, -1f)
    cubicTo(0.12f, -0.55f, 0.6f, -0.15f, 0.6f, 0.25f)
    // Bottom half of a circle, from the right side to the left side
    arcTo(Rect(-0.6f, -0.35f, 0.6f, 0.85f), 0f, 180f, false)
    cubicTo(-0.6f, -0.15f, -0.12f, -0.55f, 0f, -1f)
    close()
}
