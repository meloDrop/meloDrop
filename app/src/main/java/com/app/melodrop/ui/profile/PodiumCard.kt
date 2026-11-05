package com.app.melodrop.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.melodrop.network.dto.PodiumSongOut
import com.app.melodrop.ui.components.CoverImage
import com.app.melodrop.ui.components.PastelGradient
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Night
import com.app.melodrop.ui.theme.Peach
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.app.melodrop.ui.song.PosterSong
import com.app.melodrop.ui.song.SongPosterDialog

// Order of the columns: the 2nd place on the left, the 1st in the middle, the 3rd on the right
private val PODIUM_ORDER = listOf(2, 1, 3)

// The podium of the Mixtape: the 3 favorite songs of all time, the 1st place higher in the middle.
// Shared by the user's own profile and the profile of the others
@Composable
internal fun PodiumCard(
    // "MES TITRES PRÉFÉRÉS" on the user's own profile, "SES TITRES PRÉFÉRÉS" on the others
    title: String,
    // From the 1st place to the last, empty if the podium is not made yet
    podium: List<PodiumSongOut>,
    // Text shown in the card when the podium is empty
    emptyText: String,
    // Song whose preview is loaded in the player (its song id), null if none
    playingSongId: Int?,
    // True when the loaded preview plays
    isPlaying: Boolean,
    // Tap on a cover: plays or pauses its preview
    onPlayClick: (PodiumSongOut) -> Unit,
    // null: no "Modifier" button (the profile of the others)
    onEditClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // weight(1f): the title takes the room left, the "Modifier" button keeps its size
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // weight on the title: the badge is measured first and keeps its width,
                    // the title goes on two lines on a narrow screen
                    SectionTitle(title, modifier = Modifier.weight(1f, fill = false))
                    AllTimeBadge()
                }
                if (onEditClick != null) {
                    TextButton(
                        onClick = onEditClick,
                        // Less padding than usual: the header is narrow
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Text(
                            text = "Modifier",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            if (podium.isEmpty()) {
                Text(
                    text = emptyText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    // The three steps stand on the same floor: the 1st place is the highest
                    verticalAlignment = Alignment.Bottom,
                ) {
                    PODIUM_ORDER.forEach { place ->
                        // null when nobody is at this place yet (a podium of 1 or 2 songs)
                        val song = podium.find { it.place == place }
                        PodiumPlace(
                            place = place,
                            song = song,
                            // Pause icon only on the cover whose preview plays
                            isPlaying = isPlaying && song != null && song.songId == playingSongId,
                            onPlayClick = { if (song != null) onPlayClick(song) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

// Small "ALL TIME" pill, in the colors of the logo
@Composable
private fun AllTimeBadge() {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.outline,
    ) {
        Text(
            text = "ALL TIME",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            // Always on one line, even when the room is short
            maxLines = 1,
            softWrap = false,
            fontFamily = Unbounded,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            // Unspecified: the letters take the gradient of the style instead
            color = Color.Unspecified,
            style = LocalTextStyle.current.merge(TextStyle(brush = PastelGradient)),
        )
    }
}

// One place: the cover with a play button, the title and the artist, then the step with its number
@Composable
private fun PodiumPlace(
    place: Int,
    // null: nobody at this place yet
    song: PodiumSongOut?,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The 1st place is bigger and higher, then the 2nd, then the 3rd
    val coverSize = if (place == 1) 96.dp else 82.dp
    val stepHeight = when (place) {
        1 -> 72.dp
        2 -> 48.dp
        else -> 34.dp
    }
    // Like gold, silver and bronze, in the pastels of the app
    val stepColor = when (place) {
        1 -> Butter
        2 -> Lavender
        else -> Peach
    }

    // Tap on the cover: the poster of the song opens
    var showPoster by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (song != null) {
            Box(modifier = Modifier.size(coverSize)) {
                // Downloads the image from its URL, grey square while it loads
                CoverImage(
                    url = song.coverUrl,
                    contentDescription = "Pochette de ${song.title}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        // The play button plays, the rest of the cover opens the poster
                        .clickable(onClickLabel = "Voir la fiche du titre") { showPoster = true },
                )
                PlayPauseButton(
                    isPlaying = isPlaying,
                    onClick = onPlayClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp),
                    buttonSize = 30.dp,
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (showPoster) {
                SongPosterDialog(
                    song = PosterSong(song.songId, song.title, song.artist, song.coverUrl),
                    isPlaying = isPlaying,
                    onPlayClick = onPlayClick,
                    onDismiss = { showPoster = false },
                )
            }
        } else {
            // Empty place: a grey square where the cover goes
            Box(
                modifier = Modifier
                    .size(coverSize)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.outline),
            )
        }

        // The step, with its number. Paler when the place is empty
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(stepHeight)
                .background(
                    color = if (song != null) stepColor else stepColor.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = place.toString(),
                fontFamily = Unbounded,
                fontWeight = FontWeight.Bold,
                fontSize = if (place == 1) 26.sp else 20.sp,
                color = Night,
            )
        }
    }
}

private val previewPodium = listOf(
    PodiumSongOut(place = 1, songId = 1, deezerTrackId = 1L, title = "Bohemian Rhapsody", artist = "Queen", coverUrl = ""),
    PodiumSongOut(place = 2, songId = 2, deezerTrackId = 2L, title = "Teardrop", artist = "Massive Attack", coverUrl = ""),
    PodiumSongOut(place = 3, songId = 3, deezerTrackId = 3L, title = "Midnight City", artist = "M83", coverUrl = ""),
)

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun PodiumCardPreview() {
    MeloDropTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PodiumCard(
                title = "MES TITRES PRÉFÉRÉS",
                podium = previewPodium,
                emptyText = "",
                playingSongId = null,
                isPlaying = false,
                onPlayClick = {},
                onEditClick = {},
            )
            // A podium of 2 songs: the 3rd place stays empty
            PodiumCard(
                title = "SES TITRES PRÉFÉRÉS",
                podium = previewPodium.take(2),
                emptyText = "",
                playingSongId = null,
                isPlaying = false,
                onPlayClick = {},
            )
            PodiumCard(
                title = "SES TITRES PRÉFÉRÉS",
                podium = emptyList(),
                emptyText = "@elliot n'a pas encore fait sa mixtape.",
                playingSongId = null,
                isPlaying = false,
                onPlayClick = {},
            )
        }
    }
}
