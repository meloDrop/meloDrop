package com.app.melodrop.ui.drop

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.app.melodrop.network.dto.GenreOut
import com.app.melodrop.network.dto.MyDropOut
import com.app.melodrop.network.dto.TrackResult
import com.app.melodrop.ui.components.GenreChip
import com.app.melodrop.ui.components.MeloDropTitle
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.genreColor
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime

// Connects the screen to its ViewModel
@Composable
fun DropScreen(viewModel: DropViewModel = viewModel()) {
    // Leaving the Drop tab: the music stops
    DisposableEffect(Unit) {
        onDispose { viewModel.pausePreview() }
    }

    // App sent to the background: the music stops too
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.pausePreview()
    }

    // Each time the tab opens: is a drop online, and how many likes does it have?
    LaunchedEffect(Unit) {
        viewModel.loadMyDrop()
    }

    // Back button on the genre step: back to the results, not out of the tab
    BackHandler(enabled = viewModel.uiState.selectedTrack != null && viewModel.uiState.myDrop == null) {
        viewModel.clearSelection()
    }

    DropContent(
        uiState = viewModel.uiState,
        onQueryChange = viewModel::onQueryChange,
        onPlayClick = viewModel::togglePreview,
        onTrackClick = viewModel::selectTrack,
        onGenreClick = viewModel::toggleGenre,
        onSubmit = viewModel::submitDrop,
        onChangeTrack = viewModel::clearSelection,
        onRefresh = viewModel::loadMyDrop,
    )
}

// Pure UI: shows one step at a time, from the state it receives
@Composable
private fun DropContent(
    uiState: DropUiState,
    onQueryChange: (String) -> Unit,
    onPlayClick: (TrackResult) -> Unit,
    onTrackClick: (TrackResult) -> Unit,
    onGenreClick: (Int) -> Unit,
    onSubmit: () -> Unit,
    onChangeTrack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MeloDropTitle()

        // Local copies: Kotlin can then treat them as not null in each branch
        val myDrop = uiState.myDrop
        val selectedTrack = uiState.selectedTrack

        when {
            // First answer of GET /drops/me not there yet
            uiState.isCheckingDrop -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            myDrop != null -> MyDropView(
                drop = myDrop,
                justDropped = uiState.justDropped,
                onRefresh = onRefresh,
            )

            selectedTrack != null -> GenreStep(
                uiState = uiState,
                track = selectedTrack,
                onPlayClick = onPlayClick,
                onGenreClick = onGenreClick,
                onSubmit = onSubmit,
                onChangeTrack = onChangeTrack,
            )

            else -> SearchStep(
                uiState = uiState,
                onQueryChange = onQueryChange,
                onPlayClick = onPlayClick,
                onTrackClick = onTrackClick,
            )
        }
    }
}

// Step 1: search field and results
@Composable
private fun SearchStep(
    uiState: DropUiState,
    onQueryChange: (String) -> Unit,
    onPlayClick: (TrackResult) -> Unit,
    onTrackClick: (TrackResult) -> Unit,
) {
    // Used to close the keyboard with the search key
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Ton drop du jour",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = "Choisis le titre que tu veux faire découvrir. Il restera en ligne 24 heures.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            label = { Text("Titre ou artiste") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            // The keyboard shows a search key instead of "enter"
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            // The search already runs while typing: the key only closes the keyboard
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        )

        // Takes all the space left under the field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopCenter,
        ) {
            when {
                uiState.isSearching -> CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))

                uiState.searchError != null -> InfoText(
                    text = uiState.searchError,
                    isError = true,
                )

                uiState.query.trim().length < 2 -> InfoText(
                    text = "Tape le nom d'un titre ou d'un artiste pour lancer la recherche.",
                )

                uiState.results.isEmpty() -> InfoText(
                    text = "Aucun titre trouvé pour « ${uiState.query.trim()} ».",
                )

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    // key: Compose follows each row by its Deezer id
                    items(uiState.results, key = { it.deezerTrackId }) { track ->
                        TrackRow(
                            track = track,
                            isPlaying = uiState.isPlaying && uiState.playingTrackId == track.deezerTrackId,
                            onPlayClick = { onPlayClick(track) },
                            onClick = { onTrackClick(track) },
                        )
                    }
                }
            }
        }
    }
}

// Step 2: the chosen track, its genres, and the button to drop it
@Composable
private fun GenreStep(
    uiState: DropUiState,
    track: TrackResult,
    onPlayClick: (TrackResult) -> Unit,
    onGenreClick: (Int) -> Unit,
    onSubmit: () -> Unit,
    onChangeTrack: () -> Unit,
) {
    val selectedIds = uiState.selectedGenreIds

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Scrolls on small screens, with the 13 genres
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Quel genre pour ce titre ?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // The chosen track, with its play button (no onClick: it is already chosen)
        TrackRow(
            track = track,
            isPlaying = uiState.isPlaying && uiState.playingTrackId == track.deezerTrackId,
            onPlayClick = { onPlayClick(track) },
        )

        Text(
            text = "Choisis jusqu'à $MAX_GENRES genres. Ils indiquent aux autres à quoi s'attendre.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Spinner while the list of genres arrives
        if (uiState.genres.isEmpty() && uiState.submitError == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.genres.forEach { genre ->
                val selected = genre.id in selectedIds
                GenreChoiceChip(
                    genre = genre,
                    selected = selected,
                    // At the limit, the other genres are greyed out
                    enabled = selected || selectedIds.size < MAX_GENRES,
                    onClick = { onGenreClick(genre.id) },
                )
            }
        }

        uiState.submitError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(
            onClick = onSubmit,
            // At least one genre is needed
            enabled = selectedIds.isNotEmpty() && !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("DÉPOSER CE TITRE")
            }
        }

        TextButton(
            onClick = onChangeTrack,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Changer de titre")
        }
    }
}

