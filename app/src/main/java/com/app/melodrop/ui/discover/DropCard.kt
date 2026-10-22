package com.app.melodrop.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.app.melodrop.network.dto.FeedItem
import com.app.melodrop.ui.components.GenreChip
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.MeloDropTheme

// A drop in the feed: cover, song, genre, author and likes
@Composable
fun DropCard(
    drop: FeedItem,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    // null: no play button (for the card waiting behind)
    onPlayPause: (() -> Unit)? = null,
    // null: the @username cannot be tapped (for the card waiting behind)
    onAuthorClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // The cover, with the genre in its top left corner
            // and the play button in its bottom right corner.
            // Shrinks on short screens, so the lines below are never cut
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .align(Alignment.CenterHorizontally),
            ) {
                // Downloads the image from its URL, grey square while it loads
                AsyncImage(
                    model = drop.coverUrl,
                    contentDescription = "Pochette de ${drop.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        // Square, sized from the height left first, then from the width
                        .aspectRatio(1f, matchHeightConstraintsFirst = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.outline),
                )

                // Genre of the album, hidden when Deezer has none
                if (drop.genre != null) {
                    GenreChip(
                        genre = drop.genre,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                    )
                }

                if (onPlayPause != null) {
                    PlayPauseButton(
                        isPlaying = isPlaying,
                        onClick = onPlayPause,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                    )
                }
            }

            Text(
                text = drop.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = drop.artist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Déposé par @${drop.authorUsername}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // Tap: opens the profile of the author
                    modifier = if (onAuthorClick != null) {
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onAuthorClick)
                            .padding(vertical = 4.dp)
                    } else {
                        Modifier
                    },
                )
                Text(
                    text = likesLabel(drop.savesCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

private fun likesLabel(count: Int): String = when (count) {
    0 -> "Aucun like"
    1 -> "1 like"
    else -> "$count likes"
}

// Fixed height: the cover shrinks to the space left, so it needs a limited height
@Preview(heightDp = 640)
@Composable
private fun DropCardPreview() {
    MeloDropTheme {
        DropCard(
            drop = FeedItem(
                id = 1,
                songId = 1,
                title = "Numb",
                artist = "Linkin Park",
                // No network in the preview: the grey square shows instead
                coverUrl = "",
                genre = "Rock",
                authorId = 2,
                authorUsername = "elliot",
                savesCount = 3,
            ),
            isPlaying = false,
            onPlayPause = {},
        )
    }
}