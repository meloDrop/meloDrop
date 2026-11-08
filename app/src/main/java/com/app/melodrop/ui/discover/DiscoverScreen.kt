package com.app.melodrop.ui.discover

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import com.app.melodrop.ui.people.SongPeopleSheet
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Night
import com.app.melodrop.ui.tutorial.Tutorial
import com.app.melodrop.ui.tutorial.TutorialLastStep
import com.app.melodrop.ui.tutorial.TutorialSteps
import com.app.melodrop.ui.tutorial.TutorialTarget
import com.app.melodrop.ui.tutorial.tutorialTarget
import com.app.melodrop.ui.whatsnew.WhatsNewSheet
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.BoxWithConstraints

// Distance to reach, as a fraction of the card width, for a swipe to count
private const val SWIPE_THRESHOLD = 0.3f

// Tilt of the card when it reaches the threshold (degrees)
private const val MAX_TILT = 12f

// Time for the card to fly off the screen (ms)
private const val FLY_OUT_MS = 250

// Size of the three round buttons under the card
private val CHOICE_BUTTON_SIZE = 64.dp

// How much higher the Pick button sits than the two others
private val PICK_RAISE = 16.dp

// Below this height for the card, the buttons and the hint, the screen is short:
// the hint takes one line, so the cover keeps some room
private val COMPACT_DECK_HEIGHT = 600.dp

// Hint on one line, for short screens. Same order as the buttons under the card:
// Passer on the left, Pick in the middle, Liker on the right
private const val SHORT_HINT = "← Passer    ↑ Pick    Liker →"

// The three choices on a card. Each one sends the card a different way:
// skip to the left, like to the right, pick to the top
private enum class Choice { SKIP, LIKE, PICK }

// Connects the screen to its ViewModel
@Composable
fun DiscoverScreen(
    // Tap on the @username of a card: opens the profile of its author
    onAuthorClick: (Int) -> Unit,
    // Tap on the magnifying glass: opens the user search
    onSearchClick: () -> Unit,
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

    // Card whose list of droppers and likes is open, with the tab to show
    // (true = likes), or null when the list is closed
    var peopleOf by remember { mutableStateOf<Pair<FeedItem, Boolean>?>(null) }

    DiscoverContent(
        uiState = viewModel.uiState,
        onRefresh = viewModel::loadFeed,
        onSwiped = viewModel::onSwiped,
        onPicked = viewModel::onPicked,
        onPlayPause = viewModel::togglePlay,
        onAuthorClick = onAuthorClick,
        onSearchClick = onSearchClick,
        onInfoClick = viewModel::openWhatsNew,
        followsFirst = viewModel.followsFirst,
        onFollowsFirstClick = viewModel::toggleFollowsFirst,
        onPeopleClick = { drop, showLikers -> peopleOf = drop to showLikers },
    )

    // Who dropped the song of the card and who liked it (ADR-0021)
    val people = peopleOf
    if (people != null) {
        val (drop, showLikers) = people
        SongPeopleSheet(
            songId = drop.songId,
            songTitle = drop.title,
            droppers = drop.droppers.orEmpty(),
            startOnLikers = showLikers,
            onUserClick = onAuthorClick,
            onDismiss = { peopleOf = null },
        )
    }

    // "Nouveautés" sheet, over the screen: by itself after an update, or with the "i" button
    if (viewModel.showWhatsNew) {
        WhatsNewSheet(
            onDismiss = viewModel::closeWhatsNew,
            // Only when this version has a tour: the sheet offers to see it again
            onReplayTutorial = if (TutorialSteps.isNotEmpty()) viewModel::replayTutorial else null,
        )
    }

    // A tour waits and the feed is loaded: the elements to highlight are on screen
    if (viewModel.tutorialPending && !viewModel.uiState.isLoading) {
        LaunchedEffect(Unit) {
            // Waits for the next frame: every element has had time to note its place
            withFrameNanos { }
            Tutorial.start(TutorialSteps + TutorialLastStep)
            viewModel.onTutorialStarted()
        }
    }
}

