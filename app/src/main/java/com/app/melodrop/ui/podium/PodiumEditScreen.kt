package com.app.melodrop.ui.podium

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.network.dto.TrackResult
import com.app.melodrop.ui.components.CoverImage
import com.app.melodrop.ui.components.TrackRow
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Night
import com.app.melodrop.ui.theme.Peach

// Fewer letters than this: the search does not start (same rule as in the ViewModel)
private const val MIN_QUERY_LENGTH = 2

// Connects the screen to its ViewModel
@Composable
fun PodiumEditScreen(
    onBack: () -> Unit,
    viewModel: PodiumEditViewModel = viewModel(),
) {
    // Loads the saved podium when the screen opens
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

    PodiumEditContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onRetry = viewModel::load,
        onPlaceClick = viewModel::selectPlace,
        onRemoveClick = viewModel::removePlace,
        onQueryChange = viewModel::onQueryChange,
        onTrackClick = viewModel::chooseTrack,
        onPlayClick = viewModel::togglePreview,
    )
}

// Pure UI: back arrow, title, the 3 places, then the search when a place is selected
@Composable
private fun PodiumEditContent(
    uiState: PodiumEditUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPlaceClick: (Int) -> Unit,
    onRemoveClick: (Int) -> Unit,
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
                text = "Mon podium",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Text(
            text = "Tes 3 titres préférés de tous les temps. Touche une place pour la changer.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Local copy: Kotlin can then treat it as not null in its branch
        val loadError = uiState.loadError

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

            else -> PodiumEditBody(
                uiState = uiState,
                onPlaceClick = onPlaceClick,
                onRemoveClick = onRemoveClick,
                onQueryChange = onQueryChange,
                onTrackClick = onTrackClick,
                onPlayClick = onPlayClick,
            )
        }
    }
}

// The 3 places, the messages, then the search of the selected place
@Composable
private fun PodiumEditBody(
    uiState: PodiumEditUiState,
    onPlaceClick: (Int) -> Unit,
    onRemoveClick: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onTrackClick: (TrackResult) -> Unit,
    onPlayClick: (TrackResult) -> Unit,
) {
    // A podium has no empty place in the middle: only the first empty place can be filled
    val filledCount = uiState.places.count { it != null }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            uiState.places.forEachIndexed { index, choice ->
                PlaceRow(
                    place = index + 1,
                    choice = choice,
                    isEditing = uiState.editingIndex == index,
                    // A filled place, or the first empty one. Nothing while saving
                    enabled = index <= filledCount && !uiState.isSaving,
                    onClick = { onPlaceClick(index) },
                    onRemoveClick = { onRemoveClick(index) },
                )
            }
        }

        if (uiState.isSaving) {
            Text(
                text = "Enregistrement…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Local copies: Kotlin can then treat them as not null in their branch
        val saveError = uiState.saveError
        val editingIndex = uiState.editingIndex

        if (saveError != null) {
            Text(
                text = saveError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (editingIndex != null) {
            SearchPart(
                uiState = uiState,
                place = editingIndex + 1,
                onQueryChange = onQueryChange,
                onTrackClick = onTrackClick,
                onPlayClick = onPlayClick,
                // Takes all the height left: the results scroll inside
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// One place: its number, then its song or an invitation, and a button to remove the song
@Composable
private fun PlaceRow(
    place: Int,
    // null: nobody at this place yet
    choice: PodiumChoice?,
    // True when this place is the one being changed: lavender border
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
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlaceNumber(place = place)

            if (choice != null) {
                CoverImage(
                    url = choice.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )

                // weight(1f): the texts take the room left, the button keeps its size
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = choice.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        // While the place is being changed, the line says what to do
                        text = if (isEditing) "Choisis le titre qui le remplace" else choice.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isEditing) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                IconButton(
                    onClick = onRemoveClick,
                    enabled = enabled,
                    modifier = Modifier.semantics { contentDescription = "Retirer ${choice.title} du podium" },
                ) {
                    Text(
                        text = "×",
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    text = if (enabled) "Choisir un titre" else "Place libre",
                    modifier = Modifier
                        .weight(1f)
                        // Same height as a filled place
                        .padding(vertical = 14.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// The number of the place, on the color of its step: butter, lavender, peach
@Composable
private fun PlaceNumber(place: Int) {
    val color = when (place) {
        1 -> Butter
        2 -> Lavender
        else -> Peach
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = place.toString(),
            fontFamily = Unbounded,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Night,
        )
    }
}

// The search field and its results, shown under the places while one is being changed
@Composable
private fun SearchPart(
    uiState: PodiumEditUiState,
    place: Int,
    onQueryChange: (String) -> Unit,
    onTrackClick: (TrackResult) -> Unit,
    onPlayClick: (TrackResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            label = { Text("Titre ou artiste, pour la place n°$place") },
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
            // Local copy: Kotlin can then treat it as not null in its branch
            val searchError = uiState.searchError

            when {
                uiState.isSearching -> CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))

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
}

// Short centered text under the field: hint, no result or error
@Composable
private fun InfoText(text: String, isError: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 24.dp),
        color = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun PodiumEditPreview() {
    MeloDropTheme {
        PodiumEditContent(
            uiState = PodiumEditUiState(
                isLoading = false,
                places = listOf(
                    PodiumChoice(1L, "Bohemian Rhapsody", "Queen", ""),
                    PodiumChoice(2L, "Teardrop", "Massive Attack", ""),
                    null,
                ),
                editingIndex = 2,
                query = "da",
            ),
            onBack = {},
            onRetry = {},
            onPlaceClick = {},
            onRemoveClick = {},
            onQueryChange = {},
            onTrackClick = {},
            onPlayClick = {},
        )
    }
}
