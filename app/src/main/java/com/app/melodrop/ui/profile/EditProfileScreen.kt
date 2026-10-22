package com.app.melodrop.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
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
import com.app.melodrop.ui.components.AVATAR_COLORS
import com.app.melodrop.ui.components.AVATAR_STYLES
import com.app.melodrop.ui.components.Avatar
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.components.avatarColor
import com.app.melodrop.ui.theme.MeloDropTheme

// Drop styles on each line of the style picker
private const val STYLES_PER_ROW = 4

// Connects the screen to its ViewModel
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = viewModel(),
) {
    // Saved: back to the profile, which loads the new name and avatar when it opens
    LaunchedEffect(viewModel.uiState.isSaved) {
        if (viewModel.uiState.isSaved) onBack()
    }

    EditProfileContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onAvatarColorChange = viewModel::onAvatarColorChange,
        onAvatarStyleChange = viewModel::onAvatarStyleChange,
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
    onAvatarColorChange: (String) -> Unit,
    onAvatarStyleChange: (String) -> Unit,
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
                onAvatarColorChange = onAvatarColorChange,
                onAvatarStyleChange = onAvatarStyleChange,
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

// Live preview, the name field, the avatar choice and the save button
@Composable
private fun EditForm(
    uiState: EditProfileUiState,
    profile: ProfileOut,
    onDisplayNameChange: (String) -> Unit,
    onAvatarColorChange: (String) -> Unit,
    onAvatarStyleChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    // Preview of the profile header: changes while the user types or taps
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Avatar(color = uiState.avatarColor, style = uiState.avatarStyle)

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

    SectionTitle("COULEUR")

    ColorPicker(
        selected = uiState.avatarColor,
        enabled = !uiState.isSaving,
        onSelect = onAvatarColorChange,
    )

    SectionTitle("STYLE DE GOUTTE")

    StylePicker(
        color = uiState.avatarColor,
        selected = uiState.avatarStyle,
        enabled = !uiState.isSaving,
        onSelect = onAvatarStyleChange,
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

// The 5 background colors: a white ring shows the chosen one
@Composable
private fun ColorPicker(
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AVATAR_COLORS.forEach { color ->
            val isSelected = color.name == selected

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    // Only one color at a time, like radio buttons
                    .selectable(
                        selected = isSelected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelect(color.name) },
                    )
                    .border(
                        width = 2.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                        shape = CircleShape,
                    )
                    // Gap between the ring and the color
                    .padding(5.dp)
                    .background(avatarColor(color.name), CircleShape)
                    // The color has no text: TalkBack reads its name
                    .semantics { contentDescription = color.label },
            )
        }
    }
}

// The 7 drop styles, 4 per line, drawn with the chosen color.
// A Column of Rows, like DropsGrid, because the screen already scrolls
@Composable
private fun StylePicker(
    color: String,
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AVATAR_STYLES.chunked(STYLES_PER_ROW).forEach { line ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                line.forEach { style ->
                    val isSelected = style.name == selected

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .selectable(
                                selected = isSelected,
                                enabled = enabled,
                                role = Role.RadioButton,
                                onClick = { onSelect(style.name) },
                            )
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // White ring around the chosen style
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Avatar(color = color, style = style.name, avatarSize = 50.dp)
                        }

                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
                // Empty places on the last line, so the styles keep the same width
                repeat(STYLES_PER_ROW - line.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
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
                avatarColor = "mint",
                avatarStyle = "electro",
            ),
            onBack = {},
            onDisplayNameChange = {},
            onAvatarColorChange = {},
            onAvatarStyleChange = {},
            onSave = {},
            onRetry = {},
        )
    }
}
