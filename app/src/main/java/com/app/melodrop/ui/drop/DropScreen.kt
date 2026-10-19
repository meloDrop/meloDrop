package com.app.melodrop.ui.drop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.app.melodrop.network.dto.TrackResult
import com.app.melodrop.ui.components.MeloDropTitle
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.MeloDropTheme

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

    DropContent(
        uiState = viewModel.uiState,
        onQueryChange = viewModel::onQueryChange,
        onPlayClick = viewModel::togglePreview,
    )
}

// Pure UI: only displays the state it receives
@Composable
private fun DropContent(
    uiState: DropUiState,
    onQueryChange: (String) -> Unit,
    onPlayClick: (TrackResult) -> Unit,
) {
    // Used to close the keyboard with the search key
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MeloDropTitle()

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
                        )
                    }
                }
            }
        }
    }
}

// One search result: cover, title, artist, and a play button if there is a preview
@Composable
private fun TrackRow(
    track: TrackResult,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
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

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun DropResultsPreview() {
    MeloDropTheme {
        DropContent(
            uiState = DropUiState(
                query = "daft punk",
                results = listOf(
                    TrackResult(3135556, "Get Lucky", "Daft Punk", "", "https://example.com"),
                    TrackResult(3135553, "One More Time", "Daft Punk", "", null),
                ),
                playingTrackId = 3135556,
                isPlaying = true,
            ),
            onQueryChange = {},
            onPlayClick = {},
        )
    }
}