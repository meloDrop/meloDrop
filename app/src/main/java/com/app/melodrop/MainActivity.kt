package com.app.melodrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.melodrop.ui.theme.MeloDropTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            MeloDropTheme {
                ThemeTestScreen()
            }
        }
    }
}


@Preview()
@Composable
fun ThemeTestPreview() {
    MeloDropTheme {
        ThemeTestScreen()
    }
}

@Composable
fun ThemeTestScreen() {
    Scaffold(bottomBar = {
        Text("HELLO")
    }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text("meloDrop")
            Button(onClick = { }) {
                Text("SE CONNECTER")
            }
        }

    }

}
