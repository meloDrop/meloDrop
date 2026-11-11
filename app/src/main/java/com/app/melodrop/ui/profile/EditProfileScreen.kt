package com.app.melodrop.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.R
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.StatsOut
import com.app.melodrop.ui.components.AVATAR_AURAS
import com.app.melodrop.ui.components.AVATAR_COLORS
import com.app.melodrop.ui.components.AVATAR_DECOS
import com.app.melodrop.ui.components.AVATAR_DROPS
import com.app.melodrop.ui.components.AVATAR_FACES
import com.app.melodrop.ui.components.AVATAR_STYLES
import com.app.melodrop.ui.components.Avatar
import com.app.melodrop.ui.components.AvatarChoice
import com.app.melodrop.ui.components.Unbounded
import com.app.melodrop.ui.components.avatarColor
import com.app.melodrop.ui.theme.Butter
import com.app.melodrop.ui.theme.MeloDropTheme

// Choices on each line of the avatar pickers
private const val OPTIONS_PER_ROW = 4

// The avatar at the top of the screen: bigger than on the profile, so each choice shows well
private val BIG_AVATAR_SIZE = 112.dp

// Small avatars of the pickers. With an aura, the avatar is smaller: its box grows
private val OPTION_AVATAR_SIZE = 50.dp
private val OPTION_AURA_AVATAR_SIZE = 40.dp

// The tabs under the avatar, in their order on the screen
private enum class AvatarTab(val label: String) {
    STYLE("Style"),

    // Color of the drop
    COLOR("Couleur"),

    // Color of the background, and its decoration
    BACKGROUND("Fond"),
    FACE("Visage"),
    AURA("Aura"),
}

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
        onAvatarDropChange = viewModel::onAvatarDropChange,
        onAvatarDecoChange = viewModel::onAvatarDecoChange,
        onAvatarFaceChange = viewModel::onAvatarFaceChange,
        onAvatarAuraChange = viewModel::onAvatarAuraChange,
        onSave = viewModel::save,
        onRetry = viewModel::load,
    )
}

// Pure UI: back arrow, title, then the form, a spinner or an error.
// The screen does not scroll: only the choices of the open tab do
@Composable
private fun EditProfileContent(
    uiState: EditProfileUiState,
    onBack: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onAvatarColorChange: (String) -> Unit,
    onAvatarStyleChange: (String) -> Unit,
    onAvatarDropChange: (String) -> Unit,
    onAvatarDecoChange: (String) -> Unit,
    onAvatarFaceChange: (String) -> Unit,
    onAvatarAuraChange: (String) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Stays above the keyboard when it is open
            .imePadding()
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
                onAvatarDropChange = onAvatarDropChange,
                onAvatarDecoChange = onAvatarDecoChange,
                onAvatarFaceChange = onAvatarFaceChange,
                onAvatarAuraChange = onAvatarAuraChange,
                onSave = onSave,
                // All the room left under the title
                modifier = Modifier.weight(1f),
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

// The big avatar and the name, the tabs, the choices of the open tab and the save button
@Composable
private fun EditForm(
    uiState: EditProfileUiState,
    profile: ProfileOut,
    onDisplayNameChange: (String) -> Unit,
    onAvatarColorChange: (String) -> Unit,
    onAvatarStyleChange: (String) -> Unit,
    onAvatarDropChange: (String) -> Unit,
    onAvatarDecoChange: (String) -> Unit,
    onAvatarFaceChange: (String) -> Unit,
    onAvatarAuraChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tab open, kept after a rotation
    var selectedTab by rememberSaveable { mutableStateOf(AvatarTab.STYLE) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AvatarHeader(
            uiState = uiState,
            profile = profile,
            onDisplayNameChange = onDisplayNameChange,
        )

        AvatarTabs(
            selected = selectedTab,
            auraLocked = !uiState.premiumUnlocked,
            onSelect = { selectedTab = it },
        )

        // Only the choices scroll: the avatar, the tabs and the button stay in sight.
        // key: each tab has its own scroll, and opens at its top
        key(selectedTab) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TabChoices(
                    tab = selectedTab,
                    uiState = uiState,
                    onAvatarColorChange = onAvatarColorChange,
                    onAvatarStyleChange = onAvatarStyleChange,
                    onAvatarDropChange = onAvatarDropChange,
                    onAvatarDecoChange = onAvatarDecoChange,
                    onAvatarFaceChange = onAvatarFaceChange,
                    onAvatarAuraChange = onAvatarAuraChange,
                )
            }
        }

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
}

