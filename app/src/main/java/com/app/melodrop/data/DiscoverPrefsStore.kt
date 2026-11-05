package com.app.melodrop.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// Its own file on the phone, apart from the token ("auth"): logging out never clears it
private val Context.discoverStore by preferencesDataStore(name = "discover")

// Remembers the choices made on the Discover screen
object DiscoverPrefsStore {

    private val FOLLOWS_FIRST = booleanPreferencesKey("follows_first")

    // True until the user turns it off: the drops of the followed users come first
    suspend fun followsFirst(context: Context): Boolean =
        context.discoverStore.data.first()[FOLLOWS_FIRST] ?: true

    suspend fun setFollowsFirst(context: Context, value: Boolean) {
        context.discoverStore.edit { it[FOLLOWS_FIRST] = value }
    }
}
