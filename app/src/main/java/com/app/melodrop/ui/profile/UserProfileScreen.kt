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
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.app.melodrop.ui.song.PosterSong
import com.app.melodrop.ui.song.SongPosterDialog

// Connects the screen to its ViewModel
@Composable
fun UserProfileScreen(
    // Id of the user, taken from the route "user/{userId}"
    userId: Int,
    onBack: () -> Unit,
    // Tap on "abonnés" or "abonnements": opens the lists, with the user's id and the tab
    onFollowsClick: (Int, String) -> Unit = { _, _ -> },
    // "Voir plus" or the "drops" counter: opens all the drops, with the user's id
    onDropsClick: (Int) -> Unit = {},
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
        onFollowClick = viewModel::toggleFollow,
        onFollowsClick = onFollowsClick,
        onDropsClick = onDropsClick,
        onSendReport = viewModel::sendReport,
        onCloseReport = viewModel::closeReport,
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
    // "Suivre" or "Abonné" button
    onFollowClick: () -> Unit = {},
    // Tap on "abonnés" or "abonnements": the user's id and the tab
    onFollowsClick: (Int, String) -> Unit = { _, _ -> },
    // "Voir plus" or the "drops" counter: the user's id
    onDropsClick: (Int) -> Unit = {},
    // "Envoyer" in the report window: the reason code and the message
    onSendReport: (String, String) -> Unit = { _, _ -> },
    // The report window is closed
    onCloseReport: () -> Unit = {},
) {
    // True while the report window is open. rememberSaveable: still open after a rotation
    var showReport by rememberSaveable { mutableStateOf(false) }

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
                isFollowBusy = uiState.isFollowBusy,
                followError = uiState.followError,
                onFollowClick = onFollowClick,
                onFollowsClick = { tab -> onFollowsClick(profile.id, tab) },
                onReportClick = { showReport = true },
                onDropsClick = { onDropsClick(profile.id) },
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

    val profile = uiState.profile
    if (showReport && profile != null) {
        ReportDialog(
            username = profile.username,
            isSending = uiState.isReportSending,
            error = uiState.reportError,
            isSent = uiState.isReportSent,
            wasAlreadyReported = uiState.wasAlreadyReported,
            onSend = onSendReport,
            onDismiss = {
                showReport = false
                onCloseReport()
            },
        )
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
    // True while the backend has not answered the "Suivre" button
    isFollowBusy: Boolean = false,
    // Shown under the button when the follow could not be saved
    followError: String? = null,
    onFollowClick: () -> Unit = {},
    // Tap on "abonnés" or "abonnements": FollowTab.FOLLOWERS or FollowTab.FOLLOWING
    onFollowsClick: (String) -> Unit = {},
    // "Signaler le profil" in the "⋮" menu
    onReportClick: () -> Unit = {},
    // "Voir plus" or the "drops" counter: opens all the drops of the user
    onDropsClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // The "⋮" menu with "Signaler le profil"; nothing on the user's own account
        ProfileHeader(profile = profile) {
            if (!profile.isMe) {
                UserProfileMenu(onReportClick = onReportClick)
            }
        }

        // The counters, and the "Suivre" button (hidden on the user's own account)
        FollowBar(
            profile = profile,
            onCountClick = onFollowsClick,
            onFollowClick = onFollowClick,
            isFollowBusy = isFollowBusy,
        )
        if (followError != null) {
            Text(
                text = followError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

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

                    // Hidden while the user has no badge
                    if (profile.badges.isNotEmpty()) {
                        BadgesSection(badges = profile.badges, emptyText = null)
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
                    onDropsClick = onDropsClick,
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
    // "Voir plus" or the "drops" counter: opens all the drops of the user
    onDropsClick: () -> Unit = {},
) {
    // The profile only sends the drop of the day and the last ones:
    // "Voir plus" shows up when the user has shared more
    val shownCount = profile.recentDrops.size + (if (profile.currentDrop != null) 1 else 0)
    val hasMoreDrops = profile.stats.dropsCount > shownCount

    // No scroll here: the page already scrolls inside ProfilePages
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // The drops counter opens all the drops, like on the user's own profile.
        // No click on the likes: the "Mes likes" list of the others is private
        StatsRow(stats = profile.stats, onDropsClick = onDropsClick)

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
            // The title on the left, "Voir plus" against the right edge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("DERNIERS DROPS")
                if (hasMoreDrops) {
                    TextButton(onClick = onDropsClick) {
                        Text(
                            text = "Voir plus ›",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

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
    // Tap on the cover: the poster of the song opens
    var showPoster by remember { mutableStateOf(false) }

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
                    .clip(RoundedCornerShape(14.dp))
                    // The play button plays, the rest of the cover opens the poster
                    .clickable(onClickLabel = "Voir la fiche du titre") { showPoster = true },
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

    if (showPoster) {
        SongPosterDialog(
            song = PosterSong(drop.songId, drop.title, drop.artist, drop.coverUrl, drop.genre),
            isPlaying = isPlaying,
            onPlayClick = onPlayClick,
            onDismiss = { showPoster = false },
        )
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