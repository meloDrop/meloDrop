package com.app.melodrop.ui.tutorial

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.melodrop.ui.components.HaloBackground
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.discover.DiscoverTutorialDemo
import com.app.melodrop.ui.drop.DropTutorialDemo
import com.app.melodrop.ui.navigation.BottomBar
import com.app.melodrop.ui.profile.ProfileTutorialDemo
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.MeloDropTheme

// Space between the element and the edge of the hole
private val HOLE_PADDING = 8.dp

// Space between the hole and the bubble, and between the bubble and the edges of the screen
private val BUBBLE_GAP = 12.dp

// Smallest bubble: the step number, the title, one line of text and the buttons.
// When neither side of the element has that much room, the bubble covers a bit of it
private val BUBBLE_MIN_HEIGHT = 170.dp

// On a tablet or a phone held sideways, the bubble does not take the whole width
private val BUBBLE_MAX_WIDTH = 480.dp

// The tutorial over the whole app: an example screen of the tab, the dark veil with a hole
// on the element of the step, and a bubble that explains it. Shows nothing without a tutorial.
// Placed in MainScreen, which fills the app from its top left corner:
// the places noted by Modifier.tutorialTarget can be used as they are
@Composable
fun TutorialOverlay() {
    // Logout: the tutorial stops and the places are forgotten, for the next account
    DisposableEffect(Unit) {
        onDispose {
            Tutorial.stop()
            Tutorial.targets.clear()
        }
    }

    val screen = Tutorial.screen ?: return
    val step = Tutorial.currentStep ?: return

    // Back button of the phone: closes the tutorial
    BackHandler { Tutorial.stop() }

    Box(modifier = Modifier.fillMaxSize()) {
        // The example screen, drawn like the real tab. LocalTutorialDemo: its elements note
        // their place. clearAndSetSemantics: TalkBack reads the bubble, not the example
        CompositionLocalProvider(LocalTutorialDemo provides true) {
            Box(modifier = Modifier.clearAndSetSemantics { }) {
                TutorialDemo(screen)
            }
        }

        // Over the example, a sibling: it catches every tap, so the example cannot be used
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { } },
        ) {
            val target = Tutorial.targets[step.target]
            if (target == null) {
                // The example is not laid out yet: the veil alone for a moment
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(Color.Black.copy(alpha = step.veilAlpha))
                }
            } else {
                Spotlight(screen = screen, step = step, target = target)
            }
        }
    }
}

// The example screen of a tab: its halos, the screen and the bottom bar, like in MainScreen
@Composable
private fun TutorialDemo(screen: TutorialScreen) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Opaque: the real app behind does not show through
        HaloBackground(route = screen.route)

        Scaffold(
            containerColor = Color.Transparent,
            // The tab of the tutorial is the one lit up
            bottomBar = { BottomBar(currentRoute = screen.route, onTabClick = {}) },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (screen) {
                    TutorialScreen.DISCOVER -> DiscoverTutorialDemo()
                    TutorialScreen.DROP -> DropTutorialDemo()
                    TutorialScreen.PROFILE -> ProfileTutorialDemo()
                }
            }
        }
    }
}

