package com.app.melodrop.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.network.dto.UserSearchOut
import com.app.melodrop.ui.components.Avatar
import com.app.melodrop.ui.components.LIST_AVATAR_SIZE
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun UserSearchScreen(
    onBack: () -> Unit,
    // Tap on a result: opens the profile of this user
    onUserClick: (Int) -> Unit,
    viewModel: UserSearchViewModel = viewModel(),
) {
    UserSearchContent(
        uiState = viewModel.uiState,
        onQueryChange = viewModel::onQueryChange,
        onBack = onBack,
        onUserClick = onUserClick,
    )
}

// Pure UI: back arrow, title, search field, then the results
@Composable
private fun UserSearchContent(
    uiState: UserSearchUiState,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onUserClick: (Int) -> Unit,
) {
    // Used to put the cursor in the field when the screen opens
    val focusRequester = remember { FocusRequester() }
    // Used to close the keyboard
    val focusManager = LocalFocusManager.current

    // Screen opened from Discover: the keyboard shows up right away.
    // Back from a profile, the text is still there: the keyboard stays closed
    LaunchedEffect(Unit) {
        if (uiState.query.isEmpty()) focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Text(
                text = "Rechercher",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            label = { Text("@pseudo") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            // Usernames: no accents added by the keyboard, search key instead of "enter"
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Search,
            ),
            // The search already runs while typing: the key only closes the keyboard
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        )

        // Takes all the space left under the field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopCenter,
        ) {
            when {
                uiState.isSearching -> CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))

                uiState.error != null -> InfoText(text = uiState.error, isError = true)

                // Same cleaning as the ViewModel: not enough letters yet
                uiState.query.trim().removePrefix("@").length < 2 -> InfoText(
                    text = "Tape au moins 2 lettres du @pseudo de quelqu'un.",
                )

                uiState.hasSearched && uiState.results.isEmpty() -> InfoText(
                    text = "Aucun utilisateur trouvé.",
                )

                // Only the visible rows are built
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // The key lets Compose follow each row when the results change
                    items(uiState.results, key = { it.id }) { user ->
                        UserRow(user = user, onClick = { onUserClick(user.id) })
                    }
                }
            }
        }
    }
}

// Grey message at the top of the results area, red for an error
@Composable
private fun InfoText(text: String, isError: Boolean = false) {
    Text(
        text = text,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 24.dp),
    )
}

// One user found: avatar with its aura, display name and @username
@Composable
private fun UserRow(user: UserSearchOut, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Rounded ripple when the row is tapped
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            // Less padding than before: the room of the aura already gives air around the avatar
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(
            color = user.avatarColor,
            style = user.avatarStyle,
            avatarSize = LIST_AVATAR_SIZE,
            drop = user.avatarDrop,
            deco = user.avatarDeco,
            face = user.avatarFace,
            aura = user.avatarAura,
            showAura = true,
            // Same room with or without an aura: the names of the list stay aligned
            reserveAuraRoom = true,
        )

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

// Fake results for the previews
private val previewUsers = listOf(
    UserSearchOut(id = 2, username = "elliot", displayName = "Elliot", avatarColor = "peach", avatarStyle = "rock"),
    UserSearchOut(id = 30, username = "elsa_b", displayName = null, avatarColor = "mint", avatarStyle = "pop"),
)

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun UserSearchResultsPreview() {
    MeloDropTheme {
        UserSearchContent(
            uiState = UserSearchUiState(query = "el", results = previewUsers, hasSearched = true),
            onQueryChange = {},
            onBack = {},
            onUserClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun UserSearchEmptyPreview() {
    MeloDropTheme {
        UserSearchContent(
            uiState = UserSearchUiState(query = "zz", hasSearched = true),
            onQueryChange = {},
            onBack = {},
            onUserClick = {},
        )
    }
}