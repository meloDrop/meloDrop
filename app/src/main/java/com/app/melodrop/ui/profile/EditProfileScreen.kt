package com.app.melodrop.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.StatsOut
import com.app.melodrop.ui.components.Avatar
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = viewModel(),
) {
    // Saved: back to the profile, which loads the new name when it opens
    LaunchedEffect(viewModel.uiState.isSaved) {
        if (viewModel.uiState.isSaved) onBack()
    }

    EditProfileContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onSave = viewModel::save,
        onRetry = viewModel::load,
    )
}

// Pure UI: back arrow, title, then the form, a spinner or an error
@Composable
private fun EditProfileContent(
    uiState: EditProfileUiState,
    onBack: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Stays above the keyboard when it is open
            .imePadding()
            // Scrolls when the form does not fit on the screen
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.semantics { contentDescription = "Retour" },
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = "Modifier le profil",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Local copy: Kotlin can then treat it as not null in the first branch
        val profile = uiState.profile

        when {
            profile != null -> EditForm(
                uiState = uiState,
                profile = profile,
                onDisplayNameChange = onDisplayNameChange,
                onSave = onSave,
            )

            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                )
                Button(onClick = onRetry) {
                    Text("RÉESSAYER")
                }
            }
        }
    }
}

// Live preview, the field and the save button
@Composable
private fun EditForm(
    uiState: EditProfileUiState,
    profile: ProfileOut,
    onDisplayNameChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    // Preview of the profile header: changes while the user types
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Avatar(color = profile.avatarColor, style = profile.avatarStyle)

        Column {
            Text(
                // Empty field: the @username shows, like on the profile
                text = uiState.displayName.trim().ifEmpty { profile.username },
                fontFamily = Unbounded,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "@${profile.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    OutlinedTextField(
        value = uiState.displayName,
        onValueChange = onDisplayNameChange,
        label = { Text("Nom affiché") },
        placeholder = { Text(profile.username) },
        // Counter, so the user sees the limit coming
        supportingText = { Text("${uiState.displayName.length}/$DISPLAY_NAME_MAX") },
        singleLine = true,
        enabled = !uiState.isSaving,
        modifier = Modifier.fillMaxWidth(),
        // Capital letter at the start of each word, and a "Done" key that saves
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onSave() }),
    )

    Text(
        text = "Laisse vide pour afficher @${profile.username}. Ton @ ne change pas : " +
            "c'est lui qui apparaît sur tes drops et qui sert à te connecter.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    uiState.error?.let { message ->
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
        )
    }

    Button(
        onClick = onSave,
        enabled = !uiState.isSaving,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (uiState.isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text("ENREGISTRER")
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun EditProfilePreview() {
    MeloDropTheme {
        EditProfileContent(
            uiState = EditProfileUiState(
                isLoading = false,
                profile = ProfileOut(
                    id = 1,
                    username = "litoxam",
                    displayName = "Lito",
                    createdAt = "2026-09-07T12:00:00Z",
                    avatarColor = "lavender",
                    avatarStyle = "simple",
                    stats = StatsOut(dropsCount = 4, likesReceived = 6, likesCount = 4),
                    recentDrops = emptyList(),
                ),
                displayName = "Lito",
            ),
            onBack = {},
            onDisplayNameChange = {},
            onSave = {},
            onRetry = {},
        )
    }
}
