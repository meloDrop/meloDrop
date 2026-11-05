package com.app.melodrop.ui.labels

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.network.dto.LabeledSongOut
import com.app.melodrop.network.dto.TrackResult
import com.app.melodrop.ui.components.TrackRow
import com.app.melodrop.ui.profile.SONG_LABELS
import com.app.melodrop.ui.profile.SongLabel
import com.app.melodrop.ui.profile.songLabel
import com.app.melodrop.ui.theme.MeloDropTheme

// Fewer letters than this: the search does not start (same rule as in the ViewModel)
private const val MIN_QUERY_LENGTH = 2

// Connects the screen to its ViewModel
@Composable
fun LabelsEditScreen(
    onBack: () -> Unit,
    viewModel: LabelsEditViewModel = viewModel(),
) {
    // Loads the saved songs when the screen opens
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    // Leaving the screen: the music stops
    DisposableEffect(Unit) {
        onDispose { viewModel.pausePreview() }
    }

    // App sent to the background (home button, screen off): the music stops too
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.pausePreview()
    }

    // While a label is being changed, back closes the search instead of the screen
    val editingLabel = viewModel.uiState.editingLabel
    BackHandler(enabled = editingLabel != null) {
        if (editingLabel != null) viewModel.selectLabel(editingLabel)
    }

    LabelsEditContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onRetry = viewModel::load,
        onLabelClick = viewModel::selectLabel,
        onRemoveClick = viewModel::removeLabel,
        onQueryChange = viewModel::onQueryChange,
        onTrackClick = viewModel::chooseTrack,
        onPlayClick = viewModel::togglePreview,
    )
}

// Pure UI: back arrow and title, then the labels, or the search of the label being changed
@Composable
private fun LabelsEditContent(
    uiState: LabelsEditUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onLabelClick: (String) -> Unit,
    onRemoveClick: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onTrackClick: (TrackResult) -> Unit,
    onPlayClick: (TrackResult) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.semantics { contentDescription = "Retour" },
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = "Mes chansons",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Text(
            text = "Un son par label. Touche un label pour choisir le son qui va dessous.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Local copies: Kotlin can then treat them as not null in their branch
        val loadError = uiState.loadError
        val saveError = uiState.saveError
        val editing = uiState.editingLabel?.let { songLabel(it) }

        if (uiState.isSaving) {
            Text(
                text = "Enregistrement…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (saveError != null) {
            Text(
                text = saveError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            loadError != null -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = loadError,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 24.dp),
                ) {
                    Text("RÉESSAYER")
                }
            }

            // A label is being changed: only this label, then its search
            editing != null -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LabelRow(
                    label = editing,
                    song = uiState.songs[editing.code],
                    isEditing = true,
                    enabled = !uiState.isSaving,
                    onClick = { onLabelClick(editing.code) },
                    onRemoveClick = { onRemoveClick(editing.code) },
                )
                SearchPart(
                    uiState = uiState,
                    onQueryChange = onQueryChange,
                    onTrackClick = onTrackClick,
                    onPlayClick = onPlayClick,
                )
            }

            // All the labels, in the order of the Mixtape
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(SONG_LABELS, key = { it.code }) { label ->
                    LabelRow(
                        label = label,
                        song = uiState.songs[label.code],
                        isEditing = false,
                        enabled = !uiState.isSaving,
                        onClick = { onLabelClick(label.code) },
                        onRemoveClick = { onRemoveClick(label.code) },
                    )
                }
            }
        }
    }
}

// One label: a dot of its color, its text, its current song, and a button to remove the song
@Composable
private fun LabelRow(
    label: SongLabel,
    // null: no song under this label yet
    song: LabeledSongOut?,
    // True when this label is the one being changed: lavender border
    isEditing: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = if (isEditing) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(label.color, CircleShape),
            )

            // weight(1f): the texts take the room left, the button keeps its size
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
            ) {
                Text(
                    text = label.text,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = when {
                        isEditing -> "Choisis le son qui va sous ce label"
                        song != null -> "${song.title} · ${song.artist}"
                        else -> "Aucun son pour l'instant"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEditing) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (song != null) {
                IconButton(
                    onClick = onRemoveClick,
                    enabled = enabled,
                    modifier = Modifier.semantics { contentDescription = "Retirer ${song.title} de ce label" },
                ) {
                    Text(
                        text = "×",
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// The search field and its results, under the label being changed
@Composable
private fun SearchPart(
    uiState: LabelsEditUiState,
    onQueryChange: (String) -> Unit,
    onTrackClick: (TrackResult) -> Unit,
    onPlayClick: (TrackResult) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

        // Local copy: Kotlin can then treat it as not null in its branch
        val searchError = uiState.searchError

        when {
            uiState.isSearching -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
            }

            searchError != null -> InfoText(text = searchError, isError = true)

            uiState.query.trim().length < MIN_QUERY_LENGTH -> InfoText(
                text = "Tape le nom d'un titre ou d'un artiste pour lancer la recherche.",
            )

            uiState.results.isEmpty() -> InfoText(
                text = "Aucun titre trouvé pour « ${uiState.query.trim()} ».",
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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

// Short centered text under the field: hint, no result or error
@Composable
private fun InfoText(text: String, isError: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        color = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun LabelsEditPreview() {
    MeloDropTheme {
        LabelsEditContent(
            uiState = LabelsEditUiState(
                isLoading = false,
                songs = mapOf(
                    "party" to LabeledSongOut("party", 1, 1L, "One More Time", "Daft Punk", ""),
                    "cry" to LabeledSongOut("cry", 2, 2L, "Hurt", "Johnny Cash", ""),
                ),
            ),
            onBack = {},
            onRetry = {},
            onLabelClick = {},
            onRemoveClick = {},
            onQueryChange = {},
            onTrackClick = {},
            onPlayClick = {},
        )
    }
}
