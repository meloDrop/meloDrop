package com.app.melodrop.ui.discover

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.melodrop.ui.components.CREATOR_USERNAME
import com.app.melodrop.ui.components.CoverImage
import com.app.melodrop.network.dto.FeedItem
import com.app.melodrop.network.dto.UserBriefOut
import com.app.melodrop.ui.components.Avatar
import com.app.melodrop.ui.components.GenreChip
import com.app.melodrop.ui.components.PastelGradient
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.tutorial.TutorialTarget
import com.app.melodrop.ui.tutorial.tutorialTarget
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.BoxWithConstraints

// Below this side, the cover is small (short screens): smaller play button, no chips over it
private val SMALL_COVER = 180.dp

// Blur of the cover that fills the sides of the card on short screens
private val BACKDROP_BLUR = 24.dp

// Avatar of the dropper, next to "Déposé par"
private val CARD_AVATAR_SIZE = 30.dp

// Several droppers: up to 3 small avatars, each one shifted by 16 dp
private const val STACK_AVATARS = 3
private val STACK_AVATAR_SIZE = 24.dp
private val STACK_AVATAR_STEP = 16.dp

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
                    .fillMaxWidth(),
            ) {
                // The cover is a square as big as the room left: its side is the smaller one
                val side = minOf(maxWidth, maxHeight)
                val isSmall = side < SMALL_COVER
                // Short screen: the square is narrower than the card, its sides would stay empty
                val hasSides = maxWidth - side > 1.dp

                // The whole width of the card, as high as the cover
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(side)
                        .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    // The sides: the same cover, enlarged, blurred and darkened
                    if (hasSides && drop.coverUrl != null) {
                        CoverBackdrop(url = drop.coverUrl)
                    }

                    // The cover itself, whole, with its chips and its play button
                    Box(modifier = Modifier.size(side)) {
                        // Downloads the image from its URL, grey square while it loads
                        CoverImage(
                            url = drop.coverUrl,
                            contentDescription = "Pochette de ${drop.title}",
                            modifier = Modifier
                                .fillMaxSize()
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

                        // Song already in "Mes likes", dropped again: marked in the bottom left corner
                        if (drop.alreadyLiked) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(if (isSmall) 8.dp else 12.dp),
                                shape = CircleShape,
                                // Same dark background as the genre: readable on any cover
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                            ) {
                                Text(
                                    // Shorter on a small cover, so it fits next to the play button
                                    text = if (isSmall) "Déjà liké" else "Déjà dans tes likes",
                                    color = Mint,
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
                modifier = Modifier
                    .fillMaxWidth()
                    // Only on the card on top: the one waiting behind cannot be tapped
                    .then(if (onPeopleClick != null) Modifier.tutorialTarget(TutorialTarget.CARD_PEOPLE) else Modifier),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Little treat: the creator of meloDrop wears the logo gradient
                val isCreator = drop.authorUsername.equals(CREATOR_USERNAME, ignoreCase = true)

                if (droppersCount > 1) {
                    // Several droppers: their avatars, then one line for all. A tap opens the list
                    Row(
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DroppersAvatars(droppers = drop.droppers.orEmpty())
                        Text(
                            text = "$droppersCount personnes ont droppé cette chanson ›",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            // Two lines at most on narrow screens, the likes keep their place
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                } else {
                    // The dropper and the cosmetics of the avatar.
                    // null for the feeds before ADR-0021: only the @username shows
                    val author = drop.droppers.orEmpty().firstOrNull()

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
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (author != null) {
                            Avatar(
                                color = author.avatarColor,
                                style = author.avatarStyle,
                                avatarSize = CARD_AVATAR_SIZE,
                                drop = author.avatarDrop,
                                deco = author.avatarDeco,
                                face = author.avatarFace,
                                aura = author.avatarAura,
                                showAura = true,
                                // Moves on the card on top only: the one waiting behind stays still
                                animateAura = onPeopleClick != null,
                                // Same height on every card, with or without an aura
                                reserveAuraRoom = true,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                        }
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

// Up to 3 small avatars of the droppers, one over the other, without their aura:
// several auras side by side would mix. The first dropper (a followed user first) is on top
@Composable
private fun DroppersAvatars(droppers: List<UserBriefOut>) {
    val shown = droppers.take(STACK_AVATARS)

    // Only a decoration: TalkBack reads the line next to it
    Box(modifier = Modifier.clearAndSetSemantics { }) {
        // Drawn from the last one, so the first dropper ends on top of the others
        shown.indices.reversed().forEach { index ->
            val user = shown[index]
            Avatar(
                color = user.avatarColor,
                style = user.avatarStyle,
                avatarSize = STACK_AVATAR_SIZE,
                drop = user.avatarDrop,
                deco = user.avatarDeco,
                face = user.avatarFace,
                modifier = Modifier
                    // Each avatar covers a third of the next one
                    .padding(start = STACK_AVATAR_STEP * index)
                    // Ring in the color of the card, so the avatars stay apart
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
            )
        }
    }
}

// The same cover, enlarged to fill the card, blurred and darkened: it fills the sides
// of the card when the square cover is narrower than it (short screens).
// The blur needs Android 12: before, the cover is only darkened, a bit more
@Composable
private fun BoxScope.CoverBackdrop(url: String) {
    CoverImage(
        url = url,
        // Only a decoration: TalkBack reads the real cover
        contentDescription = null,
        modifier = Modifier
            .matchParentSize()
            .blur(BACKDROP_BLUR),
    )
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(Color.Black.copy(alpha = backdropDim())),
    )
}

// How dark the backdrop is: more without the blur, so the real cover still stands out
private fun backdropDim(): Float =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 0.35f else 0.6f

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
                alreadyLiked = true,
            ),
            isPlaying = false,
            onPlayPause = {},
        )
    }
}

// Short screen: the cover is narrower than the card, the blurred cover fills its sides
@Preview(heightDp = 400)
@Composable
private fun DropCardShortPreview() {
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
