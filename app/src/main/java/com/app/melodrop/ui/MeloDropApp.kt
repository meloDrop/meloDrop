package com.app.melodrop.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.app.melodrop.data.TokenStore
import com.app.melodrop.network.AuthEvents
import com.app.melodrop.ui.auth.LoginScreen
import com.app.melodrop.ui.main.MainScreen
import kotlinx.coroutines.launch

// Root of the app: shows the login screen or the main area
@Composable
fun MeloDropApp() {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    // null while the saved token is being read
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        TokenStore.load(context)
        isLoggedIn = TokenStore.token != null
    }

    // Clears the token and goes back to the login screen
    fun logout() {
        scope.launch {
            TokenStore.clear(context)
            isLoggedIn = false
        }
    }

    // The backend refused the saved token (ADR-0007)
    LaunchedEffect(Unit) {
        AuthEvents.sessionExpired.collect { logout() }
    }

    when (isLoggedIn) {
        null -> Unit
        false -> LoginScreen(onLoggedIn = { isLoggedIn = true })
        true -> MainScreen(onLogout = { logout() })
    }
}