package com.app.melodrop.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.R
import com.app.melodrop.ui.components.CoverImage
import com.app.melodrop.network.dto.LikedSongOut
import com.app.melodrop.network.dto.PodiumSongOut
import com.app.melodrop.ui.mypicks.PickThumbnail
import com.app.melodrop.network.dto.ProfileDropOut
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.StatsOut
import com.app.melodrop.ui.components.Avatar
import com.app.melodrop.ui.components.avatarColor
import com.app.melodrop.ui.components.MeloDropTitle
import com.app.melodrop.ui.components.PlayPauseButton
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Peach
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.TextStyle
import com.app.melodrop.ui.components.PastelGradient
import com.app.melodrop.ui.components.CREATOR_USERNAME

// Number of covers on each line of "Mes drops" and "Mes picks"
private const val COVERS_PER_ROW = 3

// Picks shown in the "Mes picks" tab: the 6th place is the "Tout voir" tile
private const val PICKS_ON_PROFILE = 5

// Connects the screen to its ViewModel
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onLikesClick: () -> Unit,
    // Tap on the "drops" counter: opens "Mes drops"
    onDropsClick: () -> Unit,
    // Tap on "Mes skips": opens the list of the skipped songs
    onSkipsClick: () -> Unit,
    // Tap on the "Tout voir" tile of the picks: opens "Mes picks"
    onPicksClick: () -> Unit,
    onAccountClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    // Each time the tab opens: fresh counters and drops
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    // Leaving the Profile tab (other tab or sub-screen): the music stops
    DisposableEffect(Unit) {
        onDispose { viewModel.pausePreview() }
    }

    // App sent to the background (home button, screen off): the music stops too
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.pausePreview()
    }

    ProfileContent(
        uiState = viewModel.uiState,
        onRetry = viewModel::load,
        onLogout = onLogout,
        onEditProfile = onEditProfile,
        onLikesClick = onLikesClick,
        onDropsClick = onDropsClick,
        onSkipsClick = onSkipsClick,
        onPicksClick = onPicksClick,
        onAccountClick = onAccountClick,
        onPlayClick = viewModel::togglePreview,
        onPickPlayClick = viewModel::togglePickPreview,
        onPodiumPlayClick = viewModel::togglePodiumPreview,
    )
}

// Pure UI: shows the profile, a spinner or an error, from the state it receives
@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onLikesClick: () -> Unit,
    onDropsClick: () -> Unit,
    onSkipsClick: () -> Unit,
    onPicksClick: () -> Unit,
    onAccountClick: () -> Unit,
    // Tap on a cover: plays or pauses its preview
    onPlayClick: (ProfileDropOut) -> Unit,
    // Same, for the cover of a pick
    onPickPlayClick: (LikedSongOut) -> Unit,
    // Same, for a cover of the podium
    onPodiumPlayClick: (PodiumSongOut) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MeloDropTitle()

        // Local copy: Kotlin can then treat it as not null in the first branch
        val profile = uiState.profile

        when {
            profile != null -> ProfileBody(
                profile = profile,
                onLogout = onLogout,
                onEditProfile = onEditProfile,
                onLikesClick = onLikesClick,
                onDropsClick = onDropsClick,
                onSkipsClick = onSkipsClick,
                onPicksClick = onPicksClick,
                onAccountClick = onAccountClick,
                playingDropId = uiState.playingDropId,
                isPlaying = uiState.isPlaying,
                previewError = uiState.previewError,
                onPlayClick = onPlayClick,
                picks = uiState.picks,
                picksError = uiState.picksError,
                playingPickId = uiState.playingPickId,
                onPickPlayClick = onPickPlayClick,
                playingPodiumSongId = uiState.playingPodiumSongId,
                onPodiumPlayClick = onPodiumPlayClick,
            )

            // First answer not there yet
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            else -> ErrorView(message = uiState.error ?: "", onRetry = onRetry)
        }
    }
}

