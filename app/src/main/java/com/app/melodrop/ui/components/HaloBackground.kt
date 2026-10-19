package com.app.melodrop.ui.components

import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.app.melodrop.ui.navigation.Routes
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Rose
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// One halo: its center (x and y as a fraction of the screen width and height),
// its radius (fraction of the width) and its strength (alpha in the middle)
private data class Halo(val x: Float, val y: Float, val radius: Float, val alpha: Float)

// Halo colors, always in this order: Lavender for the brand,
// then the colors of the three tabs of the bottom bar
private val HaloColors = listOf(Lavender, Butter, Mint, Rose)

// Where the halos sit on each tab. The halo of the tab's color leads:
// bigger, brighter, at the top. The others stay on the edges.
private val Layouts = mapOf(
    Routes.DISCOVER to listOf(
        Halo(x = 0.0f, y = 0.55f, radius = 0.75f, alpha = 0.12f),  // Lavender
        Halo(x = 0.85f, y = 0.08f, radius = 0.95f, alpha = 0.20f), // Butter (lead)
        Halo(x = 0.1f, y = 1.0f, radius = 0.7f, alpha = 0.10f),    // Mint
        Halo(x = 1.0f, y = 0.75f, radius = 0.7f, alpha = 0.12f),   // Rose
    ),
    Routes.DROP to listOf(
        Halo(x = 1.0f, y = 0.45f, radius = 0.75f, alpha = 0.12f),  // Lavender
        Halo(x = 0.0f, y = 0.9f, radius = 0.7f, alpha = 0.10f),    // Butter
        Halo(x = 0.15f, y = 0.1f, radius = 0.95f, alpha = 0.20f),  // Mint (lead)
        Halo(x = 0.9f, y = 1.0f, radius = 0.7f, alpha = 0.12f),    // Rose
    ),
    Routes.PROFILE to listOf(
        Halo(x = 0.05f, y = 0.75f, radius = 0.8f, alpha = 0.14f),  // Lavender
        Halo(x = 1.0f, y = 0.95f, radius = 0.65f, alpha = 0.10f),  // Butter
        Halo(x = 1.0f, y = 0.35f, radius = 0.65f, alpha = 0.10f),  // Mint
        Halo(x = 0.5f, y = 0.0f, radius = 1.0f, alpha = 0.20f),    // Rose (lead)
    ),
)

// Soft falloff from the center (0) to the edge (1), close to a real glow:
// the strength drops smoothly, with no break that could show as a ring
private val FalloffPositions = floatArrayOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f)
private val FalloffStrengths = floatArrayOf(1f, 0.8f, 0.48f, 0.2f, 0.05f, 0f)

// Time for the halos to glide from one tab layout to the next (ms)
private const val MOVE_MS = 1200

// Starts fast, ends slowly: the halos settle softly in their new place
private val MoveSpec = tween<Float>(MOVE_MS, easing = FastOutSlowInEasing)

// Time for one slow loop of the idle drift (ms)
private const val DRIFT_MS = 20_000

// How far a halo drifts around its place (fraction of the width)
private const val DRIFT = 0.04f

// Screen background with pastel light halos that move when the tab changes
@Composable
fun HaloBackground(route: String?, modifier: Modifier = Modifier) {
    // No route yet (very first frame): use the Discover layout
    val targets = Layouts[route] ?: Layouts.getValue(Routes.DISCOVER)

    // When the targets change, each value glides to its new target
    val halos = targets.map { target ->
        val x by animateFloatAsState(target.x, MoveSpec, label = "x")
        val y by animateFloatAsState(target.y, MoveSpec, label = "y")
        val radius by animateFloatAsState(target.radius, MoveSpec, label = "radius")
        val alpha by animateFloatAsState(target.alpha, MoveSpec, label = "alpha")
        Halo(x, y, radius, alpha)
    }

    // A clock that loops forever: from 0 to 2π (one full turn) every DRIFT_MS
    val drift = rememberInfiniteTransition(label = "drift")
    val time by drift.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(DRIFT_MS, easing = LinearEasing)),
        label = "time",
    )

    // Android Paint, created once: Compose brushes cannot dither.
    // Dithering mixes neighbour shades, so the gradient shows no steps
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        halos.forEachIndexed { index, halo ->
            // Each halo turns on its own small circle, every other one the other way
            val angle = if (index % 2 == 0) time + index else -time + index
            val center = Offset(
                x = (halo.x + DRIFT * cos(angle)) * size.width,
                y = halo.y * size.height + DRIFT * size.width * sin(angle),
            )
            val radius = halo.radius * size.width
            val color = HaloColors[index]

            // The halo color at each step of the falloff
            val colors = FalloffStrengths
                .map { strength -> color.copy(alpha = halo.alpha * strength).toArgb() }
                .toIntArray()

            paint.shader = RadialGradient(
                center.x, center.y, radius,
                colors, FalloffPositions,
                Shader.TileMode.CLAMP,
            )

            // Draws with the Android canvas behind Compose, to use our Paint
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawCircle(center.x, center.y, radius, paint)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HaloBackgroundPreview() {
    MeloDropTheme {
        // Change to Routes.DROP or Routes.PROFILE to see the other layouts
        HaloBackground(route = Routes.DISCOVER)
    }
}