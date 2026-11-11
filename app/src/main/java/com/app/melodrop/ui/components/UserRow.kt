package com.app.melodrop.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Avatar of a user in a list. With the room of its aura, the box is 64 dp
val LIST_AVATAR_SIZE = 44.dp

// One user in a list: avatar with its aura, display name and @username, like in the user search.
// Used by the follow lists, the lists of likes and the list of who dropped a song.
// trailing: small content at the right end of the row, for example a star on a pick
@Composable
fun UserRow(
    username: String,
    displayName: String?,
    avatarColor: String,
    avatarStyle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
    // Cosmetics of the avatar: color of the drop, background decoration, face and aura
    avatarDrop: String? = null,
    avatarDeco: String? = null,
    avatarFace: String? = null,
    avatarAura: String? = null,
) {
    Row(
        modifier = modifier
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
            color = avatarColor,
            style = avatarStyle,
            avatarSize = LIST_AVATAR_SIZE,
            drop = avatarDrop,
            deco = avatarDeco,
            face = avatarFace,
            aura = avatarAura,
            showAura = true,
            // Same room with or without an aura: the names of the list stay aligned
            reserveAuraRoom = true,
        )

        // Takes the width left, so long names are cut with "…"
        Column(modifier = Modifier.weight(1f)) {
            // The display name, or the username when none was chosen (ADR-0019)
            Text(
                text = displayName ?: username,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "@$username",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        trailing()
    }
}
