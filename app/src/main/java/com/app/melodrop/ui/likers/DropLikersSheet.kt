package com.app.melodrop.ui.likers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.R
import com.app.melodrop.network.dto.DropLikerOut
import com.app.melodrop.ui.components.TabPill
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.components.UserRow
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.MeloDropTheme
import kotlinx.coroutines.launch

// Bottom sheet with who liked and who picked one of the user's drops, in two tabs.
// Opened from the counters of the Drop screen and from the stats under the user's drops.
// OptIn: ModalBottomSheet is marked as experimental in Material 3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropLikersSheet(
    dropId: Int,
    // Tab open first: false = Likes, true = Picks
    startOnPicks: Boolean,
    // Tap on a user: the sheet closes, then their profile opens
    onUserClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    viewModel: DropLikersViewModel = viewModel(),
) {
    // Loads the list each time the sheet opens
    LaunchedEffect(dropId) {
        viewModel.load(dropId)
    }

    // Tab shown, kept after a rotation
    var showPicks by rememberSaveable { mutableStateOf(startOnPicks) }

    // skipPartiallyExpanded: the sheet opens fully at once, not half-way
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        // Swipe down, tap outside the sheet, or back button
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        DropLikersContent(
            uiState = viewModel.uiState,
            showPicks = showPicks,
            onTabSelect = { showPicks = it },
            onRetry = { viewModel.load(dropId) },
            onUserClick = { userId ->
                // The sheet slides down first, then the profile opens
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    onDismiss()
                    onUserClick(userId)
                }
            },
        )
    }
}

// Pure UI: title, the two tabs, then the list, a spinner or an error
@Composable
private fun DropLikersContent(
    uiState: DropLikersUiState,
    showPicks: Boolean,
    onTabSelect: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onUserClick: (Int) -> Unit,
) {
    // Same split as the tiles of the Drop screen: the simple likes, then the picks
    val likes = uiState.likers.filter { !it.isPick }
    val picks = uiState.likers.filter { it.isPick }
    val users = if (showPicks) picks else likes

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Qui a aimé ton drop",
            fontFamily = Unbounded,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // The numbers only once the list is there
            TabPill(
                text = if (uiState.isLoading) "Likes" else "Likes (${likes.size})",
                isSelected = !showPicks,
                onClick = { onTabSelect(false) },
                modifier = Modifier.weight(1f),
            )
            TabPill(
                text = if (uiState.isLoading) "Picks" else "Picks (${picks.size})",
                isSelected = showPicks,
                onClick = { onTabSelect(true) },
                modifier = Modifier.weight(1f),
            )
        }

        // Local copy: Kotlin can then treat it as not null in its branch
        val error = uiState.error

        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            error != null -> Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onRetry) {
                    Text("RÉESSAYER")
                }
            }

            users.isEmpty() -> Text(
                text = if (showPicks) "Aucun pick sur ce drop pour l'instant."
                else "Aucun like sur ce drop pour l'instant.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // A few dozen users at most: a simple Column that scrolls is enough
            else -> Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                users.forEach { user ->
                    UserRow(
                        username = user.username,
                        displayName = user.displayName,
                        avatarColor = user.avatarColor,
                        avatarStyle = user.avatarStyle,
                        onClick = { onUserClick(user.id) },
                        // A small butter star on the picks, like the Pick button of Discover
                        trailing = {
                            if (user.isPick) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_star),
                                    contentDescription = "Pick",
                                    tint = Butter,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

// Fake users for the previews
private val previewLikers = listOf(
    DropLikerOut(id = 2, username = "elliot", displayName = "Elliot", avatarColor = "peach", avatarStyle = "rock", isPick = true),
    DropLikerOut(id = 3, username = "sam", displayName = null, avatarColor = "mint", avatarStyle = "pop"),
    DropLikerOut(id = 4, username = "lea_m", displayName = "Léa", avatarColor = "rose", avatarStyle = "simple"),
)

// The content only: a ModalBottomSheet does not show in a Preview
@Preview(showBackground = true, backgroundColor = 0xFF18181F)
@Composable
private fun DropLikersLikesPreview() {
    MeloDropTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            DropLikersContent(
                uiState = DropLikersUiState(likers = previewLikers),
                showPicks = false,
                onTabSelect = {},
                onRetry = {},
                onUserClick = {},
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF18181F)
@Composable
private fun DropLikersPicksPreview() {
    MeloDropTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            DropLikersContent(
                uiState = DropLikersUiState(likers = previewLikers),
                showPicks = true,
                onTabSelect = {},
                onRetry = {},
                onUserClick = {},
            )
        }
    }
}
