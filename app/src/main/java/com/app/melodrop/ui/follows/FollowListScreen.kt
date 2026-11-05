package com.app.melodrop.ui.follows

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.network.dto.FollowUserOut
import com.app.melodrop.ui.components.Avatar
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun FollowListScreen(
    // The user whose lists are shown: the logged-in user or another one
    userId: Int,
    // Tab open first: the counter tapped on the profile
    initialTab: String,
    onBack: () -> Unit,
    // Tap on a user: opens their profile
    onUserClick: (Int) -> Unit,
    viewModel: FollowListViewModel = viewModel(),
) {
    // Loads the lists each time the screen shows (back from a profile too)
    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    // Tab shown, kept after a rotation
    var tab by rememberSaveable { mutableStateOf(initialTab) }

    FollowListContent(
        uiState = viewModel.uiState,
        tab = tab,
        onTabSelect = { tab = it },
        onBack = onBack,
        onRetry = { viewModel.load(userId) },
        onUserClick = onUserClick,
    )
}

// Pure UI: back arrow, the two tabs, then the list, a spinner or an error
@Composable
private fun FollowListContent(
    uiState: FollowListUiState,
    tab: String,
    onTabSelect: (String) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onUserClick: (Int) -> Unit,
) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TabPill(
                text = "Abonnés (${uiState.followers.size})",
                isSelected = tab == FollowTab.FOLLOWERS,
                onClick = { onTabSelect(FollowTab.FOLLOWERS) },
                modifier = Modifier.weight(1f),
            )
            TabPill(
                text = "Abonnements (${uiState.following.size})",
                isSelected = tab == FollowTab.FOLLOWING,
                onClick = { onTabSelect(FollowTab.FOLLOWING) },
                modifier = Modifier.weight(1f),
            )
        }

        // Local copy: Kotlin can then treat it as not null in its branch
        val error = uiState.error
        val users = if (tab == FollowTab.FOLLOWERS) uiState.followers else uiState.following

        when {
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

            users.isEmpty() -> Text(
                text = if (tab == FollowTab.FOLLOWERS) "Personne ne suit ce profil pour l'instant."
                else "Ce profil ne suit personne pour l'instant.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Only the visible rows are built
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // The key lets Compose follow each row when the list changes
                items(users, key = { it.id }) { user ->
                    FollowUserRow(user = user, onClick = { onUserClick(user.id) })
                }
            }
        }
    }
}

// One tab: lavender border and text when selected, grey otherwise
@Composable
private fun TabPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = modifier
            // 44 dp: enough room for a finger
            .height(44.dp)
            .clip(shape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent)
            .border(
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                ),
                shape = shape,
            )
            // TalkBack reads it as a tab, selected or not
            .selectable(selected = isSelected, onClick = onClick, role = Role.Tab),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// One user: avatar, display name and @username, like in the user search
@Composable
private fun FollowUserRow(user: FollowUserOut, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Rounded ripple when the row is tapped
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(color = user.avatarColor, style = user.avatarStyle, avatarSize = 48.dp)

        // Takes the width left, so long names are cut with "…"
        Column(modifier = Modifier.weight(1f)) {
            // The display name, or the username when none was chosen (ADR-0019)
            Text(
                text = user.displayName ?: user.username,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// Fake users for the previews
private val previewUsers = listOf(
    FollowUserOut(id = 2, username = "elliot", displayName = "Elliot", avatarColor = "peach", avatarStyle = "rock"),
    FollowUserOut(id = 30, username = "elsa_b", displayName = null, avatarColor = "mint", avatarStyle = "pop"),
)

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun FollowListPreview() {
    MeloDropTheme {
        FollowListContent(
            uiState = FollowListUiState(isLoading = false, followers = previewUsers, following = previewUsers.take(1)),
            tab = FollowTab.FOLLOWERS,
            onTabSelect = {},
            onBack = {},
            onRetry = {},
            onUserClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun FollowListEmptyPreview() {
    MeloDropTheme {
        FollowListContent(
            uiState = FollowListUiState(isLoading = false),
            tab = FollowTab.FOLLOWING,
            onTabSelect = {},
            onBack = {},
            onRetry = {},
            onUserClick = {},
        )
    }
}
