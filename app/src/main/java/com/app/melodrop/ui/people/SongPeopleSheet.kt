package com.app.melodrop.ui.people

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.network.dto.UserBriefOut
import com.app.melodrop.ui.components.TabPill
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.theme.MeloDropTheme
import kotlinx.coroutines.launch

// Bottom sheet of a Discover card: who dropped the song, and who liked it, in two tabs.
// A pick counts as a like here: who picked stays private.
// OptIn: ModalBottomSheet is marked as experimental in Material 3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongPeopleSheet(
    songId: Int,
    songTitle: String,
    // They come with the card: no need to load them
    droppers: List<UserBriefOut>,
    // Tab open first: false = droppers, true = likes
    startOnLikers: Boolean,
    // Tap on a user: the sheet closes, then their profile opens
    onUserClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    viewModel: SongPeopleViewModel = viewModel(),
) {
    // Loads the likes each time the sheet opens
    LaunchedEffect(songId) {
        viewModel.load(songId)
    }

    // Tab shown, kept after a rotation
    var showLikers by rememberSaveable { mutableStateOf(startOnLikers) }

    // skipPartiallyExpanded: the sheet opens fully at once, not half-way
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        // Swipe down, tap outside the sheet, or back button
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        SongPeopleContent(
            songTitle = songTitle,
            droppers = droppers,
            uiState = viewModel.uiState,
            showLikers = showLikers,
            onTabSelect = { showLikers = it },
            onRetry = { viewModel.load(songId) },
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

// Pure UI: the title of the song, the two tabs, then the list
@Composable
private fun SongPeopleContent(
    songTitle: String,
    droppers: List<UserBriefOut>,
    uiState: SongPeopleUiState,
    showLikers: Boolean,
    onTabSelect: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onUserClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = songTitle,
            fontFamily = Unbounded,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TabPill(
                text = "Droppé par (${droppers.size})",
                isSelected = !showLikers,
                onClick = { onTabSelect(false) },
                modifier = Modifier.weight(1f),
            )
            // The number only once the likes are there
            TabPill(
                text = if (uiState.isLoading) "Liké par" else "Liké par (${uiState.likers.size})",
                isSelected = showLikers,
                onClick = { onTabSelect(true) },
                modifier = Modifier.weight(1f),
            )
        }

        // Local copy: Kotlin can then treat it as not null in its branch
        val error = uiState.error

        when {
            !showLikers -> UserList(users = droppers, onUserClick = onUserClick)

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

            uiState.likers.isEmpty() -> Text(
                text = "Personne n'a encore liké ce titre aujourd'hui.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            else -> UserList(users = uiState.likers, onUserClick = onUserClick)
        }
    }
}

// The content only: a ModalBottomSheet does not show in a Preview
@Preview(showBackground = true, backgroundColor = 0xFF18181F)
@Composable
private fun SongPeopleDroppersPreview() {
    MeloDropTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            SongPeopleContent(
                songTitle = "All I Want for Christmas Is You",
                droppers = previewUsers,
                uiState = SongPeopleUiState(likers = previewUsers.take(2)),
                showLikers = false,
                onTabSelect = {},
                onRetry = {},
                onUserClick = {},
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF18181F)
@Composable
private fun SongPeopleLikersPreview() {
    MeloDropTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            SongPeopleContent(
                songTitle = "All I Want for Christmas Is You",
                droppers = previewUsers,
                uiState = SongPeopleUiState(likers = previewUsers.take(2)),
                showLikers = true,
                onTabSelect = {},
                onRetry = {},
                onUserClick = {},
            )
        }
    }
}
