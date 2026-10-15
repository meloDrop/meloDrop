package com.app.melodrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.app.melodrop.data.TokenStore
import com.app.melodrop.ui.auth.LoginScreen
import com.app.melodrop.ui.theme.MeloDropTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            MeloDropTheme {
                // null while the saved token is being read
                var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    TokenStore.load(applicationContext)
                    isLoggedIn = TokenStore.token != null
                }

                when (isLoggedIn) {
                    null -> Unit
                    false -> LoginScreen(onLoggedIn = { isLoggedIn = true })
                    true -> HomePlaceholder(onLoggedOut = { isLoggedIn = false })
                }
            }
        }
    }
}

// Temporary screen until the Discover feed exists
@Composable
private fun HomePlaceholder(onLoggedOut: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text("Connecté")
        Button(onClick = {
            scope.launch {
                TokenStore.clear(context)
                onLoggedOut()
            }
        }) {
            Text("Se déconnecter")
        }
    }
}