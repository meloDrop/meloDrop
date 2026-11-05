package com.app.melodrop.ui.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.network.dto.SongDetailOut
import com.app.melodrop.network.dto.SongStatsOut
import com.app.melodrop.ui.components.CoverImage
import com.app.melodrop.ui.components.GenreChip
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Night
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// What the screen already knows about the song: the poster shows it at once,
// then adds the details when they arrive
data class PosterSong(
    // Id of the song in meloDrop
    val songId: Int,
    val title: String,
    val artist: String,
    val coverUrl: String?,
    val genre: String? = null,
)

// The poster of a song, over the screen: the big cover with a play button, the Deezer
// details and what the song did on meloDrop. Opened by a tap on a cover, closed by a
// tap outside or on "Fermer". Shared by every screen that shows covers
@Composable
fun SongPosterDialog(
    song: PosterSong,
    // True when the preview of this song plays on the screen behind
    isPlaying: Boolean,
    // Same action as the play button of the cover that opened the poster
    onPlayClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    // One per screen: the posters already opened show again at once
    val viewModel: SongPosterViewModel = viewModel()
    LaunchedEffect(song.songId) {
        viewModel.load(song.songId)
    }
    // The state of another song (the last poster opened) is not shown
    val state = viewModel.uiState.takeIf { it.songId == song.songId }

    Dialog(
        onDismissRequest = onDismiss,
        // Wider than an alert: the cover takes the whole width
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        SongPoster(
            song = song,
            details = state?.details,
            // Before the first answer, the poster shows that it loads
            isLoading = state == null || state.isLoading,
            error = state?.error,
            isPlaying = isPlaying,
            onPlayClick = onPlayClick,
            onDismiss = onDismiss,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

// The poster itself, without the window and the loading: also used by the preview
@Composable
private fun SongPoster(
    song: PosterSong,
    details: SongDetailOut?,
    isLoading: Boolean,
    error: String?,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val genre = details?.genre ?: song.genre

    Surface(
        modifier = modifier
            .fillMaxWidth()
            // Not too wide on a tablet
            .widthIn(max = 420.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                // A small screen in landscape: the poster scrolls
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) {
                // Downloads the image from its URL, grey square while it loads
                CoverImage(
                    url = song.coverUrl,
                    contentDescription = "Pochette de ${song.title}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                )
                // Genre of the album, hidden when Deezer has none
                if (genre != null) {
                    GenreChip(
                        genre = genre,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                    )
                }
                PlayPauseButton(
                    isPlaying = isPlaying,
                    onClick = onPlayClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    buttonSize = 56.dp,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        // fill = false: the "E" stays right after the name
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (details?.explicit == true) {
                        ExplicitBadge()
                    }
                }
            }

            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterHorizontally),
                    strokeWidth = 2.dp,
                )
                error != null -> Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                details != null -> {
                    DeezerDetails(details = details)
                    MeloDropStats(stats = details.stats)
                }
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("FERMER")
            }
        }
    }
}

// Album, release date and length, from Deezer. Only the ones Deezer gave
@Composable
private fun DeezerDetails(details: SongDetailOut) {
    val rows = listOfNotNull(
        details.album?.let { "Album" to it },
        details.releaseDate?.let { releaseDateText(it) }?.let { "Sortie" to it },
        details.duration?.let { "Durée" to durationText(it) },
    )

    // Deezer did not answer, or no longer has the track
    if (rows.isEmpty()) {
        Text(
            text = "Deezer ne donne pas plus d'infos sur ce titre pour l'instant.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // Same width for the 3 labels: the values are aligned
                    modifier = Modifier.width(64.dp),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// What the song did on meloDrop, all the users together: 3 tiles side by side
@Composable
private fun MeloDropStats(stats: SongStatsOut) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "SUR MELODROP",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.5.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // The colors of the app: lavender for the drops, mint for the likes, yellow for the picks
            StatTile(
                value = stats.dropsCount,
                label = if (stats.dropsCount <= 1) "drop" else "drops",
                color = Lavender,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = stats.likesCount,
                label = if (stats.likesCount <= 1) "like" else "likes",
                color = Mint,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = stats.picksCount,
                label = if (stats.picksCount <= 1) "pick" else "picks",
                color = Butter,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// One counter: the number in color, the word under it
@Composable
private fun StatTile(
    value: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Small grey "E", like on Deezer, for explicit lyrics
@Composable
private fun ExplicitBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = "E",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Night,
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 1.dp)
                // Read aloud by TalkBack instead of "E"
                .semantics { contentDescription = "Paroles explicites" },
        )
    }
}

// "16 août 2011", from a Deezer date like "2011-08-16". null if the date can't be read
private fun releaseDateText(date: String): String? =
    runCatching {
        LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH))
    }.getOrNull()

// "4:03", from a length in seconds
private fun durationText(seconds: Int): String =
    "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"

private val previewSong = PosterSong(
    songId = 1,
    title = "Midnight City",
    artist = "M83",
    coverUrl = "",
    genre = "Électro",
)

private val previewDetails = SongDetailOut(
    songId = 1,
    title = "Midnight City",
    artist = "M83",
    coverUrl = "",
    genre = "Électro",
    album = "Hurry Up, We're Dreaming",
    releaseDate = "2011-08-16",
    duration = 243,
    explicit = false,
    stats = SongStatsOut(dropsCount = 3, likesCount = 12, picksCount = 4),
)

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun SongPosterPreview() {
    MeloDropTheme {
        SongPoster(
            song = previewSong,
            details = previewDetails,
            isLoading = false,
            error = null,
            isPlaying = false,
            onPlayClick = {},
            onDismiss = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun SongPosterLoadingPreview() {
    MeloDropTheme {
        SongPoster(
            song = previewSong,
            details = null,
            isLoading = true,
            error = null,
            isPlaying = true,
            onPlayClick = {},
            onDismiss = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
