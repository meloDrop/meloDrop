package com.app.melodrop.ui.likes

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.app.melodrop.network.dto.LikedSongOut
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun LikesScreen(
    onBack: () -> Unit,
    viewModel: LikesViewModel = viewModel(),
) {
    // Leaving the screen: the music stops
    DisposableEffect(Unit) {
        onDispose { viewModel.pausePreview() }
    }

    // App sent to the background: the music stops too
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.pausePreview()
    }

    LikesContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onPlayClick = viewModel::togglePreview,
        onRemove = viewModel::remove,
        onRetry = viewModel::load,
    )
}

// Pure UI: back arrow, title, then the list, a spinner or an error
@Composable
private fun LikesContent(
    uiState: LikesUiState,
    onBack: () -> Unit,
    onPlayClick: (LikedSongOut) -> Unit,
    onRemove: (LikedSongOut) -> Unit,
    onRetry: () -> Unit,
) {
    // Song waiting for the confirmation, null when no dialog is open
    var songToRemove by remember { mutableStateOf<LikedSongOut?>(null) }

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
                text = "Mes likes",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        uiState.message?.let { message ->
            Text(
                text = message,
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

            uiState.likes.isEmpty() -> Text(
                text = "Aucun titre pour l'instant. Glisse une carte à droite dans Discover pour garder un titre ici.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            else -> {
                Text(
                    text = if (uiState.likes.size == 1) "1 titre gardé grâce à meloDrop."
                    else "${uiState.likes.size} titres gardés grâce à meloDrop.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Only the visible rows are built: the list can grow long
                LazyColumn(
                    // Takes the height left under the title
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // The key lets Compose follow each row when one is removed
                    items(uiState.likes, key = { it.songId }) { song ->
                        LikedSongRow(
                            song = song,
                            isPlaying = uiState.isPlaying && uiState.playingSongId == song.songId,
                            onPlayClick = { onPlayClick(song) },
                            onRemoveClick = { songToRemove = song },
                            // Slides the rows below up when one is removed
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    // Removing asks for a confirmation: a liked song cannot be liked again
    // once its drop has been swiped
    songToRemove?.let { song ->
        AlertDialog(
            onDismissRequest = { songToRemove = null },
            title = { Text("Retirer ce titre ?") },
            text = { Text("« ${song.title} » de ${song.artist} va sortir de tes likes.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        songToRemove = null
                        onRemove(song)
                    },
                ) {
                    Text("RETIRER")
                }
            },
            dismissButton = {
                TextButton(onClick = { songToRemove = null }) {
                    Text("ANNULER")
                }
            },
        )
    }
}

// One liked song: cover, title, artist, play button and remove button
@Composable
private fun LikedSongRow(
    song: LikedSongOut,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Downloads the image from its URL, grey square while it loads
        AsyncImage(
            model = song.coverUrl,
            contentDescription = "Pochette de ${song.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.outline),
        )

        // Takes the space left, so the buttons stay on the right
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Who made the user discover the song; nothing if the author's
            // account was deleted (ADR-0013)
            if (song.authorUsername != null) {
                Text(
                    text = "drop par @${song.authorUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        PlayPauseButton(
            isPlaying = isPlaying,
            onClick = onPlayClick,
            buttonSize = 40.dp,
        )

        IconButton(
            onClick = onRemoveClick,
            modifier = Modifier.semantics { contentDescription = "Retirer ${song.title}" },
        ) {
            Text(
                text = "✕",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val previewSongs = listOf(
    LikedSongOut(12, "Enter Sandman (Remastered 2021)", "Metallica", "", "2026-10-20T14:00:00Z"),
    LikedSongOut(9, "Bohemian Rhapsody", "Queen", "", "2026-10-19T11:00:00Z"),
    LikedSongOut(7, "So What", "Miles Davis", "", "2026-10-19T10:00:00Z"),
)

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun LikesScreenPreview() {
    MeloDropTheme {
        LikesContent(
            uiState = LikesUiState(
                isLoading = false,
                likes = previewSongs,
                playingSongId = 9,
                isPlaying = true,
            ),
            onBack = {},
            onPlayClick = {},
            onRemove = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun LikesEmptyPreview() {
    MeloDropTheme {
        LikesContent(
            uiState = LikesUiState(isLoading = false),
            onBack = {},
            onPlayClick = {},
            onRemove = {},
            onRetry = {},
        )
    }
}