// Pure UI: only displays the state it receives
@Composable
private fun DiscoverContent(
    uiState: DiscoverUiState,
    onRefresh: () -> Unit,
    onSwiped: (FeedItem, Boolean) -> Unit,
    onPicked: (FeedItem) -> Unit,
    onPlayPause: () -> Unit,
    onAuthorClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    // Tap on the "i": opens the "Nouveautés" sheet
    onInfoClick: () -> Unit,
    // True: the drops of the followed users come first
    followsFirst: Boolean = true,
    // Tap on the "Abonnements d'abord" chip
    onFollowsFirstClick: () -> Unit = {},
    // Tap on the droppers or the likes of the card: the card, and true for the likes
    onPeopleClick: (FeedItem, Boolean) -> Unit = { _, _ -> },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // The title and the chip under it, close together: the cover keeps more room
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Title on the left, "i" and search buttons on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MeloDropTitle()
                // The two buttons side by side, against the right edge
                Row {
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.tutorialTarget(TutorialTarget.INFO),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info_drop),
                            contentDescription = "Nouveautés de la version",
                            // Unspecified: the drop keeps its own pastel stripes
                            tint = Color.Unspecified,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.tutorialTarget(TutorialTarget.SEARCH),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "Rechercher un utilisateur",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }

            // The user chooses whether the drops of the followed users come first
            FollowsFirstChip(
                selected = followsFirst,
                onClick = onFollowsFirstClick,
                modifier = Modifier.tutorialTarget(TutorialTarget.FOLLOWS_FIRST),
            )
        }

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
                    onPicked = onPicked,
                    onPlayPause = onPlayPause,
                    onAuthorClick = onAuthorClick,
                    onPeopleClick = onPeopleClick,
                )
            }
        }
    }
}

// The card on top, the next one waiting behind it, the three buttons and a hint below
@Composable
private fun DropDeck(
    uiState: DiscoverUiState,
    onSwiped: (FeedItem, Boolean) -> Unit,
    onPicked: (FeedItem) -> Unit,
    onPlayPause: () -> Unit,
    onAuthorClick: (Int) -> Unit,
    onPeopleClick: (FeedItem, Boolean) -> Unit = { _, _ -> },
) {
    val drops = uiState.drops
    val top = drops.first()
    // First message found wins: swipe problem, then preview problem,
    // then the pick confirmation, then the hint
    val message = uiState.swipeError ?: uiState.previewError
    val hint = when {
        message != null -> message
        uiState.justPicked -> "Ajouté à Mes picks"
        else -> "Glisse à droite pour Liker, à gauche pour Passer\nVers le haut pour un Pick : une découverte qui te plaît"
    }
    val hintColor = when {
        message != null -> MaterialTheme.colorScheme.error
        uiState.justPicked -> Butter
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val scope = rememberCoroutineScope()
    // Shift of the card on top in pixels (0 = centered): across, and up or down.
    // remember(top.id): each new card starts again from the center
    val offsetX = remember(top.id) { Animatable(0f) }
    val offsetY = remember(top.id) { Animatable(0f) }
    // True once the card on top flies away: a second choice on it is ignored
    var isLeaving by remember(top.id) { mutableStateOf(false) }
    // Card width in pixels, known after the first layout
    var cardWidth by remember { mutableFloatStateOf(1f) }

    // The deck leaves (empty feed, error, other tab): the Pick button is no longer
    // on screen, so the tour must not point at its old place
    DisposableEffect(Unit) {
        onDispose { Tutorial.targets.remove(TutorialTarget.PICK) }
    }

    // Sends the card on top off the screen, then the choice counts.
    // Used by the swipes and by the three buttons, so both look the same
    val flyOut: (Choice) -> Unit = { choice ->
        if (!isLeaving) {
            isLeaving = true
            scope.launch {
                try {
                    // Far enough to leave the screen: left, right or up
                    when (choice) {
                        Choice.SKIP -> offsetX.animateTo(-cardWidth * 1.5f, tween(FLY_OUT_MS))
                        Choice.LIKE -> offsetX.animateTo(cardWidth * 1.5f, tween(FLY_OUT_MS))
                        Choice.PICK -> offsetY.animateTo(-cardWidth * 2.5f, tween(FLY_OUT_MS))
                    }
                } finally {
                    // Even if the animation is cut short, the choice is made
                    when (choice) {
                        Choice.SKIP -> onSwiped(top, false)
                        Choice.LIKE -> onSwiped(top, true)
                        Choice.PICK -> onPicked(top)
                    }
                }
            }
        }
    }

    // Height given to the deck: a short screen gets a one-line hint and less space
    BoxWithConstraints {
        val isCompact = maxHeight < COMPACT_DECK_HEIGHT

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 16.dp),
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
                        offsetY = offsetY,
                        width = cardWidth,
                        isLeaving = isLeaving,
                        onWidthChange = { cardWidth = it },
                        onFlyOut = flyOut,
                        onPlayPause = onPlayPause,
                        onAuthorClick = { onAuthorClick(top.authorId) },
                        onPeopleClick = { showLikers -> onPeopleClick(top, showLikers) },
                    )
                }
            }

            // Same choices as the swipes, for those who prefer to tap.
            // Aligned at the bottom: the raised Pick button stands out in the middle
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                ChoiceButton(
                    iconRes = R.drawable.ic_close,
                    description = "Passer",
                    containerColor = MaterialTheme.colorScheme.surface,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    buttonSize = CHOICE_BUTTON_SIZE,
                    enabled = !isLeaving,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    onClick = { flyOut(Choice.SKIP) },
                )
                ChoiceButton(
                    iconRes = R.drawable.ic_star,
                    description = "Pick : je découvre ce titre et il me plaît",
                    containerColor = Butter,
                    iconColor = Night,
                    buttonSize = CHOICE_BUTTON_SIZE,
                    enabled = !isLeaving,
                    onClick = { flyOut(Choice.PICK) },
                    // Raised above the two others. The tour notes the button, not the space under it
                    modifier = Modifier
                        .padding(bottom = PICK_RAISE)
                        .tutorialTarget(TutorialTarget.PICK),
                )
                ChoiceButton(
                    iconRes = R.drawable.ic_favorite,
                    description = "J'aime",
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    iconColor = MaterialTheme.colorScheme.onTertiary,
                    buttonSize = CHOICE_BUTTON_SIZE,
                    enabled = !isLeaving,
                    onClick = { flyOut(Choice.LIKE) },
                )
            }

            Text(
                // Short screen: the usual hint is replaced by its one-line version
                text = if (isCompact && message == null && !uiState.justPicked) SHORT_HINT else hint,
                style = MaterialTheme.typography.bodySmall,
                color = hintColor,
                textAlign = TextAlign.Center,
                // Always as high: the card does not move when the text changes
                minLines = if (isCompact) 1 else 2,
            )
        }
    }
}