// The profile itself: the header stays at the top, the Mixtape and the activity slide under it
@Composable
private fun ProfileBody(
    profile: ProfileOut,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onLikesClick: () -> Unit,
    onDropsClick: () -> Unit,
    onSkipsClick: () -> Unit,
    onPicksClick: () -> Unit,
    onAccountClick: () -> Unit,
    playingDropId: Int?,
    isPlaying: Boolean,
    previewError: String?,
    onPlayClick: (ProfileDropOut) -> Unit,
    picks: List<LikedSongOut>,
    picksError: String?,
    playingPickId: Int?,
    onPickPlayClick: (LikedSongOut) -> Unit,
    playingPodiumSongId: Int?,
    onPodiumPlayClick: (PodiumSongOut) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // The menu is given to the header: only the user's own profile has one
        ProfileHeader(profile = profile) {
            ProfileMenu(
                onLogout = onLogout,
                onEditProfile = onEditProfile,
                onAccountClick = onAccountClick,
            )
        }

        ProfilePages(
            // The line under the labels takes the color of the avatar
            accent = avatarColor(profile.avatarColor),
            mixtape = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // A preview could not be played
                    if (previewError != null) {
                        Text(
                            text = previewError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    PodiumCard(
                        title = "MES TITRES PRÉFÉRÉS",
                        podium = profile.podium,
                        emptyText = "Ton podium est vide. Bientôt, tu pourras y mettre tes 3 titres préférés de tous les temps.",
                        playingSongId = playingPodiumSongId,
                        isPlaying = isPlaying,
                        onPlayClick = onPodiumPlayClick,
                    )
                }
            },
            activity = {
                ActivityPage(
                    profile = profile,
                    onLikesClick = onLikesClick,
                    onDropsClick = onDropsClick,
                    onSkipsClick = onSkipsClick,
                    onPicksClick = onPicksClick,
                    playingDropId = playingDropId,
                    isPlaying = isPlaying,
                    previewError = previewError,
                    onPlayClick = onPlayClick,
                    picks = picks,
                    picksError = picksError,
                    playingPickId = playingPickId,
                    onPickPlayClick = onPickPlayClick,
                )
            },
            // Takes all the height left under the header
            modifier = Modifier.weight(1f),
        )
    }
}

