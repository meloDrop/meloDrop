package com.app.melodrop.ui.profile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// Icon shown for a code this version of the app does not know yet
private const val DEFAULT_BADGE_ICON = "star"

// The icons of the badges: same codes as BadgeIcon in the backend (schemas/badges.py),
// same drawings as the dashboard. Outlines on a 24 x 24 grid, drawn with a round stroke 2 wide
private val BADGE_ICON_PATHS = mapOf(
    // Goutte
    "drop" to "M12 3 C14 7 18 10 18 14.5 A6 6 0 0 1 6 14.5 C6 10 10 7 12 3 Z",
    // Flamme
    "flame" to "M12 3 c1.15 4.03 5.75 6.33 5.75 12.08 a5.75 5.75 0 0 1 -11.5 0 c0 -2.99 1.61 -4.83 2.99 -6.1 0.23 2.3 1.5 3.57 2.76 3.57 0 -3.68 -1.15 -6.1 0 -9.55 z",
    // Étoile
    "star" to "M12 3.5 l2.6 5.6 6.1 0.6 -4.6 4.1 1.3 6 -5.4 -3.1 -5.4 3.1 1.3 -6 -4.6 -4.1 6.1 -0.6 z",
    // Cœur
    "heart" to "M12 20.3 l-1.3 -1.2 C6 14.9 3 12.2 3 8.8 3 6.1 5.1 4 7.8 4 c1.5 0 3 0.7 4.2 1.9 C13.2 4.7 14.7 4 16.2 4 18.9 4 21 6.1 21 8.8 c0 3.4 -3 6.1 -7.7 10.3 z",
    // Casque
    "headphones" to "M4 15 v-3 a8 8 0 0 1 16 0 v3 M5 14 h1.5 a1.5 1.5 0 0 1 1.5 1.5 v3.5 a1.5 1.5 0 0 1 -1.5 1.5 H5 a1.5 1.5 0 0 1 -1.5 -1.5 v-3.5 A1.5 1.5 0 0 1 5 14 z M17.5 14 H19 a1.5 1.5 0 0 1 1.5 1.5 v3.5 a1.5 1.5 0 0 1 -1.5 1.5 h-1.5 a1.5 1.5 0 0 1 -1.5 -1.5 v-3.5 a1.5 1.5 0 0 1 1.5 -1.5 z",
    // Calendrier
    "calendar" to "M7 5.5 h10 a3 3 0 0 1 3 3 v9 a3 3 0 0 1 -3 3 H7 a3 3 0 0 1 -3 -3 v-9 a3 3 0 0 1 3 -3 z M4 10 h16 M8.5 3.5 v4 M15.5 3.5 v4 M9 15 l2 2 4 -4",
    // Éclair
    "bolt" to "M13 3 L5 13.5 h6 L10 21 l8 -10.5 h-6 z",
    // Couronne
    "crown" to "M5 16.5 L3.5 7.5 l4.5 3.5 L12 5 l4 6 4.5 -3.5 L19 16.5 z M5 20 h14",
    // Trophée
    "trophy" to "M8 4 h8 v5 a4 4 0 0 1 -8 0 z M8 6 H5 a3 3 0 0 0 3 4 M16 6 h3 a3 3 0 0 1 -3 4 M12 13 v4 M8 20 h8",
    // Vinyle
    "vinyl" to "M3.5 12 a8.5 8.5 0 1 0 17 0 a8.5 8.5 0 1 0 -17 0 z M9.5 12 a2.5 2.5 0 1 0 5 0 a2.5 2.5 0 1 0 -5 0 z M12 6.5 a5.5 5.5 0 0 1 5.5 5.5",
    // Micro
    "mic" to "M9 6 a3 3 0 0 1 6 0 v5 a3 3 0 0 1 -6 0 z M5.5 11 a6.5 6.5 0 0 0 13 0 M12 17.5 V21 M8.5 21 h7",
    // Note
    "note" to "M9 18 V5 l11 -2 v13 M3 18 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0 z M14 16 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0 z",
)

// Each icon is built once, the first time a badge is shown
private val BADGE_ICONS: Map<String, ImageVector> by lazy {
    BADGE_ICON_PATHS.mapValues { (code, path) -> buildBadgeIcon(code, path) }
}

// The icon of a badge, from its code. An unknown code shows the star
internal fun badgeIcon(code: String): ImageVector =
    BADGE_ICONS[code] ?: BADGE_ICONS.getValue(DEFAULT_BADGE_ICON)

// One outline icon: black, Icon() paints it in the color of the badge
private fun buildBadgeIcon(code: String, path: String): ImageVector =
    ImageVector.Builder(
        name = "badge_$code",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(path).toNodes(),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()
