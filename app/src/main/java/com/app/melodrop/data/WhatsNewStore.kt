package com.app.melodrop.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.melodrop.BuildConfig
import kotlinx.coroutines.flow.first

// Its own file on the phone, apart from the token ("auth"): logging out never clears it
private val Context.whatsNewStore by preferencesDataStore(name = "whats_new")

// Remembers the last version whose "Nouveautés" sheet opened by itself
object WhatsNewStore {

    private val KEY = intPreferencesKey("last_seen_version")

    // True the first time the app opens after an update: the sheet must open by itself.
    // The version is noted as seen at once, so the sheet never opens by itself again
    suspend fun checkNewVersion(context: Context): Boolean {
        val current = BuildConfig.VERSION_CODE
        val lastSeen = context.whatsNewStore.data.first()[KEY]

        // This version was already seen: nothing to open
        if (lastSeen == current) return false

        // Noted now: after a restart it does not open again, even if it was not closed
        context.whatsNewStore.edit { it[KEY] = current }

        // First install: nothing is "new" for someone who discovers the app
        return !isFirstInstall(context)
    }
}

// Android keeps two dates: the first install and the last update.
// They stay the same until the app is updated once
private fun isFirstInstall(context: Context): Boolean {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    return info.firstInstallTime == info.lastUpdateTime
}