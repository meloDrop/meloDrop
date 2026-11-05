package com.app.melodrop.ui.profile

import androidx.compose.ui.graphics.Color
import com.app.melodrop.ui.theme.Lavender
import com.app.melodrop.ui.theme.Mint
import com.app.melodrop.ui.theme.Peach
import com.app.melodrop.ui.theme.Rose

// One label of the Mixtape: its code in the backend, its text, and its color
data class SongLabel(
    val code: String,
    val text: String,
    val color: Color
)

// The labels, in the order of the Mixtape: same codes and same order as SongLabel in the backend
val SONG_LABELS = listOf(
    SongLabel("feel_good", "Le son qui me met bien", Mint),
    SongLabel("party", "Le son qui retourne la soirée", Peach),
    SongLabel("never_skip", "Le son que je skippe jamais", Lavender),
    SongLabel("theme_song", "Mon générique préféré", Rose),
    SongLabel("chills", "Le son qui me file des frissons", Mint),
    SongLabel("cry", "Le son qui me fait chialer", Lavender),
    SongLabel("guilty_pleasure", "Le son que j'assume pas de ouf", Rose),
    SongLabel("cant_stand", "Le son que je supporte pas", Peach),
)

// The label of a code, or null if this version of the app does not know it
fun songLabel(code: String): SongLabel? = SONG_LABELS.find { it.code == code }
