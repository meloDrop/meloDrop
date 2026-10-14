package com.app.melodrop.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "auth")

object TokenStore {

    private val KEY = stringPreferencesKey("access_token")

    // In-memory copy, read by the interceptor on every request
    @Volatile
    var token: String? = null
        private set

    // Call once when the app starts
    suspend fun load(context: Context) {
        token = context.dataStore.data.first()[KEY]
    }

    suspend fun save(context: Context, value: String) {
        context.dataStore.edit { it[KEY] = value }
        token = value
    }

    suspend fun clear(context: Context) {
        context.dataStore.edit { it.remove(KEY) }
        token = null
    }
}