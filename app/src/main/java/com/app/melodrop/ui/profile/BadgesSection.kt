package com.app.melodrop.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.melodrop.network.dto.BadgeOut
import com.app.melodrop.ui.components.avatarColor
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Peach
import com.app.melodrop.ui.theme.Rose
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Badges on one line of the Mixtape
private const val BADGES_PER_ROW = 4

// Color code of the badges that take all the pastels
private const val SPECTRUM = "spectrum"

// Dark disc inside the "spectrum" badges, like on the mockup
private val SpectrumDisc = Color(0xFF211F2B)

// The 5 pastels around the "spectrum" badges
private val SpectrumRing = Brush.linearGradient(listOf(Lavender, Rose, Peach, Butter, Mint))

// The badges of the Mixtape, 4 per line: given by the meloDrop team from the dashboard.
// A tap on a badge opens its details. Shared by the user's own profile and the profile of the others
@Composable
internal fun BadgesSection(
    // The most recently given first
    badges: List<BadgeOut>,
    // Shown when there is no badge yet, null to show nothing
    emptyText: String?,
) {
    // Badge whose details are open, null when the window is closed
    var openBadge by remember { mutableStateOf<BadgeOut?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("BADGES")

        if (badges.isEmpty() && emptyText != null) {
            Text(
                text = emptyText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // A Column of Rows, because a LazyVerticalGrid cannot be placed inside a Column that scrolls
        badges.chunked(BADGES_PER_ROW).forEach { line ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                line.forEach { badge ->
                    BadgeTile(
                        badge = badge,
                        onClick = { openBadge = badge },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Empty places at the end of the last line: the badges keep the same width
                repeat(BADGES_PER_ROW - line.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    openBadge?.let { badge ->
        BadgeDialog(badge = badge, onDismiss = { openBadge = null })
    }
}

// One badge of the grid: the circle, its name under it
@Composable
private fun BadgeTile(
    badge: BadgeOut,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClickLabel = "Voir le badge", role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BadgeCircle(badge = badge, size = 56.dp)
        Text(
            text = badge.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            // The longest names go on two lines
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// A badge: a ring of its color, a paler disc and its icon. "spectrum": the 5 pastels around
// a dark disc, the icon in rose. An unknown color shows lavender, like the avatars
@Composable
internal fun BadgeCircle(
    badge: BadgeOut,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val isSpectrum = badge.color == SPECTRUM
    val color = if (isSpectrum) Rose else avatarColor(badge.color)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isSpectrum) SpectrumDisc else color.copy(alpha = 0.14f))
            .border(
                border = BorderStroke(2.dp, if (isSpectrum) SpectrumRing else SolidColor(color)),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = badgeIcon(badge.icon),
            // The name of the badge is written next to it
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size * 0.43f),
        )
    }
}

// Details of a badge: bigger, with its description and the day it was given
@Composable
private fun BadgeDialog(
    badge: BadgeOut,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { BadgeCircle(badge = badge, size = 72.dp) },
        title = {
            Text(
                text = badge.name,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = badge.description,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = awardedText(badge.awardedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("FERMER")
            }
        },
    )
}

// "Attribué par meloDrop · 3 nov. 2026", from a UTC date like "2026-11-03T18:00:00Z"
private fun awardedText(awardedAt: String): String {
    val day = runCatching {
        OffsetDateTime.parse(awardedAt)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH))
    }.getOrNull()
    // A date the app can't read: the day is left out
    return if (day != null) "Attribué par meloDrop · $day" else "Attribué par meloDrop"
}

private val previewBadges = listOf(
    BadgeOut(1, "Testeur de la 1re heure", "Tu as testé meloDrop avant sa sortie.", "drop", "spectrum", "2026-11-03T18:00:00Z"),
    BadgeOut(2, "Tube du jour", "Le drop le plus liké d'une journée.", "flame", "peach", "2026-11-02T18:00:00Z"),
    BadgeOut(3, "Oreille d'or", "Tes drops plaisent à presque tout le monde.", "headphones", "butter", "2026-11-01T18:00:00Z"),
    BadgeOut(4, "7 jours d'affilée", "Un drop par jour pendant 7 jours.", "calendar", "mint", "2026-10-30T18:00:00Z"),
    BadgeOut(5, "Pionnier du vinyle", "10 titres sortis avant 1980.", "vinyl", "lavender", "2026-10-29T18:00:00Z"),
)

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun BadgesSectionPreview() {
    MeloDropTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            BadgesSection(badges = previewBadges, emptyText = null)
            BadgesSection(
                badges = emptyList(),
                emptyText = "Pas encore de badge. L'équipe meloDrop en donne pour récompenser certains membres.",
            )
        }
    }
}
