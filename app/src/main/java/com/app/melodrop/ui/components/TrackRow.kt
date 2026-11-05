package com.app.melodrop.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.melodrop.network.dto.TrackResult
import com.app.melodrop.ui.theme.MeloDropTheme

// One track of the Deezer search: cover, title, artist, and a play button if there is a preview.
// Shared by the Drop screen and the podium screen. onClick null: the row cannot be tapped
@Composable
fun TrackRow(
    track: TrackResult,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                // Inside the Surface: the touch effect keeps the rounded corners
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CoverImage(
                url = track.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )

            // weight(1f): the texts take the room left, the button keeps its size
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (track.previewUrl != null) {
                PlayPauseButton(
                    isPlaying = isPlaying,
                    onClick = onPlayClick,
                    buttonSize = 40.dp,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TrackRowPreview() {
    MeloDropTheme {
        TrackRow(
            // No network in the preview: the grey square shows instead of the cover
            track = TrackResult(3135556, "Get Lucky", "Daft Punk", "", "https://example.com"),
            isPlaying = false,
            onPlayClick = {},
        )
    }
}