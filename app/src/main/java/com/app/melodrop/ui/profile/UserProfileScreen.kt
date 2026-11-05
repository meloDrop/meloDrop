package com.app.melodrop.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.app.melodrop.ui.components.CoverImage
import com.app.melodrop.network.dto.LabeledSongOut
import com.app.melodrop.network.dto.PodiumSongOut
import com.app.melodrop.network.dto.ProfileDropOut
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.StatsOut
import com.app.melodrop.ui.components.GenreChip
import com.app.melodrop.ui.components.avatarColor
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun UserProfileScreen(
    // Id of the user, taken from the route "user/{userId}"
    userId: Int,
    onBack: () -> Unit,
    viewModel: UserProfileViewModel = viewModel(),
) {
    // Loads the profile when the screen opens (and again if the id changes)
    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    // Leaving the profile: the music stops
    DisposableEffect(Unit) {
        onDispose { viewModel.pausePreview() }
    }

    // App sent to the background (home button, screen off): the music stops too
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.pausePreview()
    }

    UserProfileContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onRetry = { viewModel.load(userId) },
        onPlayClick = viewModel::togglePreview,
        onPodiumPlayClick = viewModel::togglePodiumPreview,
        onLabeledSongPlayClick = viewModel::toggleLabeledSongPreview,
    )
}

// Pure UI: back arrow, then the profile, a spinner or an error
@Composable
private fun UserProfileContent(
    uiState: UserProfileUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    // Tap on a cover: plays or pauses its preview
    onPlayClick: (ProfileDropOut) -> Unit,
    // Same, for a cover of the podium
    onPodiumPlayClick: (PodiumSongOut) -> Unit,
    // Same, for a cover of "Ses chansons"
    onLabeledSongPlayClick: (LabeledSongOut) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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

        // Local copies: Kotlin can then treat them as not null in their branch
        val profile = uiState.profile
        val error = uiState.error

        when {
            profile != null -> UserProfileBody(
                profile = profile,
                playingDropId = uiState.playingDropId,
                isPlaying = uiState.isPlaying,
                previewError = uiState.previewError,
                onPlayClick = onPlayClick,
                playingPodiumSongId = uiState.playingPodiumSongId,
                onPodiumPlayClick = onPodiumPlayClick,
                playingLabel = uiState.playingLabel,
                onLabeledSongPlayClick = onLabeledSongPlayClick,
            )

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
        }
    }
}

// The profile of another user: the header stays at the top, the Mixtape and the activity slide under it
@Composable
private fun UserProfileBody(
    profile: ProfileOut,
    // Drop whose preview is loaded in the player, null if none
    playingDropId: Int?,
    // True when the loaded preview plays
    isPlaying: Boolean,
    previewError: String?,
    onPlayClick: (ProfileDropOut) -> Unit,
    // Song of the podium whose preview is loaded, null if none
    playingPodiumSongId: Int?,
    onPodiumPlayClick: (PodiumSongOut) -> Unit,
    // Label whose song is loaded in the player, null if none
    playingLabel: String?,
    onLabeledSongPlayClick: (LabeledSongOut) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // No menu: only the user's own profile has one
        ProfileHeader(profile = profile)

        ProfilePages(
            // The line under the labels takes the color of the avatar
            accent = avatarColor(profile.avatarColor),
            mixtape = {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // A preview could not be played
                    if (previewError != null) {
                        Text(
                            text = previewError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    // No "Modifier" button: only the user can change their own podium
                    PodiumCard(
                        title = "SES TITRES PRÉFÉRÉS",
                        podium = profile.podium,
                        emptyText = "@${profile.username} n'a pas encore fait sa mixtape.",
                        playingSongId = playingPodiumSongId,
                        isPlaying = isPlaying,
                        onPlayClick = onPodiumPlayClick,
                    )

                    // Hidden while the user has no labeled song: nothing to show to the others
                    if (profile.labeledSongs.isNotEmpty()) {
                        LabeledSongsSection(
                            title = "SES CHANSONS",
                            labeledSongs = profile.labeledSongs,
                            playingLabel = playingLabel,
                            isPlaying = isPlaying,
                            onPlayClick = onLabeledSongPlayClick,
                        )
                    }
                }
            },
            activity = {
                UserActivityPage(
                    profile = profile,
                    playingDropId = playingDropId,
                    isPlaying = isPlaying,
                    previewError = previewError,
                    onPlayClick = onPlayClick,
                )
            },
            // Takes all the height left under the header
            modifier = Modifier.weight(1f),
        )
    }
}

