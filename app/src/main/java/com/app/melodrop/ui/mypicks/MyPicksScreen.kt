package com.app.melodrop.ui.mypicks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.app.melodrop.network.dto.LikedSongOut
import com.app.melodrop.ui.components.CoverImage
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.MeloDropTheme

// Number of covers on each line, same as on the profile
private const val COVERS_PER_ROW = 3

// Connects the screen to its ViewModel
@Composable
fun MyPicksScreen(
    onBack: () -> Unit,
    viewModel: MyPicksViewModel = viewModel(),
) {
    // Leaving the screen: the music stops
    DisposableEffect(Unit) {
        onDispose { viewModel.pausePreview() }
    }

    // App sent to the background (home button, screen off): the music stops too
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.pausePreview()
    }

    MyPicksContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onPlayClick = viewModel::togglePreview,
        onRetry = viewModel::load,
    )
}

// Pure UI: back arrow, title, then the grid, a spinner or an error
@Composable
private fun MyPicksContent(
    uiState: MyPicksUiState,
    onBack: () -> Unit,
    // Tap on a cover: plays or pauses its preview
    onPlayClick: (LikedSongOut) -> Unit,
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
                text = "Mes picks",
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

            uiState.picks.isEmpty() -> Text(
                text = "Aucun pick pour l'instant. Dans Discover, l'étoile garde un titre que tu découvres et qui te plaît.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Lazy grid: only the covers on screen are built, even with hundreds of picks.
            // It scrolls by itself, so no verticalScroll around it
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(COVERS_PER_ROW),
                // Takes the height left under the title
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // The count takes the whole line, above the covers
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = picksCountLabel(uiState.picks.size),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // The key lets the grid follow each pick when the list changes
                items(uiState.picks, key = { it.songId }) { pick ->
                    PickThumbnail(
                        pick = pick,
                        // Pause icon only on the cover whose preview plays
                        isPlaying = uiState.isPlaying && pick.songId == uiState.playingPickId,
                        onPlayClick = { onPlayClick(pick) },
                    )
                }
            }
        }
    }
}

// One pick: its cover with a play button, its title, and who made the user discover it.
// Shared with the "Mes picks" tab of the profile
@Composable
internal fun PickThumbnail(
    pick: LikedSongOut,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box {
            // Downloads the image from its URL, grey square while it loads
            CoverImage(
                url = pick.coverUrl,
                contentDescription = "Pochette de ${pick.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    // The whole cover can be tapped, not only the small button
                    .clickable(onClick = onPlayClick),
            )

            // Small play button in the bottom right corner of the cover
            PlayPauseButton(
                isPlaying = isPlaying,
                onClick = onPlayClick,
                buttonSize = 32.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp),
            )
        }

        Column {
            Text(
                text = pick.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Same line as in "Mes likes": rescued from the skips, or the author of the drop
            val origin = when {
                pick.likedLate -> "Récupéré dans les skips"
                pick.authorUsername != null -> "drop par @${pick.authorUsername}"
                // Author's account deleted: nothing to show (ADR-0013)
                else -> null
            }
            if (origin != null) {
                Text(
                    text = origin,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (pick.likedLate) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// "1 pick", "12 picks"
private fun picksCountLabel(count: Int): String =
    if (count <= 1) "$count pick, du plus récent au plus ancien."
    else "$count picks, du plus récent au plus ancien."

// Fake pick for the previews
private val previewPick = LikedSongOut(
    songId = 1,
    title = "Midnight City",
    artist = "M83",
    coverUrl = "",
    likedAt = "2026-11-02T18:00:00Z",
    authorId = 2,
    authorUsername = "elliot",
    isPick = true,
)

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun MyPicksPreview() {
    MeloDropTheme {
        MyPicksContent(
            uiState = MyPicksUiState(
                isLoading = false,
                picks = listOf(
                    previewPick,
                    previewPick.copy(songId = 2, title = "Teardrop"),
                    previewPick.copy(songId = 3, title = "Nightcall", authorUsername = "noe"),
                    previewPick.copy(songId = 4, title = "Sunset Lover", likedLate = true),
                    previewPick.copy(songId = 5, title = "Alright"),
                    previewPick.copy(songId = 6, title = "Intro", authorUsername = null),
                    previewPick.copy(songId = 7, title = "Diamond Veins"),
                ),
                playingPickId = 2,
                isPlaying = true,
            ),
            onBack = {},
            onPlayClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun MyPicksEmptyPreview() {
    MeloDropTheme {
        MyPicksContent(
            uiState = MyPicksUiState(isLoading = false),
            onBack = {},
            onPlayClick = {},
            onRetry = {},
        )
    }
}
