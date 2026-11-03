package com.app.melodrop.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Peach
import com.app.melodrop.ui.theme.Rose
import kotlinx.coroutines.launch
import kotlin.math.pow

// Wave colors, in the order of the stripes of the logo
private val RingColors = listOf(Lavender, Rose, Peach, Butter, Mint)

// Height divided by width of the waves: below 1, they look like
// water seen from the side, like the waves of the logo (1f = circles)
private const val PERSPECTIVE = 0.45f

// Step 1: the drop falls onto the touched point (ms)
private const val FALL_MS = 260f

// Step 2: the impact, with a glow and a droplet that bounces back up (ms)
private const val SPLASH_MS = 450f

// Step 3: one wave starts every RING_DELAY_MS, and each one grows for RING_MS
private const val RING_DELAY_MS = 200f
private const val RING_MS = 1800f

// Total duration of one ripple (ms)
private val DURATION_MS = FALL_MS + RING_DELAY_MS * (RingColors.size - 1) + RING_MS

// Opacity of the first wave; each next wave is weaker, like a real ripple
private const val MAX_ALPHA = 0.35f
private const val DAMPING = 0.75f

// One ripple: where the finger touched, and the time since the touch (ms)
private class Ripple(val center: Offset) {
    val elapsed = Animatable(0f)
}

// Full-screen layer that makes a drop fall where the user touches
@Composable
fun WaterRippleLayer(
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
) {
    val ripples = remember { mutableStateListOf<Ripple>() }
    val scope = rememberCoroutineScope()
    // Always calls the latest onTap, even though pointerInput starts only once
    val currentOnTap by rememberUpdatedState(onTap)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    // The drop starts falling as soon as the finger touches the screen
                    onPress = { position ->
                        val ripple = Ripple(position)
                        ripples.add(ripple)
                        scope.launch {
                            ripple.elapsed.animateTo(
                                targetValue = DURATION_MS,
                                animationSpec = tween(DURATION_MS.toInt(), easing = LinearEasing),
                            )
                            // The ripple is over: forget it
                            ripples.remove(ripple)
                        }
                    },
                    onTap = { currentOnTap() },
                )
            }
    ) {
        ripples.forEach { ripple -> drawRipple(ripple) }
    }
}

// Draws the three steps of one ripple: falling drop, splash, then waves
private fun DrawScope.drawRipple(ripple: Ripple) {
    val time = ripple.elapsed.value
    val center = ripple.center

    if (time < FALL_MS) {
        drawFallingDrop(center, progress = time / FALL_MS)
        return
    }

    val sinceImpact = time - FALL_MS

    if (sinceImpact < SPLASH_MS) {
        drawSplash(center, progress = sinceImpact / SPLASH_MS)
    }

    RingColors.forEachIndexed { index, color ->
        // Each wave starts a little after the previous one
        val ringProgress = (sinceImpact - index * RING_DELAY_MS) / RING_MS
        if (ringProgress > 0f && ringProgress < 1f) {
            drawWave(center, color, index, ringProgress)
        }
    }
}

// The drop falls from above and speeds up, like with gravity
private fun DrawScope.drawFallingDrop(center: Offset, progress: Float) {
    val fallHeight = 56.dp.toPx()
    val width = 5.dp.toPx()
    val height = 8.dp.toPx()
    // progress squared: slow at first, then faster and faster
    val bottom = center.y - fallHeight * (1f - progress * progress)

    drawOval(
        color = Color.White.copy(alpha = 0.2f + 0.4f * progress),
        topLeft = Offset(center.x - width / 2, bottom - height),
        size = Size(width, height),
    )
}

// The impact: a soft glow, and a tiny droplet that jumps up and falls back
private fun DrawScope.drawSplash(center: Offset, progress: Float) {
    val fade = 1f - progress

    // Glow on the water, flattened like the waves
    val glowRadius = 6.dp.toPx() + 18.dp.toPx() * progress
    scale(scaleX = 1f, scaleY = PERSPECTIVE, pivot = center) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.35f * fade), Color.Transparent),
                center = center,
                radius = glowRadius,
            ),
            radius = glowRadius,
            center = center,
        )
    }

    // Droplet: 4 * p * (1 - p) goes from 0 up to 1, then back to 0 (a jump)
    val jump = 22.dp.toPx() * 4f * progress * (1f - progress)
    drawCircle(
        color = Color.White.copy(alpha = 0.45f * fade),
        radius = 2.5.dp.toPx(),
        center = Offset(center.x, center.y - jump),
    )
}

// One wave: a soft pastel crest that grows, widens and fades out
private fun DrawScope.drawWave(center: Offset, color: Color, index: Int, progress: Float) {
    // Fast at first, then slower, like a real ripple
    val eased = LinearOutSlowInEasing.transform(progress)
    val radius = size.minDimension * 0.45f * eased
    val halfWidth = 4.dp.toPx() + 6.dp.toPx() * eased
    val outer = radius + halfWidth
    val alpha = MAX_ALPHA * DAMPING.pow(index) * (1f - progress)

    // Gradient positions, from 0 (center) to 1 (outer edge of the crest)
    val inner = ((radius - halfWidth) / outer).coerceAtLeast(0f)
    val crest = radius / outer

    // Draws a circle, then squashes it vertically to get the perspective
    scale(scaleX = 1f, scaleY = PERSPECTIVE, pivot = center) {
        drawCircle(
            brush = Brush.radialGradient(
                inner to Color.Transparent,
                crest to color.copy(alpha = alpha),
                1f to Color.Transparent,
                center = center,
                radius = outer,
            ),
            radius = outer,
            center = center,
        )
    }
}