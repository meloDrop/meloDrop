package com.app.melodrop.ui.likes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.melodrop.network.dto.LikedSongOut
import com.app.melodrop.ui.theme.MeloDropTheme
import java.text.Normalizer
import java.util.Locale

// How the songs of "Mes likes" and "Mes picks" are ordered. Shared by the two screens
enum class LikesSort(val label: String) {
    // The most recent like first, like before
    RECENT("Récents"),
    // Grouped by genre, A to Z
    GENRE("Genre"),
    // Grouped by the user who dropped the song, A to Z
    DROPPER("Droppeur"),
}

// A group of songs under a small title ("Pop", "@elliot"). No title when sorted by date
data class LikesGroup(
    val title: String?,
    val songs: List<LikedSongOut>,
)

// Group titles of the songs without a genre or without a dropper: always at the end
private const val NO_GENRE = "Sans genre"
private const val RESCUED = "Récupérés dans les skips"
private const val NO_DROPPER = "Compte supprimé"

// Lowercase and without accents: "Électro" matches "electro"
private fun simplify(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase(Locale.FRENCH)

// True when the song matches the search: title, artist, genre or @dropper
fun LikedSongOut.matches(query: String): Boolean {
    // "@elliot" is searched as "elliot": the @ is not in the username
    val words = simplify(query).split(" ").map { it.removePrefix("@") }.filter { it.isNotBlank() }
    if (words.isEmpty()) return true
    val text = simplify(listOfNotNull(title, artist, genre, authorUsername).joinToString(" "))
    // Every word typed must be found somewhere
    return words.all { text.contains(it) }
}

// Title of the group of a song, for the chosen sort
private fun LikedSongOut.groupTitle(sort: LikesSort): String? = when (sort) {
    LikesSort.RECENT -> null
    LikesSort.GENRE -> genre ?: NO_GENRE
    LikesSort.DROPPER -> when {
        likedLate -> RESCUED
        authorUsername != null -> "@$authorUsername"
        else -> NO_DROPPER
    }
}

// Keeps the songs that match the search, then groups them for the chosen sort.
// Inside a group, the most recent like stays first (the order of the list)
fun groupLikes(songs: List<LikedSongOut>, query: String, sort: LikesSort): List<LikesGroup> {
    val kept = songs.filter { it.matches(query) }
    if (sort == LikesSort.RECENT) return if (kept.isEmpty()) emptyList() else listOf(LikesGroup(null, kept))

    val special = setOf(NO_GENRE, RESCUED, NO_DROPPER)
    return kept
        .groupBy { it.groupTitle(sort) ?: NO_GENRE }
        .map { (title, groupSongs) -> LikesGroup(title, groupSongs) }
        // A to Z without the accents, the groups without a genre or a dropper at the end
        .sortedWith(compareBy<LikesGroup>({ it.title in special }, { simplify(it.title ?: "") }))
}

// "3 titres sur 45" while searching, "45 titres" otherwise
fun shownCountLabel(shown: Int, total: Int, one: String, many: String): String = when {
    shown == total -> if (total <= 1) "$total $one" else "$total $many"
    else -> "$shown sur $total"
}

// Search field and sort pills, above the lists of "Mes likes" and "Mes picks"
@Composable
fun LikesToolbar(
    query: String,
    onQueryChange: (String) -> Unit,
    sort: LikesSort,
    onSortChange: (LikesSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Titre, artiste, genre ou @droppeur") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            // The list is filtered while typing: the key only closes the keyboard
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            trailingIcon = if (query.isEmpty()) null else {
                {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.semantics { contentDescription = "Effacer la recherche" },
                    ) {
                        Text(text = "✕", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
        )

        // One choice at a time: TalkBack reads them as radio buttons
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Trier :",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LikesSort.entries.forEach { option ->
                SortPill(
                    label = option.label,
                    selected = option == sort,
                    onClick = { onSortChange(option) },
                )
            }
        }
    }
}

// Same pill as "Abonnements d'abord" in Discover
@Composable
private fun SortPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(percent = 50)
    val accent = MaterialTheme.colorScheme.primary

    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.16f) else Color.Transparent)
            .border(BorderStroke(1.dp, if (selected) accent else MaterialTheme.colorScheme.outline), shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

// Small title of a group, above its songs
@Composable
fun LikesGroupTitle(title: String, count: Int, modifier: Modifier = Modifier) {
    Text(
        text = "$title · $count",
        modifier = modifier.padding(top = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun LikesToolbarPreview() {
    MeloDropTheme {
        LikesToolbar(
            query = "daft",
            onQueryChange = {},
            sort = LikesSort.GENRE,
            onSortChange = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
