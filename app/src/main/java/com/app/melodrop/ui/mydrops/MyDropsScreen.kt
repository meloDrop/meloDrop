package com.app.melodrop.ui.mydrops

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.network.dto.ProfileDropOut
import com.app.melodrop.ui.profile.DropsGrid
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun MyDropsScreen(
    onBack: () -> Unit,
    viewModel: MyDropsViewModel = viewModel(),
) {
    // Leaving the screen: the music stops
    DisposableEffect(Unit) {
        onDispose { viewModel.pausePreview() }
    }

    // App sent to the background (home button, screen off): the music stops too
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.pausePreview()
    }

    MyDropsContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onPlayClick = viewModel::togglePreview,
        onRetry = viewModel::load,
    )
}

// Pure UI: back arrow, title, then the grid, a spinner or an error
@Composable
private fun MyDropsContent(
    uiState: MyDropsUiState,
    onBack: () -> Unit,
    // Tap on a cover: plays or pauses its preview
    onPlayClick: (ProfileDropOut) -> Unit,
    onRetry: () -> Unit,
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
                text = "Mes drops",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // A preview could not be played
        uiState.previewError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        // Local copy: Kotlin can then treat it as not null in its branch
        val error = uiState.error

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            error != null -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = error,
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

            uiState.drops.isEmpty() -> Text(
                text = "Tu n'as encore déposé aucun titre. Ton premier drop apparaîtra ici.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // DropsGrid is not a lazy list: the Column around it does the scrolling
            else -> Column(
                // Takes the height left under the title
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = dropsCountLabel(uiState.drops.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Same grid as on the profile: covers, play buttons and "EN LIGNE" badge
                DropsGrid(
                    drops = uiState.drops,
                    playingDropId = uiState.playingDropId,
                    isPlaying = uiState.isPlaying,
                    onPlayClick = onPlayClick,
                    onlineDropId = uiState.onlineDropId,
                )
            }
        }
    }
}

// "1 titre déposé", "12 titres déposés"
private fun dropsCountLabel(count: Int): String =
    if (count <= 1) "$count titre déposé, du plus récent au plus ancien."
    else "$count titres déposés, du plus récent au plus ancien."

// Fake drops for the previews
private val previewDrop = ProfileDropOut(
    id = 1,
    songId = 1,
    title = "Numb",
    artist = "Linkin Park",
    coverUrl = "",
    genre = "Alternative",
    droppedAt = "2026-10-22T09:00:00Z",
    savesCount = 4,
)

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun MyDropsPreview() {
    MeloDropTheme {
        MyDropsContent(
            uiState = MyDropsUiState(
                isLoading = false,
                drops = listOf(
                    previewDrop,
                    previewDrop.copy(id = 2, title = "Enter Sandman"),
                    previewDrop.copy(id = 3, title = "Bohemian Rhapsody"),
                    previewDrop.copy(id = 4, title = "Dreams"),
                    previewDrop.copy(id = 5, title = "Smells Like Teen Spirit"),
                    previewDrop.copy(id = 6, title = "HUMBLE."),
                    previewDrop.copy(id = 7, title = "Levitating"),
                    previewDrop.copy(id = 8, title = "Alors on danse"),
                ),
                onlineDropId = 1,
            ),
            onBack = {},
            onPlayClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun MyDropsEmptyPreview() {
    MeloDropTheme {
        MyDropsContent(
            uiState = MyDropsUiState(isLoading = false),
            onBack = {},
            onPlayClick = {},
            onRetry = {},
        )
    }
}