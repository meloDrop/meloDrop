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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.app.melodrop.network.dto.ProfileDropOut
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.StatsOut
import com.app.melodrop.ui.components.Avatar
import com.app.melodrop.ui.components.MeloDropTitle
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Peach
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Number of covers on each line of "Mes derniers drops"
private const val COVERS_PER_ROW = 3

// Connects the screen to its ViewModel
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onLikesClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    // Each time the tab opens: fresh counters and drops
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    ProfileContent(
        uiState = viewModel.uiState,
        onRetry = viewModel::load,
        onLogout = onLogout,
        onEditProfile = onEditProfile,
        onLikesClick = onLikesClick,
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

// The profile itself: header, counters and last drops
@Composable
private fun ProfileBody(
    profile: ProfileOut,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onLikesClick: () -> Unit,
) {
    // The drop of the day first, then the past ones
    val drops = listOfNotNull(profile.currentDrop) + profile.recentDrops

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Scrolls on small screens
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // The menu is given to the header: only the user's own profile has one
        ProfileHeader(profile = profile) {
            ProfileMenu(onLogout = onLogout, onEditProfile = onEditProfile)
        }

        StatsRow(stats = profile.stats, onLikesClick = onLikesClick)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("MES DERNIERS DROPS")

            if (drops.isEmpty()) {
                Text(
                    text = "Tu n'as encore déposé aucun titre. Ton premier drop apparaîtra ici.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                DropsGrid(drops = drops, onlineDropId = profile.currentDrop?.id)
            }
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
                color = MaterialTheme.colorScheme.onBackground,
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

// "⋮" button with the profile and account actions ("Mon compte" will be added here)
@Composable
private fun ProfileMenu(onLogout: () -> Unit, onEditProfile: () -> Unit) {
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
// the last one opens "Mes likes". Shared with UserProfileScreen
@Composable
internal fun StatsRow(stats: StatsOut, onLikesClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CounterTile(
            value = stats.dropsCount,
            label = if (stats.dropsCount <= 1) "drop" else "drops",
            color = Butter,
            modifier = Modifier.weight(1f),
        )
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

// The covers, 3 per line. A Column of Rows, because a LazyVerticalGrid
// cannot be placed inside a Column that scrolls. Shared with UserProfileScreen
@Composable
internal fun DropsGrid(drops: List<ProfileDropOut>, onlineDropId: Int? = null) {
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

// One past drop: its cover and its title
@Composable
private fun DropThumbnail(
    drop: ProfileDropOut,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box {
            // Downloads the image from its URL, grey square while it loads
            AsyncImage(
                model = drop.coverUrl,
                contentDescription = "Pochette de ${drop.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.outline),
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
                    ),
                ),
            ),
            onRetry = {},
            onLogout = {},
            onEditProfile = {},
            onLikesClick = {},
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
        )
    }
}
