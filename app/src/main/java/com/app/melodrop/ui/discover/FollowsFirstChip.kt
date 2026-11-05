package com.app.melodrop.ui.discover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.melodrop.ui.theme.MeloDropTheme

// "Abonnements d'abord" pill under the title of Discover: on, the drops of the followed
// users come first; off, every drop is mixed. The choice is kept on the phone
@Composable
internal fun FollowsFirstChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = 50)
    val accent = MaterialTheme.colorScheme.primary

    Text(
        // The check mark tells it is on, not only the color
        text = if (selected) "✓ Abonnements d'abord" else "Abonnements d'abord",
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.16f) else Color.Transparent)
            .border(BorderStroke(1.dp, if (selected) accent else MaterialTheme.colorScheme.outline), shape)
            // TalkBack reads it as a switch, on or off
            .toggleable(value = selected, role = Role.Switch, onValueChange = { onClick() })
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun FollowsFirstChipPreview() {
    MeloDropTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FollowsFirstChip(selected = true, onClick = {})
            FollowsFirstChip(selected = false, onClick = {})
        }
    }
}
