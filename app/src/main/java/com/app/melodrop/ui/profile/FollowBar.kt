package com.app.melodrop.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.StatsOut
import com.app.melodrop.ui.follows.FollowTab
import com.app.melodrop.ui.theme.MeloDropTheme

// Under the header of a profile: the followers and the followed users (a tap opens the
// lists), and on the profile of the others the "Suivre" button. Shared by both profiles
@Composable
internal fun FollowBar(
    profile: ProfileOut,
    // Tap on a counter: FollowTab.FOLLOWERS or FollowTab.FOLLOWING
    onCountClick: (String) -> Unit,
    // null: no button (the user's own profile)
    onFollowClick: (() -> Unit)? = null,
    // True while the backend has not answered: the button waits
    isFollowBusy: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FollowCount(
            count = profile.followersCount,
            label = if (profile.followersCount <= 1) "abonné" else "abonnés",
            onClick = { onCountClick(FollowTab.FOLLOWERS) },
        )
        FollowCount(
            count = profile.followingCount,
            label = if (profile.followingCount <= 1) "abonnement" else "abonnements",
            onClick = { onCountClick(FollowTab.FOLLOWING) },
        )

        // Pushes the button against the right edge
        Spacer(modifier = Modifier.weight(1f))

        // Never on the user's own profile, even opened from a list
        if (onFollowClick != null && !profile.isMe) {
            FollowButton(
                isFollowed = profile.isFollowed,
                followsYou = profile.followsYou,
                enabled = !isFollowBusy,
                onClick = onFollowClick,
            )
        }
    }
}

// "12 abonnés": the number in bold, the word in grey. A tap opens the list
@Composable
private fun FollowCount(
    count: Int,
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                append(count.toString())
            }
            append(" $label")
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClickLabel = "Voir la liste", onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
    )
}

// "Suivre" in lavender, "Suivre en retour" when this user already follows the logged-in
// user, "Abonné" with a border once followed: a tap on it stops following
@Composable
private fun FollowButton(
    isFollowed: Boolean,
    followsYou: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    // Smaller than the usual buttons: it shares the line with the counters
    val padding = PaddingValues(horizontal = 16.dp)
    val modifier = Modifier.height(36.dp)

    if (isFollowed) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            contentPadding = padding,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Text(
                text = "Abonné",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            contentPadding = padding,
        ) {
            Text(if (followsYou) "Suivre en retour" else "Suivre")
        }
    }
}

private val previewProfile = ProfileOut(
    id = 2,
    username = "elliot",
    displayName = "Elliot",
    createdAt = "2026-10-01T10:00:00Z",
    avatarColor = "peach",
    avatarStyle = "rock",
    stats = StatsOut(dropsCount = 12, likesReceived = 30, likesCount = 18),
    recentDrops = emptyList(),
    followersCount = 12,
    followingCount = 1,
    followsYou = true,
)

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun FollowBarPreview() {
    MeloDropTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Own profile: no button
            FollowBar(profile = previewProfile.copy(isMe = true), onCountClick = {})
            // Follows the logged-in user: "Suivre en retour"
            FollowBar(profile = previewProfile, onCountClick = {}, onFollowClick = {})
            // Already followed: "Abonné"
            FollowBar(profile = previewProfile.copy(isFollowed = true), onCountClick = {}, onFollowClick = {})
        }
    }
}