// The card on top: follows the finger, then flies away or comes back to the center.
// Its shift is kept by DropDeck, so the buttons can move it too
@Composable
private fun SwipeableDropCard(
    drop: FeedItem,
    isPlaying: Boolean,
    offsetX: Animatable<Float, AnimationVector1D>,
    offsetY: Animatable<Float, AnimationVector1D>,
    width: Float,
    isLeaving: Boolean,
    onWidthChange: (Float) -> Unit,
    onFlyOut: (Choice) -> Unit,
    onPlayPause: () -> Unit,
    onAuthorClick: () -> Unit,
    // Tap on the droppers (false) or on the likes (true) of the card
    onPeopleClick: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .onSizeChanged { onWidthChange(it.width.toFloat()) }
            // Starts again when the width is known, and when the card starts leaving
            .pointerInput(width, isLeaving) {
                // Already leaving (swipe or button): the finger can no longer move it
                if (isLeaving) return@pointerInput

                // Drag in any direction: across for like and skip, up for pick
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // The card follows the finger, across and up or down
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    },
                    onDragEnd = {
                        // Distance moved, as a fraction of the card width
                        val progressX = offsetX.value / width
                        val progressY = offsetY.value / width
                        when {
                            // Up far enough, and more up than across: pick
                            -progressY > SWIPE_THRESHOLD && abs(progressY) > abs(progressX) ->
                                onFlyOut(Choice.PICK)
                            // Far enough to the right or to the left: same exit as the buttons
                            progressX > SWIPE_THRESHOLD -> onFlyOut(Choice.LIKE)
                            progressX < -SWIPE_THRESHOLD -> onFlyOut(Choice.SKIP)
                            // Not far enough: back to the center with a small bounce
                            else -> {
                                val bounce = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy)
                                scope.launch { offsetX.animateTo(0f, bounce) }
                                scope.launch { offsetY.animateTo(0f, bounce) }
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch { offsetX.animateTo(0f) }
                        scope.launch { offsetY.animateTo(0f) }
                    },
                )
            }
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                // Tilts toward a side swipe, like a card held by its bottom
                rotationZ = MAX_TILT * offsetX.value / (width * SWIPE_THRESHOLD)
            },
    ) {
        DropCard(
            drop = drop,
            isPlaying = isPlaying,
            onPlayPause = onPlayPause,
            onAuthorClick = onAuthorClick,
            onPeopleClick = onPeopleClick,
        )

        // 0 at rest, 1 (or -1) at the threshold, to the right (or to the left)
        val progress = offsetX.value / (width * SWIPE_THRESHOLD)
        // 0 at rest, 1 at the threshold going up
        val progressUp = -offsetY.value / (width * SWIPE_THRESHOLD)

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
        SwipeLabel(
            text = "PICK",
            color = Butter,
            visibility = progressUp.coerceIn(0f, 1f),
            tilt = 0f,
            // In the middle of the card: it rises with it
            modifier = Modifier.align(Alignment.Center),
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
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(buttonSize),
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
            onPicked = {},
            onPlayPause = {},
            onAuthorClick = {},
            onSearchClick = {},
            onInfoClick = {},
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
            onPicked = {},
            onPlayPause = {},
            onAuthorClick = {},
            onSearchClick = {},
            onInfoClick = {},
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
            onPicked = {},
            onPlayPause = {},
            onAuthorClick = {},
            onSearchClick = {},
            onInfoClick = {},
        )
    }
}