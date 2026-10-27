package com.app.melodrop.ui.discover

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.R
import com.app.melodrop.network.dto.FeedItem
import com.app.melodrop.ui.components.MeloDropTitle
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import kotlinx.coroutines.launch
import kotlin.math.abs

// Distance to reach, as a fraction of the card width, for a swipe to count
private const val SWIPE_THRESHOLD = 0.3f

// Tilt of the card when it reaches the threshold (degrees)
private const val MAX_TILT = 12f

// Time for the card to fly off the screen (ms)
private const val FLY_OUT_MS = 250

// Connects the screen to its ViewModel
@Composable
fun DiscoverScreen(
    // Tap on the @username of a card: opens the profile of its author
    onAuthorClick: (Int) -> Unit,
    viewModel: DiscoverViewModel = viewModel(),
) {
    // Leaving the Discover tab: the music stops
    DisposableEffect(Unit) {
        onDispose { viewModel.pausePreview() }
    }

    // App sent to the background (home button, screen off): the music stops too
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.pausePreview()
    }

    DiscoverContent(
        uiState = viewModel.uiState,
        onRefresh = viewModel::loadFeed,
        onSwiped = viewModel::onSwiped,
        onPlayPause = viewModel::togglePlay,
        onAuthorClick = onAuthorClick,
    )
}

// Pure UI: only displays the state it receives
@Composable
private fun DiscoverContent(
    uiState: DiscoverUiState,
    onRefresh: () -> Unit,
    onSwiped: (FeedItem, Boolean) -> Unit,
    onPlayPause: () -> Unit,
    onAuthorClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MeloDropTitle()

        // Takes all the space left under the title, content centered
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()

                uiState.error != null -> FeedMessage(
                    title = "Le fil n'a pas pu se charger",
                    text = uiState.error,
                    buttonLabel = "RÉESSAYER",
                    onClick = onRefresh,
                )

                uiState.drops.isEmpty() -> FeedMessage(
                    title = "Plus de drop à découvrir",
                    text = "Tu as fait le tour. Reviens plus tard, de nouveaux titres sont déposés chaque jour.",
                    buttonLabel = "ACTUALISER",
                    onClick = onRefresh,
                )

                else -> DropDeck(
                    uiState = uiState,
                    onSwiped = onSwiped,
                    onPlayPause = onPlayPause,
                    onAuthorClick = onAuthorClick,
                )
            }
        }
    }
}