// The activity page of another user: the counters, the drop of the day and the last drops
@Composable
private fun UserActivityPage(
    profile: ProfileOut,
    playingDropId: Int?,
    isPlaying: Boolean,
    previewError: String?,
    onPlayClick: (ProfileDropOut) -> Unit,
) {
    // No scroll here: the page already scrolls inside ProfilePages
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // No click on the likes: the "Mes likes" list of the others is private
        StatsRow(stats = profile.stats)

        // A preview could not be played
        if (previewError != null) {
            Text(
                text = previewError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("DROP DU JOUR")

            val currentDrop = profile.currentDrop
            if (currentDrop != null) {
                CurrentDropCard(
                    drop = currentDrop,
                    isPlaying = isPlaying && currentDrop.id == playingDropId,
                    onPlayClick = { onPlayClick(currentDrop) },
                )
            } else {
                Text(
                    text = "Pas de drop en ligne aujourd'hui.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("DERNIERS DROPS")

            if (profile.recentDrops.isEmpty()) {
                Text(
                    text = "Aucun drop passé pour l'instant.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                DropsGrid(
                    drops = profile.recentDrops,
                    playingDropId = playingDropId,
                    isPlaying = isPlaying,
                    onPlayClick = onPlayClick,
                )
            }
        }
    }
}

// The drop of the day: cover, song, genre, number of likes and a play button
@Composable
private fun CurrentDropCard(
    drop: ProfileDropOut,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Downloads the image from its URL, grey square while it loads
            CoverImage(
                url = drop.coverUrl,
                contentDescription = "Pochette de ${drop.title}",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp)),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = drop.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = drop.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Genre of the album, hidden when Deezer has none
                val genre = drop.genre
                if (genre != null) {
                    GenreChip(genre = genre)
                }

                Text(
                    text = keptByLabel(drop.savesCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            PlayPauseButton(
                isPlaying = isPlaying,
                onClick = onPlayClick,
                buttonSize = 44.dp,
            )
        }
    }
}

// "Gardé par 14 personnes", like "23 personnes l'ont ajouté" on the Stage 3 mockup
private fun keptByLabel(count: Int): String = when (count) {
    0 -> "Pas encore liké"
    1 -> "Liké par 1 personne"
    else -> "Liké par $count personnes"
}

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
private fun UserProfilePreview() {
    MeloDropTheme {
        UserProfileContent(
            uiState = UserProfileUiState(
                isLoading = false,
                profile = ProfileOut(
                    id = 2,
                    username = "elliot",
                    displayName = "Elliot",
                    createdAt = "2026-08-23T10:00:00Z",
                    avatarColor = "peach",
                    avatarStyle = "simple",
                    stats = StatsOut(dropsCount = 6, likesReceived = 13, likesCount = 3),
                    currentDrop = previewDrop,
                    recentDrops = listOf(
                        previewDrop.copy(id = 2, title = "Enter Sandman"),
                        previewDrop.copy(id = 3, title = "Bohemian Rhapsody"),
                        previewDrop.copy(id = 4, title = "Dreams"),
                        previewDrop.copy(id = 5, title = "Smells Like Teen Spirit"),
                        previewDrop.copy(id = 6, title = "HUMBLE."),
                    ),
                ),
            ),
            onBack = {},
            onRetry = {},
            onPlayClick = {},
            onPodiumPlayClick = {},
            onLabeledSongPlayClick = {},
        )
    }
}