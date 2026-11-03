package com.app.melodrop.ui.skips

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
import com.app.melodrop.R
import com.app.melodrop.network.dto.SkippedSongOut
import com.app.melodrop.ui.components.CoverImage
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun SkipsScreen(
    onBack: () -> Unit,
    // Tap on "drop par @username": opens the profile of the author
    onAuthorClick: (Int) -> Unit,
    viewModel: SkipsViewModel = viewModel(),
) {
    // Leaving the screen: the music stops
    DisposableEffect(Unit) {
        onDispose { viewModel.pausePreview() }
    }

    // App sent to the background: the music stops too
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.pausePreview()
    }

    SkipsContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onPlayClick = viewModel::togglePreview,
        onRescue = viewModel::rescue,
        onRetry = viewModel::load,
        onAuthorClick = onAuthorClick,
    )
}

// Pure UI: back arrow, title, then the list, a spinner or an error
@Composable
private fun SkipsContent(
    uiState: SkipsUiState,
    onBack: () -> Unit,
    onPlayClick: (SkippedSongOut) -> Unit,
    onRescue: (SkippedSongOut) -> Unit,
    onRetry: () -> Unit,
    onAuthorClick: (Int) -> Unit,
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
                text = "Mes skips",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Text(
            text = "Un skip par erreur ? Repêche le titre avec le cœur. Si son drop est encore en ligne, l'auteur gagne un like.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Where a rescued song went (mint), or a problem (red)
        uiState.message?.let { message ->
            Text(
                text = message,
                color = if (uiState.isErrorMessage) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.tertiary,
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

            uiState.skips.isEmpty() -> Text(
                text = "Aucun titre skippé ces 7 derniers jours.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Only the visible rows are built: the list can grow long
            else -> LazyColumn(
                // Takes the height left under the title
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // The key lets Compose follow each row when one is rescued
                items(uiState.skips, key = { it.dropId }) { song ->
                    SkippedSongRow(
                        song = song,
                        isPlaying = uiState.isPlaying && uiState.playingSongId == song.songId,
                        onPlayClick = { onPlayClick(song) },
                        onRescueClick = { onRescue(song) },
                        onAuthorClick = onAuthorClick,
                        // Slides the rows below up when one is rescued
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

// One skipped song: cover, title, artist, author, drop status, play and rescue buttons
@Composable
private fun SkippedSongRow(
    song: SkippedSongOut,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onRescueClick: () -> Unit,
    onAuthorClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Downloads the image from its URL, grey square while it loads
        CoverImage(
            url = song.coverUrl,
            contentDescription = "Pochette de ${song.title}",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
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
            Text(
                text = "drop par @${song.authorUsername}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                // Tap: opens the profile of the author
                modifier = Modifier.clickable { onAuthorClick(song.authorId) },
            )
            // Tells before the rescue whether the author will get a like
            Text(
                text = if (song.dropActive) "Drop en ligne" else "Drop expiré",
                style = MaterialTheme.typography.labelSmall,
                color = if (song.dropActive) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        PlayPauseButton(
            isPlaying = isPlaying,
            onClick = onPlayClick,
            buttonSize = 40.dp,
        )

        // Same heart as the "J'aime" button of Discover: a mint round button
        Surface(
            onClick = onRescueClick,
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_favorite),
                // Read aloud by TalkBack, since the button has no text
                contentDescription = "Repêcher ${song.title}",
                tint = MaterialTheme.colorScheme.onTertiary,
                // The heart takes the middle of the button
                modifier = Modifier.padding(10.dp),
            )
        }
    }
}

private val previewSkips = listOf(
    SkippedSongOut(3, 1, "Numb", "Linkin Park", "", "2026-10-29T14:00:00Z", 2, "elliot", true),
    SkippedSongOut(
        27,
        17,
        "Dreams (2004 Remaster)",
        "Fleetwood Mac",
        "",
        "2026-10-24T11:00:00Z",
        2,
        "elliot",
        false
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun SkipsScreenPreview() {
    MeloDropTheme {
        SkipsContent(
            uiState = SkipsUiState(
                isLoading = false,
                skips = previewSkips,
                message = "« Levitating » est dans tes likes. @sam gagne un like.",
            ),
            onBack = {},
            onPlayClick = {},
            onRescue = {},
            onRetry = {},
            onAuthorClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun SkipsEmptyPreview() {
    MeloDropTheme {
        SkipsContent(
            uiState = SkipsUiState(isLoading = false),
            onBack = {},
            onPlayClick = {},
            onRescue = {},
            onRetry = {},
            onAuthorClick = {},
        )
    }
}