// The veil with its hole on the element, the lavender ring, and the bubble
@Composable
private fun Spotlight(screen: TutorialScreen, step: TutorialStep, target: Rect) {
    val density = LocalDensity.current
    // Hole a bit bigger than the element, so the ring does not touch it
    val hole = target.inflate(with(density) { HOLE_PADDING.toPx() })
    // The hole slides from one element to the next, instead of jumping
    val animatedHole by animateRectAsState(targetValue = hole, label = "tutorialHole")
    // The veil gets lighter or darker smoothly from one step to the next
    val veilAlpha by animateFloatAsState(targetValue = step.veilAlpha, label = "tutorialVeil")

    // The lavender ring fades in and out, so the eye finds it at once.
    // A black veil alone shows little on the dark background of the app
    val pulse = rememberInfiniteTransition(label = "tutorialPulse")
    val ringAlpha by pulse.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900), RepeatMode.Reverse),
        label = "tutorialRing",
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Draws in its own layer: BlendMode.Clear then makes a real hole,
                // instead of a black shape
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            // Round ends: a circle on a round button, a pill on a chip or a line.
            // At most 28 dp: a big element, like the card, keeps its rounded corners
            val corner = CornerRadius(minOf(animatedHole.width, animatedHole.height, 56.dp.toPx()) / 2)

            drawRect(Color.Black.copy(alpha = veilAlpha))
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

        // The part of the screen the bubble can use: under the status bar
        // and above the navigation bar, with a small gap
        val bars = WindowInsets.systemBars.asPaddingValues()
        val safeTop = bars.calculateTopPadding() + BUBBLE_GAP
        val safeBottom = maxHeight - bars.calculateBottomPadding() - BUBBLE_GAP

        // Room above and under the element, inside that part
        val holeTop = with(density) { hole.top.toDp() }
        val holeBottom = with(density) { hole.bottom.toDp() }
        val roomAbove = holeTop - BUBBLE_GAP - safeTop
        val roomBelow = safeBottom - (holeBottom + BUBBLE_GAP)

        // The bubble goes on the side with the most room, and never gets taller than
        // that room: the text scrolls inside, the buttons always stay on the screen
        val isBelow = roomBelow >= roomAbove
        val room = maxOf(roomAbove, roomBelow)
        val bubblePlace = when {
            // Next to the element, under it
            // coerceAtLeast: a padding cannot be negative, even for an element
            // that is still sliding in from outside the screen
            isBelow && room >= BUBBLE_MIN_HEIGHT -> Modifier
                .align(Alignment.TopCenter)
                .padding(top = (holeBottom + BUBBLE_GAP).coerceAtLeast(0.dp))
                .heightIn(max = room)

            // Next to the element, above it
            room >= BUBBLE_MIN_HEIGHT -> Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = (maxHeight - holeTop + BUBBLE_GAP).coerceAtLeast(0.dp))
                .heightIn(max = room)

            // A big element on a small screen: against the edge with the most room,
            // just tall enough for the buttons, so it covers as little of the element as possible
            isBelow -> Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = maxHeight - safeBottom)
                .heightIn(max = BUBBLE_MIN_HEIGHT)

            else -> Modifier
                .align(Alignment.TopCenter)
                .padding(top = safeTop)
                .heightIn(max = BUBBLE_MIN_HEIGHT)
        }

        TutorialBubble(
            screenLabel = screen.label,
            step = step,
            number = Tutorial.index + 1,
            count = Tutorial.steps.size,
            isLast = Tutorial.isLastStep,
            onNext = Tutorial::next,
            onSkip = Tutorial::stop,
            modifier = bubblePlace.padding(horizontal = 16.dp),
        )
    }
}

// Card next to the hole: the tab and the step number, the title, the text, and the buttons
@Composable
private fun TutorialBubble(
    screenLabel: String,
    step: TutorialStep,
    number: Int,
    count: Int,
    // True on the very last step: "Compris" closes the tutorial
    isLast: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A new step starts at the top of its text
    val textScroll = remember(step) { ScrollState(0) }

    Surface(
        modifier = modifier
            .widthIn(max = BUBBLE_MAX_WIDTH)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Lavender),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // "DISCOVER · 2 / 7": the user knows where they are and how long it is
            Text(
                text = "$screenLabel · $number / $count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // The title and the text take the height left above the buttons, and scroll
            // when it is too short (small screen, big font): the buttons stay visible.
            // fill = false: when there is room, the bubble keeps the height of its text
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(textScroll),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
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
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // No "Passer" on the last step: "Compris" already closes the tutorial
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
            screenLabel = "DISCOVER",
            step = DiscoverSteps.first(),
            number = 1,
            count = DiscoverSteps.size,
            isLast = false,
            onNext = {},
            onSkip = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

// The smallest bubble, on a small screen: the text scrolls, the buttons stay visible
@Preview(showBackground = true, backgroundColor = 0xFF0C0C10, widthDp = 320)
@Composable
private fun TutorialBubbleSmallPreview() {
    MeloDropTheme {
        TutorialBubble(
            screenLabel = "DISCOVER",
            step = DiscoverSteps[2],
            number = 3,
            count = DiscoverSteps.size,
            isLast = false,
            onNext = {},
            onSkip = {},
            modifier = Modifier
                .padding(16.dp)
                .heightIn(max = BUBBLE_MIN_HEIGHT),
        )
    }
}
