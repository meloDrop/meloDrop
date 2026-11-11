package com.app.melodrop.ui.people

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.melodrop.network.dto.UserBriefOut
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.components.UserRow
import com.app.melodrop.ui.theme.MeloDropTheme
import kotlinx.coroutines.launch

// The droppers of a song, or the author alone for the lists sent before ADR-0021
fun droppersOrAuthor(droppers: List<UserBriefOut>?, authorId: Int?, authorUsername: String?): List<UserBriefOut> {
    val list = droppers.orEmpty()
    if (list.isNotEmpty() || authorId == null || authorUsername == null) return list
    return listOf(UserBriefOut(id = authorId, username = authorUsername))
}

// "drop par @elliot" under a song of "Mes likes", "Mes picks" or "Mes skips",
// or "drop par 3 personnes ›" when several users dropped it (ADR-0021).
// One dropper: a tap opens their profile. Several: a tap opens the list,
// and each one opens their profile. Nothing when the list is empty
@Composable
fun DroppedByText(
    droppers: List<UserBriefOut>,
    onUserClick: (Int) -> Unit,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    // True while the list of droppers is open
    var showList by remember { mutableStateOf(false) }

    val first = droppers.firstOrNull() ?: return
    val isMany = droppers.size > 1

    Text(
        text = if (isMany) "drop par ${droppers.size} personnes ›" else "drop par @${first.username}",
        style = style,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClickLabel = if (isMany) "Voir qui a droppé" else "Voir le profil de @${first.username}") {
                if (isMany) showList = true else onUserClick(first.id)
            },
    )

    if (showList) {
        DroppersSheet(
            droppers = droppers,
            onUserClick = onUserClick,
            onDismiss = { showList = false },
        )
    }
}

// Bottom sheet with everyone who dropped a song: a tap on a user opens their profile.
// OptIn: ModalBottomSheet is marked as experimental in Material 3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DroppersSheet(
    droppers: List<UserBriefOut>,
    onUserClick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    // skipPartiallyExpanded: the sheet opens fully at once, not half-way
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        // Swipe down, tap outside the sheet, or back button
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        DroppersContent(
            droppers = droppers,
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

// Pure UI: the title and one row per dropper
@Composable
private fun DroppersContent(
    droppers: List<UserBriefOut>,
    onUserClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Droppé par ${droppers.size} personnes",
            fontFamily = Unbounded,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        UserList(users = droppers, onUserClick = onUserClick)
    }
}

// The users one under the other. A few dozen at most: a Column that scrolls is enough
@Composable
internal fun UserList(users: List<UserBriefOut>, onUserClick: (Int) -> Unit) {
    Column(
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
                avatarDrop = user.avatarDrop,
                avatarDeco = user.avatarDeco,
                avatarFace = user.avatarFace,
                avatarAura = user.avatarAura,
                onClick = { onUserClick(user.id) },
            )
        }
    }
}

// Fake users for the previews
internal val previewUsers = listOf(
    UserBriefOut(id = 2, username = "elliot", displayName = "Elliot", avatarColor = "peach", avatarStyle = "rock"),
    UserBriefOut(id = 3, username = "sam", avatarColor = "mint", avatarStyle = "pop"),
    UserBriefOut(id = 4, username = "lea_m", displayName = "Léa", avatarColor = "rose"),
)

// The content only: a ModalBottomSheet does not show in a Preview
@Preview(showBackground = true, backgroundColor = 0xFF18181F)
@Composable
private fun DroppersContentPreview() {
    MeloDropTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            DroppersContent(droppers = previewUsers, onUserClick = {})
        }
    }
}
