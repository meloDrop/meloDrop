package com.app.melodrop.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// Its own file on the phone, apart from the token ("auth"): logging out never clears it
private val Context.tutorialStore by preferencesDataStore(name = "tutorial")

// Remembers which tabs already showed their tutorial on this phone
object TutorialStore {

    // True the first time a tab opens on this phone, or after a new account: its tutorial opens.
    // Noted at once, like the "Nouveautés" sheet: it never opens by itself again.
    // screen: name of the tab, like "DISCOVER"
    suspend fun checkFirstVisit(context: Context, screen: String): Boolean {
        val key = booleanPreferencesKey("seen_$screen")
        val seen = context.tutorialStore.data.first()[key] ?: false
        if (seen) return false

        context.tutorialStore.edit { it[key] = true }
        return true
    }

    // A new account was created on this phone: every tutorial opens again
    suspend fun reset(context: Context) {
        context.tutorialStore.edit { it.clear() }
    }
}
