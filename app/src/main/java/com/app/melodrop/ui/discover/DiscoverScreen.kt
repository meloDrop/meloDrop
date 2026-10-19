package com.app.melodrop.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.ui.components.MeloDropTitle
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel = viewModel()) {
    DiscoverContent(
        uiState = viewModel.uiState,
        onRefresh = viewModel::loadFeed,
    )
}

// Pure UI: only displays the state it receives
@Composable
private fun DiscoverContent(
    uiState: DiscoverUiState,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MeloDropTitle()

        // Takes all the space left under the title, content centered
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()

                uiState.error != null -> FeedMessage(
                    title = "Le fil n'a pas pu se charger",
                    text = uiState.error,
                    buttonLabel = "RÉESSAYER",
                    onClick = onRefresh,
                )

                uiState.drops.isEmpty() -> FeedMessage(
                    title = "Plus de drop à découvrir",
                    text = "Tu as fait le tour. Reviens plus tard, de nouveaux titres sont déposés chaque jour.",
                    buttonLabel = "ACTUALISER",
                    onClick = onRefresh,
                )

                // The first drop of the list is the one on screen
                else -> DropCard(drop = uiState.drops.first())
            }
        }
    }
}

// Centered message with a button: used for errors and the empty feed
@Composable
private fun FeedMessage(
    title: String,
    text: String,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onClick) {
            Text(buttonLabel)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun DiscoverEmptyPreview() {
    MeloDropTheme {
        DiscoverContent(uiState = DiscoverUiState(), onRefresh = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun DiscoverErrorPreview() {
    MeloDropTheme {
        DiscoverContent(
            uiState = DiscoverUiState(error = "Impossible de joindre le serveur."),
            onRefresh = {},
        )
    }
}