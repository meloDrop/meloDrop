package com.app.melodrop.ui.discover

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun DiscoverScreen(viewModel: DiscoverViewModel = viewModel()) {
    DiscoverContent(
        uiState = viewModel.uiState,
        onRefresh = viewModel::loadFeed,
        onSwiped = viewModel::onSwiped,
    )
}

// Pure UI: only displays the state it receives
@Composable
private fun DiscoverContent(
    uiState: DiscoverUiState,
    onRefresh: () -> Unit,
    onSwiped: (FeedItem, Boolean) -> Unit,
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
                    drops = uiState.drops,
                    swipeError = uiState.swipeError,
                    onSwiped = onSwiped,
                )
            }
        }
    }
}

// The card on top, the next one waiting behind it, and a hint below
@Composable
private fun DropDeck(
    drops: List<FeedItem>,
    swipeError: String?,
    onSwiped: (FeedItem, Boolean) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
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

            val top = drops.first()
            // New drop = new key: the swipe position starts again from 0
            key(top.id) {
                SwipeableDropCard(
                    drop = top,
                    onSwiped = { liked -> onSwiped(top, liked) },
                )
            }
        }

        Text(
            text = swipeError ?: "Glisse à droite pour garder, à gauche pour passer",
            style = MaterialTheme.typography.bodySmall,
            color = if (swipeError != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// A card that follows the finger, then flies away or comes back to the center
@Composable
private fun SwipeableDropCard(
    drop: FeedItem,
    onSwiped: (liked: Boolean) -> Unit,
) {
    // Horizontal shift of the card in pixels (0 = centered)
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    // Card width in pixels, known after the first layout
    var width by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .onSizeChanged { width = it.width.toFloat() }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        // The card follows the finger
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                    },
                    onDragEnd = {
                        scope.launch {
                            val progress = offsetX.value / width
                            if (abs(progress) > SWIPE_THRESHOLD) {
                                // Far enough: the card flies off the screen, then the swipe counts
                                val liked = progress > 0
                                val target = if (liked) width * 1.5f else -width * 1.5f
                                offsetX.animateTo(target, tween(FLY_OUT_MS))
                                onSwiped(liked)
                            } else {
                                // Not far enough: back to the center with a small bounce
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
        DropCard(drop = drop)

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
        DiscoverContent(uiState = DiscoverUiState(), onRefresh = {}, onSwiped = { _, _ -> })
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
        )
    }
}