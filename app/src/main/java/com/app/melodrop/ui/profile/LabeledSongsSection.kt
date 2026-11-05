package com.app.melodrop.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.melodrop.network.dto.LabeledSongOut
import com.app.melodrop.ui.components.CoverImage
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.MeloDropTheme
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.app.melodrop.ui.song.PosterSong
import com.app.melodrop.ui.song.SongPosterDialog

// The labeled songs of the Mixtape, under the podium: one card per song, its label above the title.
// Shared by the user's own profile and the profile of the others
@Composable
internal fun LabeledSongsSection(
    // "MES CHANSONS" on the user's own profile, "SES CHANSONS" on the others
    title: String,
    labeledSongs: List<LabeledSongOut>,
    // Label whose song is loaded in the player (its code), null if none
    playingLabel: String?,
    // True when the loaded preview plays
    isPlaying: Boolean,
    // Tap on a play button: plays or pauses its preview
    onPlayClick: (LabeledSongOut) -> Unit,
    // null: no edit buttons (the profile of the others)
    onEditClick: (() -> Unit)? = null,
) {
    // Only the songs whose label this version of the app knows
    val songs = labeledSongs.mapNotNull { song ->
        songLabel(song.label)?.let { label -> song to label }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f)) {
                SectionTitle(title)
            }
            // "Modifier" once there is something to change
            if (onEditClick != null && songs.isNotEmpty()) {
                TextButton(
                    onClick = onEditClick,
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

        if (songs.isEmpty() && onEditClick != null) {
            Text(
                text = "Choisis les sons qui te ressemblent : celui qui te met bien, celui qui te fait chialer, celui que t'assumes pas de ouf…",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        songs.forEach { (song, label) ->
            LabeledSongRow(
                song = song,
                label = label,
                // Pause icon only on the song whose preview plays
                isPlaying = isPlaying && song.label == playingLabel,
                onPlayClick = { onPlayClick(song) },
            )
        }

        // Some labels are still empty: a button to fill them
        if (onEditClick != null && songs.size < SONG_LABELS.size) {
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    text = "+ Ajouter une chanson",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// One song: its cover, its label in color, the title, the artist and a play button
@Composable
private fun LabeledSongRow(
    song: LabeledSongOut,
    label: SongLabel,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
) {
    // Tap on the cover: the poster of the song opens
    var showPoster by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Downloads the image from its URL, grey square while it loads
            CoverImage(
                url = song.coverUrl,
                contentDescription = "Pochette de ${song.title}",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    // The play button plays, the rest of the cover opens the poster
                    .clickable(onClickLabel = "Voir la fiche du titre") { showPoster = true },
            )

            // weight(1f): the texts take the room left, the button keeps its size
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.text.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = label.color,
                    // The longest labels go on two lines
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
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
            }

            PlayPauseButton(
                isPlaying = isPlaying,
                onClick = onPlayClick,
                buttonSize = 40.dp,
            )
        }
    }

    if (showPoster) {
        SongPosterDialog(
            song = PosterSong(song.songId, song.title, song.artist, song.coverUrl),
            isPlaying = isPlaying,
            onPlayClick = onPlayClick,
            onDismiss = { showPoster = false },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun LabeledSongsSectionPreview() {
    MeloDropTheme {
        Column(modifier = Modifier.padding(24.dp)) {
            LabeledSongsSection(
                title = "MES CHANSONS",
                labeledSongs = listOf(
                    LabeledSongOut("party", 1, 1L, "One More Time", "Daft Punk", ""),
                    LabeledSongOut("cry", 2, 2L, "Hurt", "Johnny Cash", ""),
                    LabeledSongOut("guilty_pleasure", 3, 3L, "Barbie Girl", "Aqua", ""),
                ),
                playingLabel = null,
                isPlaying = false,
                onPlayClick = {},
                onEditClick = {},
            )
        }
    }
}