// The card on top, the next one waiting behind it, the two buttons and a hint below
@Composable
private fun DropDeck(
    uiState: DiscoverUiState,
    onSwiped: (FeedItem, Boolean) -> Unit,
    onPlayPause: () -> Unit,
    onAuthorClick: (Int) -> Unit,
) {
    val drops = uiState.drops
    val top = drops.first()
    // First message found wins: swipe problem, then preview problem, then the hint
    val message = uiState.swipeError ?: uiState.previewError
    val hint = message ?: "Glisse à droite pour garder, à gauche pour passer"

    val scope = rememberCoroutineScope()
    // Horizontal shift of the card on top in pixels (0 = centered).
    // remember(top.id): each new card starts again from the center
    val offsetX = remember(top.id) { Animatable(0f) }
    // True once the card on top flies away: a second choice on it is ignored
    var isLeaving by remember(top.id) { mutableStateOf(false) }
    // Card width in pixels, known after the first layout
    var cardWidth by remember { mutableFloatStateOf(1f) }

    // Sends the card on top off the screen, then the choice counts.
    // Used by the swipe and by the two buttons, so both look the same
    val flyOut: (Boolean) -> Unit = { liked ->
        if (!isLeaving) {
            isLeaving = true
            scope.launch {
                val target = if (liked) cardWidth * 1.5f else -cardWidth * 1.5f
                try {
                    offsetX.animateTo(target, tween(FLY_OUT_MS))
                } finally {
                    // Even if the animation is cut short, the choice is made
                    onSwiped(top, liked)
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.weight(1f, fill = false),
            contentAlignment = Alignment.Center,
        ) {
            // Next card: a bit smaller, seen when the top one moves away
            drops.getOrNull(1)?.let { next ->
                DropCard(
                    drop = next,
                    modifier = Modifier.graphicsLayer {
                        scaleX = 0.95f
                        scaleY = 0.95f
                    },
                )
            }

            // New drop = new key: the gestures start again for the new card
            key(top.id) {
                SwipeableDropCard(
                    drop = top,
                    isPlaying = uiState.isPlaying,
                    offsetX = offsetX,
                    width = cardWidth,
                    isLeaving = isLeaving,
                    onWidthChange = { cardWidth = it },
                    onFlyOut = flyOut,
                    onPlayPause = onPlayPause,
                    onAuthorClick = { onAuthorClick(top.authorId) },
                )
            }
        }

        // Same choices as the swipe, for those who prefer to tap
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChoiceButton(
                iconRes = R.drawable.ic_close,
                description = "Passer",
                containerColor = MaterialTheme.colorScheme.surface,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                buttonSize = 56.dp,
                enabled = !isLeaving,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                onClick = { flyOut(false) },
            )
            ChoiceButton(
                iconRes = R.drawable.ic_favorite,
                description = "J'aime",
                containerColor = MaterialTheme.colorScheme.tertiary,
                iconColor = MaterialTheme.colorScheme.onTertiary,
                buttonSize = 64.dp,
                enabled = !isLeaving,
                onClick = { flyOut(true) },
            )
        }

        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = if (message != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// The card on top: follows the finger, then flies away or comes back to the center.
// Its shift is kept by DropDeck, so the buttons can move it too
@Composable
private fun SwipeableDropCard(
    drop: FeedItem,
    isPlaying: Boolean,
    offsetX: Animatable<Float, AnimationVector1D>,
    width: Float,
    isLeaving: Boolean,
    onWidthChange: (Float) -> Unit,
    onFlyOut: (liked: Boolean) -> Unit,
    onPlayPause: () -> Unit,
    onAuthorClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .onSizeChanged { onWidthChange(it.width.toFloat()) }
            // Starts again when the width is known, and when the card starts leaving
            .pointerInput(width, isLeaving) {
                // Already leaving (swipe or button): the finger can no longer move it
                if (isLeaving) return@pointerInput

                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        // The card follows the finger
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                    },
                    onDragEnd = {
                        val progress = offsetX.value / width
                        if (abs(progress) > SWIPE_THRESHOLD) {
                            // Far enough: same exit as the buttons
                            onFlyOut(progress > 0)
                        } else {
                            // Not far enough: back to the center with a small bounce
                            scope.launch {
                                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch { offsetX.animateTo(0f) }
                    },
                )
            }
            .graphicsLayer {
                translationX = offsetX.value
                // Tilts toward the swipe, like a card held by its bottom
                rotationZ = MAX_TILT * offsetX.value / (width * SWIPE_THRESHOLD)
            },
    ) {
        DropCard(
            drop = drop,
            isPlaying = isPlaying,
            onPlayPause = onPlayPause,
            onAuthorClick = onAuthorClick,
        )

        // 0 at rest, 1 (or -1) at the threshold
        val progress = offsetX.value / (width * SWIPE_THRESHOLD)

        SwipeLabel(
            text = "J'AIME",
            color = Mint,
            visibility = progress.coerceIn(0f, 1f),
            tilt = -MAX_TILT,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(32.dp),
        )
        SwipeLabel(
            text = "PASSER",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            visibility = (-progress).coerceIn(0f, 1f),
            tilt = MAX_TILT,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(32.dp),
        )
    }
}

// Stamp shown on the card while it moves
@Composable
private fun SwipeLabel(
    text: String,
    color: Color,
    visibility: Float,
    tilt: Float,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.graphicsLayer {
            alpha = visibility
            rotationZ = tilt
        },
        shape = RoundedCornerShape(12.dp),
        // Dark and see-through: the text stays readable on any cover
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
        border = BorderStroke(2.dp, color),
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}

// Round button under the card, with an icon in its middle
@Composable
private fun ChoiceButton(
    @DrawableRes iconRes: Int,
    description: String,
    containerColor: Color,
    iconColor: Color,
    buttonSize: Dp,
    enabled: Boolean,
    onClick: () -> Unit,
    border: BorderStroke? = null,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(buttonSize),
        shape = CircleShape,
        color = containerColor,
        border = border,
    ) {
        Icon(
            painter = painterResource(iconRes),
            // Read aloud by TalkBack, since the button has no text
            contentDescription = description,
            tint = iconColor,
            // The icon takes the middle half of the button
            modifier = Modifier.padding(buttonSize / 4),
        )
    }
}

// Centered message with a button: used for errors and the empty feed
@Composable
private fun FeedMessage(
    title: String,
    text: String,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onClick) {
            Text(buttonLabel)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun DiscoverEmptyPreview() {
    MeloDropTheme {
        DiscoverContent(
            uiState = DiscoverUiState(),
            onRefresh = {},
            onSwiped = { _, _ -> },
            onPlayPause = {},
            onAuthorClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun DiscoverErrorPreview() {
    MeloDropTheme {
        DiscoverContent(
            uiState = DiscoverUiState(error = "Impossible de joindre le serveur."),
            onRefresh = {},
            onSwiped = { _, _ -> },
            onPlayPause = {},
            onAuthorClick = {},
        )
    }
}

// Fixed height: the cover shrinks to the space left, so it needs a limited height
@Preview(showBackground = true, backgroundColor = 0xFF0C0C10, heightDp = 800)
@Composable
private fun DiscoverDeckPreview() {
    MeloDropTheme {
        DiscoverContent(
            uiState = DiscoverUiState(
                drops = listOf(
                    FeedItem(
                        id = 1,
                        songId = 1,
                        title = "Numb",
                        artist = "Linkin Park",
                        // No network in the preview: the grey square shows instead
                        coverUrl = "",
                        genre = "Rock",
                        authorId = 2,
                        authorUsername = "elliot",
                        savesCount = 3,
                    ),
                ),
            ),
            onRefresh = {},
            onSwiped = { _, _ -> },
            onPlayPause = {},
            onAuthorClick = {},
        )
    }
}