// Preview of the profile header: the big avatar with its aura, then the name.
// A tap on the name opens its field. Changes while the user types or taps
@Composable
private fun AvatarHeader(
    uiState: EditProfileUiState,
    profile: ProfileOut,
    onDisplayNameChange: (String) -> Unit,
) {
    // True while the name field is open, kept after a rotation
    var isEditingName by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Avatar(
            color = uiState.avatarColor,
            style = uiState.avatarStyle,
            drop = uiState.avatarDrop,
            deco = uiState.avatarDeco,
            face = uiState.avatarFace,
            aura = uiState.avatarAura,
            showAura = true,
            avatarSize = BIG_AVATAR_SIZE,
            // The room of the aura is kept even without one: nothing moves when it changes
            reserveAuraRoom = true,
        )

        if (isEditingName) {
            NameField(
                uiState = uiState,
                profile = profile,
                onDisplayNameChange = onDisplayNameChange,
                onDone = { isEditingName = false },
            )
        } else {
            NameLine(
                uiState = uiState,
                profile = profile,
                enabled = !uiState.isSaving,
                onEdit = { isEditingName = true },
            )
        }
    }
}

// The name, as on the profile, with a pencil. Empty field: the @username shows instead
@Composable
private fun NameLine(
    uiState: EditProfileUiState,
    profile: ProfileOut,
    enabled: Boolean,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            // The whole line opens the field, not only the pencil: easier to hit
            .clickable(
                enabled = enabled,
                onClickLabel = "Modifier le nom affiché",
                role = Role.Button,
                onClick = onEdit,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        // Centered: the name and its pencil stay under the avatar
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Text(
            text = uiState.displayName.trim().ifEmpty { profile.username },
            fontFamily = Unbounded,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            // A long name is cut, and the pencil stays in sight
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(
            painter = painterResource(R.drawable.ic_edit),
            // The line already tells TalkBack what a tap does
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }

    Text(
        text = "@${profile.username}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// The name field, opened by the pencil: the keyboard opens with it, "Done" closes it.
// The name is only sent with the rest, by the save button
@Composable
private fun NameField(
    uiState: EditProfileUiState,
    profile: ProfileOut,
    onDisplayNameChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = uiState.displayName,
        onValueChange = onDisplayNameChange,
        label = { Text("Nom affiché") },
        placeholder = { Text(profile.username) },
        // What an empty field shows, and the counter, so the user sees the limit coming.
        // The @username itself never changes: it is on the drops and is used to log in
        supportingText = {
            Text("Vide : @${profile.username} s'affiche · ${uiState.displayName.length}/$DISPLAY_NAME_MAX")
        },
        singleLine = true,
        enabled = !uiState.isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        // Capital letter at the start of each word, and a "Done" key that closes the field
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onDone()
            },
        ),
    )

    // Puts the cursor in the field as soon as it opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

// The 5 tabs under the avatar: a lavender line under the open one.
// A Row instead of a Material TabRow: with 5 tabs, the padding of a Tab cuts "Couleur"
@Composable
private fun AvatarTabs(
    selected: AvatarTab,
    auraLocked: Boolean,
    onSelect: (AvatarTab) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // TalkBack reads them as one group of tabs
                .selectableGroup(),
        ) {
            AvatarTab.entries.forEach { tab ->
                val isSelected = tab == selected
                val color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onSelect(tab) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        // 44 dp with the line under it: enough room for a finger
                        modifier = Modifier.height(42.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        // The auras need the subscription: a small lock on their tab
                        if (tab == AvatarTab.AURA && auraLocked) {
                            Icon(
                                painter = painterResource(R.drawable.ic_lock),
                                contentDescription = "Avec l'abonnement",
                                tint = Butter,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                    )
                }
            }
        }

        // Thin line under all the tabs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )
    }
}

// The choices of the open tab. Each picker shows the current avatar,
// with only its own part changed
@Composable
private fun TabChoices(
    tab: AvatarTab,
    uiState: EditProfileUiState,
    onAvatarColorChange: (String) -> Unit,
    onAvatarStyleChange: (String) -> Unit,
    onAvatarDropChange: (String) -> Unit,
    onAvatarDecoChange: (String) -> Unit,
    onAvatarFaceChange: (String) -> Unit,
    onAvatarAuraChange: (String) -> Unit,
) {
    val enabled = !uiState.isSaving

    when (tab) {
        AvatarTab.STYLE -> AvatarOptionPicker(
            choices = AVATAR_STYLES,
            selected = uiState.avatarStyle,
            enabled = enabled,
            onSelect = onAvatarStyleChange,
        ) { name ->
            OptionAvatar(uiState, style = name)
        }

        AvatarTab.COLOR -> {
            if (!uiState.premiumUnlocked) {
                PremiumHint("Océan, Prune, Or et Spectre arrivent avec l'abonnement meloDrop.")
            }
            AvatarOptionPicker(
                choices = AVATAR_DROPS,
                selected = uiState.avatarDrop,
                enabled = enabled,
                onSelect = onAvatarDropChange,
                isLocked = uiState::isDropLocked,
            ) { name ->
                OptionAvatar(uiState, drop = name)
            }
        }

        AvatarTab.BACKGROUND -> {
            SectionTitle("COULEUR DU FOND")
            ColorPicker(
                selected = uiState.avatarColor,
                enabled = enabled,
                onSelect = onAvatarColorChange,
            )
            SectionTitle("DÉCOR")
            AvatarOptionPicker(
                choices = AVATAR_DECOS,
                selected = uiState.avatarDeco,
                enabled = enabled,
                onSelect = onAvatarDecoChange,
            ) { name ->
                OptionAvatar(uiState, deco = name)
            }
        }

        AvatarTab.FACE -> AvatarOptionPicker(
            choices = AVATAR_FACES,
            selected = uiState.avatarFace,
            enabled = enabled,
            onSelect = onAvatarFaceChange,
        ) { name ->
            OptionAvatar(uiState, face = name)
        }

        AvatarTab.AURA -> {
            if (!uiState.premiumUnlocked) {
                PremiumHint("Les auras arrivent avec l'abonnement meloDrop.")
            }
            AvatarOptionPicker(
                choices = AVATAR_AURAS,
                selected = uiState.avatarAura,
                enabled = enabled,
                onSelect = onAvatarAuraChange,
                isLocked = uiState::isAuraLocked,
            ) { name ->
                OptionAvatar(uiState, aura = name)
            }
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

// The current avatar, with one part replaced: the preview of a choice in a picker.
// The aura shows still, on a smaller avatar so it fits in the ring
@Composable
private fun OptionAvatar(
    uiState: EditProfileUiState,
    style: String = uiState.avatarStyle,
    drop: String = uiState.avatarDrop,
    deco: String = uiState.avatarDeco,
    face: String = uiState.avatarFace,
    aura: String? = null,
) {
    Avatar(
        color = uiState.avatarColor,
        style = style,
        drop = drop,
        deco = deco,
        face = face,
        aura = aura,
        showAura = aura != null,
        animateAura = false,
        avatarSize = if (aura != null) OPTION_AURA_AVATAR_SIZE else OPTION_AVATAR_SIZE,
    )
}

// Short text above the choices of a tab, when its premium choices are locked
@Composable
private fun PremiumHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Butter,
    )
}

// The choices of one part of the avatar, 4 per line, each drawn by preview.
// A Column of Rows, like DropsGrid, because the tab around it already scrolls.
// isLocked: true for a choice that needs the subscription: dimmed, with a lock
@Composable
private fun AvatarOptionPicker(
    choices: List<AvatarChoice>,
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
    isLocked: (String) -> Boolean = { false },
    preview: @Composable (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        choices.chunked(OPTIONS_PER_ROW).forEach { line ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                line.forEach { choice ->
                    val isSelected = choice.name == selected
                    val locked = isLocked(choice.name)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            // A locked choice can still be tapped: the screen explains why
                            .selectable(
                                selected = isSelected,
                                enabled = enabled,
                                role = Role.RadioButton,
                                onClick = { onSelect(choice.name) },
                            )
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // White ring around the chosen one
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
                            Box(modifier = Modifier.graphicsLayer { alpha = if (locked) 0.45f else 1f }) {
                                preview(choice.name)
                            }
                            if (locked) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lock),
                                    contentDescription = "Avec l'abonnement",
                                    tint = Butter,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(16.dp),
                                )
                            }
                        }

                        Text(
                            text = choice.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
                // Empty places on the last line, so the choices keep the same width
                repeat(OPTIONS_PER_ROW - line.size) {
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
                avatarStyle = "jazz",
                avatarDrop = "gold",
                avatarDeco = "waves",
                avatarFace = "smile",
                avatarAura = "equalizer",
                premiumUnlocked = false,
            ),
            onBack = {},
            onDisplayNameChange = {},
            onAvatarColorChange = {},
            onAvatarStyleChange = {},
            onAvatarDropChange = {},
            onAvatarDecoChange = {},
            onAvatarFaceChange = {},
            onAvatarAuraChange = {},
            onSave = {},
            onRetry = {},
        )
    }
}
