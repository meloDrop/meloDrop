package com.app.melodrop.ui.tutorial

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.MeloDropTheme

// Darkness of the veil: 0 = see-through, 1 = black
private const val VEIL_ALPHA = 0.8f

// Space between the element and the edge of the hole
private val HOLE_PADDING = 8.dp

// Space between the hole and the bubble
private val BUBBLE_GAP = 16.dp

// Dark veil over the whole app, with a hole on the element of the current step
// and a bubble that explains it. Shows nothing when there is no tour.
// Placed in MainScreen, which fills the app from its top left corner:
// the places noted by Modifier.tutorialTarget can be used as they are
@Composable
fun TutorialOverlay() {
    // Logout: the tour stops and the places are forgotten, for the next account
    DisposableEffect(Unit) {
        onDispose {
            Tutorial.stop()
            Tutorial.targets.clear()
        }
    }

    val step = Tutorial.currentStep ?: return
    // Element not on the screen: nothing to point at
    val target = Tutorial.targets[step.target] ?: return

    // Back button of the phone: ends the tour, the screen behind stays as it is
    BackHandler { Tutorial.stop() }

    val density = LocalDensity.current
    // Hole a bit bigger than the element, so the ring does not touch it
    val hole = target.inflate(with(density) { HOLE_PADDING.toPx() })
    // The hole slides from one element to the next, instead of jumping
    val animatedHole by animateRectAsState(targetValue = hole, label = "tutorialHole")

    // The lavender ring fades in and out, so the eye finds it at once.
    // A black veil alone shows little on the dark background of the app
    val pulse = rememberInfiniteTransition(label = "tutorialPulse")
    val ringAlpha by pulse.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900), RepeatMode.Reverse),
        label = "tutorialRing",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            // Catches the taps outside the bubble: the screen behind cannot be used
            .pointerInput(Unit) { detectTapGestures { } },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Draws in its own layer: BlendMode.Clear then makes a real hole,
                // instead of a black shape
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            // Round ends: a circle on a round button, a pill on a chip or a tab
            val corner = CornerRadius(minOf(animatedHole.width, animatedHole.height) / 2)

            drawRect(Color.Black.copy(alpha = VEIL_ALPHA))
            // Clear: erases the veil inside the hole
            drawRoundRect(
                color = Color.Transparent,
                topLeft = animatedHole.topLeft,
                size = animatedHole.size,
                cornerRadius = corner,
                blendMode = BlendMode.Clear,
            )
            drawRoundRect(
                color = Lavender.copy(alpha = ringAlpha),
                topLeft = animatedHole.topLeft,
                size = animatedHole.size,
                cornerRadius = corner,
                style = Stroke(width = 3.dp.toPx()),
            )
        }

        // Element in the top half of the screen: the bubble goes under it. Otherwise, above it
        val holeTop = with(density) { hole.top.toDp() }
        val holeBottom = with(density) { hole.bottom.toDp() }
        val bubblePlace = if (hole.center.y < constraints.maxHeight / 2f) {
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = holeBottom + BUBBLE_GAP)
        } else {
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = maxHeight - holeTop + BUBBLE_GAP)
        }

        TutorialBubble(
            step = step,
            number = Tutorial.index + 1,
            count = Tutorial.steps.size,
            onNext = Tutorial::next,
            onSkip = Tutorial::stop,
            modifier = bubblePlace.padding(horizontal = 24.dp),
        )
    }
}

// Card next to the hole: step number, title, text, and the buttons
@Composable
private fun TutorialBubble(
    step: TutorialStep,
    number: Int,
    count: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLast = number == count

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Lavender),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // "2 / 5": the user knows how long the tour is
            Text(
                text = "$number / $count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = step.title,
                fontFamily = Unbounded,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = step.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // No "Passer" on the last step: "Compris" already ends the tour
                if (!isLast) {
                    TextButton(onClick = onSkip) {
                        Text("Passer", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Pushes the main button against the right edge
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onNext) {
                    Text(if (isLast) "Compris" else "Suivant")
                }
            }
        }
    }
}

// The bubble only: the hole needs the real places of the elements
@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun TutorialBubblePreview() {
    MeloDropTheme {
        TutorialBubble(
            step = TutorialLastStep,
            number = 2,
            count = 5,
            onNext = {},
            onSkip = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