// The activity page: counters, then the "Mes drops" and "Mes picks" tabs (the profile before the Mixtape)
@Composable
private fun ActivityPage(
    profile: ProfileOut,
    onLikesClick: () -> Unit,
    onDropsClick: () -> Unit,
    onSkipsClick: () -> Unit,
    onPicksClick: () -> Unit,
    playingDropId: Int?,
    isPlaying: Boolean,
    previewError: String?,
    onPlayClick: (ProfileDropOut) -> Unit,
    picks: List<LikedSongOut>,
    picksError: String?,
    playingPickId: Int?,
    onPickPlayClick: (LikedSongOut) -> Unit,
) {
    // The drop of the day first, then the past ones
    val drops = listOfNotNull(profile.currentDrop) + profile.recentDrops

    // Tab shown under the counters: false = "Mes drops", true = "Mes picks".
    // rememberSaveable: the choice survives a rotation and a trip to another tab
    var showPicks by rememberSaveable { mutableStateOf(false) }

    // No scroll here: the page already scrolls inside ProfilePages
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // No gap inside: the link stays close to the counters
        Column {
            StatsRow(
                stats = profile.stats,
                onLikesClick = onLikesClick,
                onDropsClick = onDropsClick,
            )

            // Discreet link under "titres likés", on the user's own profile only
            TextButton(
                onClick = onSkipsClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = "Mes skips ›",
                    style = MaterialTheme.typography.bodySmall,
                    // Lavender: butter yellow is now the color of the picks
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileTabs(showPicks = showPicks, onSelect = { showPicks = it })

            // A preview could not be played
            if (previewError != null) {
                Text(
                    text = previewError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            when {
                // "Mes drops" tab: the drop of the day and the last ones
                !showPicks && drops.isEmpty() -> Text(
                    text = "Tu n'as encore déposé aucun titre. Ton premier drop apparaîtra ici.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                !showPicks -> DropsGrid(
                    drops = drops,
                    playingDropId = playingDropId,
                    isPlaying = isPlaying,
                    onPlayClick = onPlayClick,
                    onlineDropId = profile.currentDrop?.id,
                )

                // "Mes picks" tab: the picks could not be loaded
                picksError != null -> Text(
                    text = picksError,
                    color = MaterialTheme.colorScheme.error,
                )

                picks.isEmpty() -> Text(
                    text = "Aucun pick pour l'instant. Dans Discover, l'étoile garde un titre que tu découvres et qui te plaît.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> PicksGrid(
                    picks = picks,
                    playingPickId = playingPickId,
                    isPlaying = isPlaying,
                    onPlayClick = onPickPlayClick,
                    onSeeAllClick = onPicksClick,
                )
            }
        }
    }
}

// Two tabs side by side under the counters: "Mes drops" and "Mes picks"
@Composable
private fun ProfileTabs(showPicks: Boolean, onSelect: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ProfileTab(
                text = "MES DROPS",
                isSelected = !showPicks,
                selectedColor = MaterialTheme.colorScheme.onBackground,
                onClick = { onSelect(false) },
                modifier = Modifier.weight(1f),
            )
            ProfileTab(
                text = "MES PICKS",
                isSelected = showPicks,
                // Butter yellow, like the Pick button in Discover
                selectedColor = Butter,
                withStar = true,
                onClick = { onSelect(true) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// One tab: a pill, filled when it is the selected one
@Composable
private fun ProfileTab(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    withStar: Boolean = false,
) {
    val color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            // TalkBack says which tab is selected
            .semantics { selected = isSelected },
        shape = RoundedCornerShape(percent = 50),
        color = if (isSelected) MaterialTheme.colorScheme.outline else Color.Transparent,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (withStar) {
                Icon(
                    painter = painterResource(R.drawable.ic_star),
                    // Decoration: the text already says "MES PICKS"
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                letterSpacing = 1.5.sp,
            )
        }
    }
}

// Avatar, display name, @username and sign-up date, with an optional menu on the right.
// Shared with the profile of the other users (UserProfileScreen)
@Composable
internal fun ProfileHeader(
    profile: ProfileOut,
    // "Slot": a piece of UI given by the caller, nothing by default
    menu: @Composable () -> Unit = {},
) {
    // Little treat: the creator of meloDrop wears the logo gradient
    val isCreator = profile.username.equals(CREATOR_USERNAME, ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Avatar(color = profile.avatarColor, style = profile.avatarStyle)

        // Takes all the space left, so the menu stays on the right
        Column(modifier = Modifier.weight(1f)) {
            // The display name, or the username when none was chosen (ADR-0019)
            Text(
                text = profile.displayName ?: profile.username,
                fontFamily = Unbounded,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                // Unspecified: the letters take the gradient of the style instead
                color = if (isCreator) Color.Unspecified else MaterialTheme.colorScheme.onBackground,
                style = if (isCreator) LocalTextStyle.current.merge(TextStyle(brush = PastelGradient))
                else LocalTextStyle.current,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "@${profile.username} · membre depuis ${memberSince(profile.createdAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        menu()
    }
}

// "⋮" button with the profile and account actions
@Composable
private fun ProfileMenu(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onAccountClick: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }

    // Box: the menu opens right under the button
    Box {
        IconButton(
            onClick = { menuOpen = true },
            modifier = Modifier.semantics { contentDescription = "Menu du compte" },
        ) {
            Text(
                text = "⋮",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
        ) {
            DropdownMenuItem(
                text = { Text("Modifier le profil") },
                onClick = {
                    menuOpen = false
                    onEditProfile()
                },
            )
            DropdownMenuItem(
                text = { Text("Mon compte") },
                onClick = {
                    menuOpen = false
                    onAccountClick()
                },
            )
            DropdownMenuItem(
                text = { Text("Se déconnecter") },
                onClick = {
                    menuOpen = false
                    confirmLogout = true
                },
            )
        }
    }

    // Logout asks for a confirmation first, like on the Stage 3 mockup
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Te déconnecter ?") },
            text = { Text("Tu devras saisir ton mot de passe pour revenir.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                        onLogout()
                    },
                ) {
                    Text("SE DÉCONNECTER")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text("ANNULER")
                }
            },
        )
    }
}

// Three tiles side by side, with the same width. On the user's own profile,
// the first one opens "Mes drops" and the last one "Mes likes". Shared with UserProfileScreen
@Composable
internal fun StatsRow(
    stats: StatsOut,
    onLikesClick: (() -> Unit)? = null,
    onDropsClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val dropsLabel = if (stats.dropsCount <= 1) "drop" else "drops"
        if (onDropsClick != null) {
            CounterTile(
                value = stats.dropsCount,
                // The arrow shows that the tile can be tapped
                label = "$dropsLabel ›",
                color = Butter,
                modifier = Modifier
                    .weight(1f)
                    // Clip first, so the ripple of the tap keeps the round corners
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onDropsClick),
            )
        } else {
            CounterTile(
                value = stats.dropsCount,
                label = dropsLabel,
                color = Butter,
                modifier = Modifier.weight(1f),
            )
        }
        CounterTile(
            value = stats.likesReceived,
            label = if (stats.likesReceived <= 1) "like reçu" else "likes reçus",
            color = Peach,
            modifier = Modifier.weight(1f),
        )
        val likesLabel = if (stats.likesCount <= 1) "titre liké" else "titres likés"
        if (onLikesClick != null) {
            CounterTile(
                value = stats.likesCount,
                // The arrow shows that the tile can be tapped
                label = "$likesLabel ›",
                color = Mint,
                modifier = Modifier
                    .weight(1f)
                    // Clip first, so the ripple of the tap keeps the round corners
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onLikesClick),
            )
        } else {
            CounterTile(
                value = stats.likesCount,
                label = likesLabel,
                color = Mint,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// A big number with a short label under it
@Composable
private fun CounterTile(
    value: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// Small grey title in capitals, above a section. Shared with UserProfileScreen
@Composable
internal fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.5.sp,
    )
}

// The covers, 3 per line, each one with a play button. A Column of Rows, because a
// LazyVerticalGrid cannot be placed inside a Column that scrolls. Shared with UserProfileScreen
@Composable
internal fun DropsGrid(
    drops: List<ProfileDropOut>,
    // Drop whose preview is loaded in the player, null if none
    playingDropId: Int?,
    // True when the loaded preview plays
    isPlaying: Boolean,
    // Tap on a cover: plays or pauses its preview
    onPlayClick: (ProfileDropOut) -> Unit,
    onlineDropId: Int? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        drops.chunked(COVERS_PER_ROW).forEach { line ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                line.forEach { drop ->
                    DropThumbnail(
                        drop = drop,
                        isOnline = drop.id == onlineDropId,
                        // Pause icon only on the cover whose preview plays
                        isPlaying = isPlaying && drop.id == playingDropId,
                        onPlayClick = { onPlayClick(drop) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Empty places on the last line, so its covers keep the same size
                repeat(COVERS_PER_ROW - line.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// One drop: its cover with a play button, and its title
@Composable
private fun DropThumbnail(
    drop: ProfileDropOut,
    isOnline: Boolean,
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
                url = drop.coverUrl,
                contentDescription = "Pochette de ${drop.title}",
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

            // The drop of the day is marked on its cover
            if (isOnline) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                ) {
                    Text(
                        text = "EN LIGNE",
                        color = Mint,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }

        Text(
            text = drop.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// The last picks, 3 per line. With more than 5 picks, only 5 show,
// and the 6th place is a "Tout voir" tile that opens "Mes picks"
@Composable
private fun PicksGrid(
    picks: List<LikedSongOut>,
    // Pick whose preview is loaded in the player (its song id), null if none
    playingPickId: Int?,
    // True when the loaded preview plays
    isPlaying: Boolean,
    // Tap on a cover: plays or pauses its preview
    onPlayClick: (LikedSongOut) -> Unit,
    // Tap on the "Tout voir" tile
    onSeeAllClick: () -> Unit,
) {
    val hasMore = picks.size > PICKS_ON_PROFILE
    val lines = picks.take(PICKS_ON_PROFILE).chunked(COVERS_PER_ROW)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        lines.forEachIndexed { index, line ->
            // The tile goes right after the last pick, on the last line
            val withTile = hasMore && index == lines.lastIndex

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                line.forEach { pick ->
                    // Same thumbnail as on the "Mes picks" screen
                    PickThumbnail(
                        pick = pick,
                        // Pause icon only on the cover whose preview plays
                        isPlaying = isPlaying && pick.songId == playingPickId,
                        onPlayClick = { onPlayClick(pick) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (withTile) {
                    SeeAllTile(
                        hiddenCount = picks.size - PICKS_ON_PROFILE,
                        onClick = onSeeAllClick,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Empty places on the last line, so the covers keep the same size
                val used = line.size + if (withTile) 1 else 0
                repeat(COVERS_PER_ROW - used) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// 6th place of the picks: how many are not shown, and "Tout voir". Opens "Mes picks"
@Composable
private fun SeeAllTile(
    hiddenCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        // Square, the same size as a cover
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "+$hiddenCount",
                style = MaterialTheme.typography.headlineSmall,
                color = Butter,
            )
            Text(
                text = "Tout voir ›",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Shown when the profile could not be loaded
@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Ton profil n'a pas pu se charger",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("RÉESSAYER")
        }
    }
}

// "2026-09-07T12:05:59Z" becomes "septembre 2026"
private fun memberSince(createdAt: String): String =
    OffsetDateTime.parse(createdAt).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH))

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
private fun ProfileScreenPreview() {
    MeloDropTheme {
        ProfileContent(
            uiState = ProfileUiState(
                isLoading = false,
                profile = ProfileOut(
                    id = 2,
                    username = "Litoxam",
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
                    ),
                ),
            ),
            onRetry = {},
            onLogout = {},
            onEditProfile = {},
            onLikesClick = {},
            onDropsClick = {},
            onSkipsClick = {},
            onPicksClick = {},
            onAccountClick = {},
            onPlayClick = {},
            onPickPlayClick = {},
            onPodiumPlayClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun ProfileErrorPreview() {
    MeloDropTheme {
        ProfileContent(
            uiState = ProfileUiState(isLoading = false, error = "Impossible de joindre le serveur."),
            onRetry = {},
            onLogout = {},
            onEditProfile = {},
            onLikesClick = {},
            onDropsClick = {},
            onSkipsClick = {},
            onPicksClick = {},
            onAccountClick = {},
            onPlayClick = {},
            onPickPlayClick = {},
            onPodiumPlayClick = {},
        )
    }
}