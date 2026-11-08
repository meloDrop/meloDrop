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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.melodrop.ui.components.CREATOR_USERNAME
import com.app.melodrop.ui.components.CoverImage
import com.app.melodrop.network.dto.FeedItem
import com.app.melodrop.ui.components.GenreChip
import com.app.melodrop.ui.components.PastelGradient
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.MeloDropTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.BoxWithConstraints

// Below this side, the cover is small (short screens): smaller play button, no chips over it
private val SMALL_COVER = 180.dp

// A song in the feed: cover, song, genre, who dropped it and its likes.
// Several users can have dropped the same song: it shows once (ADR-0021)
@Composable
fun DropCard(
    drop: FeedItem,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    // null: no play button (for the card waiting behind)
    onPlayPause: (() -> Unit)? = null,
    // null: the @username cannot be tapped (for the card waiting behind)
    onAuthorClick: (() -> Unit)? = null,
    // Opens who dropped the song (false) or who liked it (true).
    // null: nothing can be tapped (for the card waiting behind)
    onPeopleClick: ((Boolean) -> Unit)? = null,
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
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .align(Alignment.CenterHorizontally),
            ) {
                // The cover is a square as big as the room left: its side is the smaller one
                val isSmall = minOf(maxWidth, maxHeight) < SMALL_COVER

                // Downloads the image from its URL, grey square while it loads
                CoverImage(
                    url = drop.coverUrl,
                    contentDescription = "Pochette de ${drop.title}",
                    modifier = Modifier
                        // Square, sized from the height left first, then from the width
                        .aspectRatio(1f, matchHeightConstraintsFirst = true)
                        .clip(RoundedCornerShape(20.dp)),
                )

                // Genre of the album, hidden when Deezer has none or when the cover is small
                if (drop.genre != null && !isSmall) {
                    GenreChip(
                        genre = drop.genre,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                    )
                }

                // The user follows the author: marked in the other corner, if there is room
                if (drop.isFollowed && !isSmall) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        shape = CircleShape,
                        // Same dark background as the genre: readable on any cover
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                    ) {
                        Text(
                            text = "Abonné",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }

                if (onPlayPause != null) {
                    PlayPauseButton(
                        isPlaying = isPlaying,
                        onClick = onPlayPause,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(if (isSmall) 8.dp else 12.dp),
                        // Smaller on a small cover, so it does not hide it
                        buttonSize = if (isSmall) 40.dp else 52.dp,
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

            // Everyone who dropped the song; the author alone for the feeds before ADR-0021
            val droppersCount = drop.droppers.orEmpty().size

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Little treat: the creator of meloDrop wears the logo gradient
                val isCreator = drop.authorUsername.equals(CREATOR_USERNAME, ignoreCase = true)

                if (droppersCount > 1) {
                    // Several droppers: one line for all, a tap opens the list
                    Text(
                        text = "$droppersCount personnes ont droppé cette chanson ›",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        // Two lines at most on narrow screens, the likes keep their place
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 8.dp)
                            .then(
                                if (onPeopleClick != null) {
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(onClickLabel = "Voir qui a droppé") { onPeopleClick(false) }
                                        .padding(vertical = 4.dp)
                                } else {
                                    Modifier
                                },
                            ),
                    )
                } else {
                    Row(
                        // One dropper, two Text side by side: the gradient only covers the @username.
                        // Tap: opens the profile of the author
                        modifier = if (onAuthorClick != null) {
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onAuthorClick)
                                .padding(vertical = 4.dp)
                        } else {
                            Modifier
                        },
                    ) {
                        Text(
                            text = "Déposé par ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "@${drop.authorUsername}",
                            style = if (isCreator) MaterialTheme.typography.bodyMedium.merge(TextStyle(brush = PastelGradient))
                            else MaterialTheme.typography.bodyMedium,
                            // Unspecified: the letters take the gradient of the style instead
                            color = if (isCreator) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                // With likes, a tap opens who liked the song
                val canOpenLikes = onPeopleClick != null && drop.savesCount > 0
                Text(
                    text = if (canOpenLikes) "${likesLabel(drop.savesCount)} ›" else likesLabel(drop.savesCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = if (canOpenLikes && onPeopleClick != null) {
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClickLabel = "Voir qui a liké") { onPeopleClick(true) }
                            .padding(4.dp)
                    } else {
                        Modifier
                    },
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
                isFollowed = true,
            ),
            isPlaying = false,
            onPlayPause = {},
        )
    }
}