// Shown while a drop is online: the drop, its likes and the time left
@Composable
private fun MyDropView(
    drop: MyDropOut,
    justDropped: Boolean,
    onRefresh: () -> Unit,
) {
    // Current time, refreshed every minute for the countdown
    val now by produceState(initialValue = Instant.now()) {
        while (true) {
            delay(60_000)
            value = Instant.now()
        }
    }
    val expiresAt = OffsetDateTime.parse(drop.expiresAt).toInstant()
    val minutesLeft = Duration.between(now, expiresAt).toMinutes()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (justDropped) "C'est en ligne !" else "Ton drop du jour",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // The cover, with the genre in its top left corner
        Box(modifier = Modifier.fillMaxWidth(0.6f)) {
            AsyncImage(
                model = drop.coverUrl,
                contentDescription = "Pochette de ${drop.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.outline),
            )

            // Genre of the album, hidden when Deezer has none
            if (drop.genre != null) {
                GenreChip(
                    genre = drop.genre,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                )
            }
        }

        Text(
            text = drop.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Text(
            text = drop.artist,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Two tiles side by side, with the same width
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatTile(
                value = drop.savesCount.toString(),
                label = if (drop.savesCount == 1) "like reçu" else "likes reçus",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = timeLeftLabel(minutesLeft),
                label = "avant expiration",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
        }

        if (minutesLeft <= 0) {
            // Expired: the backend now accepts a new drop
            Text(
                text = "Ton drop a expiré. Tu peux en déposer un nouveau.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("DÉPOSER UN NOUVEAU TITRE")
            }
        } else {
            Text(
                text = "Tu pourras déposer un nouveau titre quand celui-ci aura expiré.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// A big number with a short label under it
@Composable
private fun StatTile(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Time left in a short form: "14 h 05", "45 min", or "0 min" once expired
private fun timeLeftLabel(minutes: Long): String = when {
    minutes <= 0 -> "0 min"
    minutes < 60 -> "$minutes min"
    else -> "${minutes / 60} h ${(minutes % 60).toString().padStart(2, '0')}"
}

// One track: cover, title, artist, and a play button if there is a preview.
// onClick null: the row cannot be tapped
@Composable
private fun TrackRow(
    track: TrackResult,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                // Inside the Surface: the touch effect keeps the rounded corners
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.outline),
            )

            // weight(1f): the texts take the room left, the button keeps its size
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (track.previewUrl != null) {
                PlayPauseButton(
                    isPlaying = isPlaying,
                    onClick = onPlayClick,
                    buttonSize = 40.dp,
                )
            }
        }
    }
}

// A genre to pick, in its own pastel once selected
@Composable
private fun GenreChoiceChip(
    genre: GenreOut,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val color = genreColor(genre.label)

    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(genre.label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.15f),
            selectedLabelColor = color,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            selectedBorderColor = color,
            selectedBorderWidth = 1.dp,
        ),
    )
}

// Short centered text under the field: hint, no result or error
@Composable
private fun InfoText(text: String, isError: Boolean = false) {
    Text(
        text = text,
        color = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 24.dp),
    )
}

private val previewTrack = TrackResult(3135556, "Get Lucky", "Daft Punk", "", "https://example.com")

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun DropGenreStepPreview() {
    MeloDropTheme {
        DropContent(
            uiState = DropUiState(
                isCheckingDrop = false,
                selectedTrack = previewTrack,
                genres = listOf(GenreOut(1, "Pop"), GenreOut(2, "Electro"), GenreOut(3, "Rock"), GenreOut(5, "Jazz")),
                selectedGenreIds = setOf(1, 2),
            ),
            onQueryChange = {},
            onPlayClick = {},
            onTrackClick = {},
            onGenreClick = {},
            onSubmit = {},
            onChangeTrack = {},
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun MyDropPreview() {
    MeloDropTheme {
        DropContent(
            uiState = DropUiState(
                isCheckingDrop = false,
                myDrop = MyDropOut(
                    id = 1,
                    songId = 1,
                    title = "Get Lucky",
                    artist = "Daft Punk",
                    coverUrl = "",
                    genre = "Electro",
                    // Still required by MyDropOut, removed in ticket 15
                    genres = listOf(GenreOut(1, "Pop"), GenreOut(2, "Electro")),
                    // Expires in 14 hours, whatever the day the preview is opened
                    expiresAt = Instant.now().plusSeconds(14L * 3600).toString(),
                    savesCount = 3,
                    genreReported = true,
                ),
            ),
            onQueryChange = {},
            onPlayClick = {},
            onTrackClick = {},
            onGenreClick = {},
            onSubmit = {},
            onChangeTrack = {},
            onRefresh = {},
        )
    }
}