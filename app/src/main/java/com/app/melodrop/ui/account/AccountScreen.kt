package com.app.melodrop.ui.account

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.network.dto.MeOut
import com.app.melodrop.ui.profile.SectionTitle
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    // Called once the account is deleted: logs out, like the logout button
    onAccountDeleted: () -> Unit,
    viewModel: AccountViewModel = viewModel(),
) {
    LaunchedEffect(viewModel.uiState.isDeleted) {
        if (viewModel.uiState.isDeleted) onAccountDeleted()
    }

    AccountContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onRetry = viewModel::load,
        onOpenEmailForm = viewModel::openEmailForm,
        onRequestEmailChange = viewModel::requestEmailChange,
        onConfirmEmail = viewModel::confirmEmail,
        onOpenPasswordForm = viewModel::openPasswordForm,
        onChangePassword = viewModel::changePassword,
        onDeleteAccount = viewModel::deleteAccount,
    )
}

// Pure UI: back arrow, title, then the account sections, a spinner or an error
@Composable
private fun AccountContent(
    uiState: AccountUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenEmailForm: (Boolean) -> Unit,
    onRequestEmailChange: (newEmail: String, password: String) -> Unit,
    onConfirmEmail: (code: String) -> Unit,
    onOpenPasswordForm: (Boolean) -> Unit,
    onChangePassword: (old: String, new: String, confirm: String) -> Unit,
    onDeleteAccount: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Stays above the keyboard when it is open
            .imePadding()
            // Scrolls when the forms do not fit on the screen
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
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
                text = "Mon compte",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Local copy: Kotlin can then treat it as not null in the first branch
        val me = uiState.me

        when {
            me != null -> {
                AccountInfo(me = me)
                EmailSection(
                    me = me,
                    uiState = uiState,
                    onOpenForm = onOpenEmailForm,
                    onRequestChange = onRequestEmailChange,
                    onConfirm = onConfirmEmail,
                )
                PasswordSection(
                    uiState = uiState,
                    onOpenForm = onOpenPasswordForm,
                    onChange = onChangePassword,
                )
                DeleteSection(uiState = uiState, onDelete = onDeleteAccount)
            }

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
                    text = uiState.loadError ?: "",
                    color = MaterialTheme.colorScheme.error,
                )
                Button(onClick = onRetry) {
                    Text("RÉESSAYER")
                }
            }
        }
    }
}

// The @username and the current email, in a card
@Composable
private fun AccountInfo(me: MeOut) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("COMPTE")

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InfoLine(label = "Pseudo", value = "@${me.username}")
                InfoLine(label = "E-mail", value = me.email)
            }
        }
    }
}

// A label on the left, its value on the right
@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

// Email change in two steps: the new email and the password, then the code
@Composable
private fun EmailSection(
    me: MeOut,
    uiState: AccountUiState,
    onOpenForm: (Boolean) -> Unit,
    onRequestChange: (newEmail: String, password: String) -> Unit,
    onConfirm: (code: String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("ADRESSE E-MAIL")

        val pendingEmail = me.pendingEmail
        when {
            // Step 1: the form is open
            uiState.isEmailFormOpen -> EmailForm(
                isBusy = uiState.isBusy,
                onSend = onRequestChange,
                onCancel = { onOpenForm(false) },
            )

            // Step 2: a new email waits for its code
            pendingEmail != null -> CodeForm(
                pendingEmail = pendingEmail,
                isBusy = uiState.isBusy,
                onConfirm = onConfirm,
                onOtherEmail = { onOpenForm(true) },
            )

            else -> OutlinedButton(
                onClick = { onOpenForm(true) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Changer d'adresse e-mail")
            }
        }

        SectionMessage(error = uiState.emailError, info = uiState.emailInfo)
    }
}

// Step 1: the new email and the current password
@Composable
private fun EmailForm(
    isBusy: Boolean,
    onSend: (newEmail: String, password: String) -> Unit,
    onCancel: () -> Unit,
) {
    // The fields live here: they are cleared when the form closes
    var newEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    OutlinedTextField(
        value = newEmail,
        onValueChange = { newEmail = it },
        label = { Text("Nouvelle adresse e-mail") },
        singleLine = true,
        enabled = !isBusy,
        modifier = Modifier.fillMaxWidth(),
        // Shows a keyboard with the @ key
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    )

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Mot de passe actuel") },
        singleLine = true,
        enabled = !isBusy,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )

    Text(
        text = "On t'envoie un code sur la nouvelle adresse. L'ancienne reste active tant que le code n'est pas saisi.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    BusyButton(label = "ENVOYER LE CODE", isBusy = isBusy, onClick = { onSend(newEmail, password) })

    TextButton(onClick = onCancel, enabled = !isBusy) {
        Text("Annuler")
    }
}

// Step 2: the 6-digit code received at the new email
@Composable
private fun CodeForm(
    pendingEmail: String,
    isBusy: Boolean,
    onConfirm: (code: String) -> Unit,
    onOtherEmail: () -> Unit,
) {
    var code by remember { mutableStateOf("") }

    Text(
        text = "Saisis le code reçu sur $pendingEmail. Ton adresse actuelle reste active en attendant.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    OutlinedTextField(
        value = code,
        onValueChange = { input ->
            // Keeps digits only, 6 at most
            if (input.length <= 6 && input.all { it.isDigit() }) code = input
        },
        label = { Text("Code à 6 chiffres") },
        singleLine = true,
        enabled = !isBusy,
        modifier = Modifier.fillMaxWidth(),
        // Digit-only keyboard
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    )

    BusyButton(label = "VALIDER", isBusy = isBusy, onClick = { onConfirm(code) })

    // A new request replaces the pending email and sends a new code
    TextButton(onClick = onOtherEmail, enabled = !isBusy) {
        Text("Utiliser une autre adresse ou recevoir un nouveau code")
    }
}

// Password change: the current one, the new one and its confirmation
@Composable
private fun PasswordSection(
    uiState: AccountUiState,
    onOpenForm: (Boolean) -> Unit,
    onChange: (old: String, new: String, confirm: String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("MOT DE PASSE")

        if (uiState.isPasswordFormOpen) {
            PasswordForm(
                isBusy = uiState.isBusy,
                onSave = onChange,
                onCancel = { onOpenForm(false) },
            )
        } else {
            OutlinedButton(
                onClick = { onOpenForm(true) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Changer de mot de passe")
            }
        }

        SectionMessage(error = uiState.passwordError, info = uiState.passwordInfo)
    }
}

@Composable
private fun PasswordForm(
    isBusy: Boolean,
    onSave: (old: String, new: String, confirm: String) -> Unit,
    onCancel: () -> Unit,
) {
    // The fields live here: they are cleared when the form closes
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    PasswordField(value = oldPassword, onValueChange = { oldPassword = it }, label = "Mot de passe actuel", enabled = !isBusy)
    PasswordField(value = newPassword, onValueChange = { newPassword = it }, label = "Nouveau mot de passe", enabled = !isBusy)
    PasswordField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirme le nouveau mot de passe", enabled = !isBusy)

    Text(
        text = "8 caractères minimum, avec au moins un chiffre et un caractère spécial.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    BusyButton(
        label = "ENREGISTRER",
        isBusy = isBusy,
        onClick = { onSave(oldPassword, newPassword, confirmPassword) },
    )

    TextButton(onClick = onCancel, enabled = !isBusy) {
        Text("Annuler")
    }
}

// A password field: the characters are hidden
@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
}

// Account deletion: a red button, then a dialog that explains what is erased
@Composable
private fun DeleteSection(uiState: AccountUiState, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("SUPPRESSION DU COMPTE")

        OutlinedButton(
            onClick = { confirmDelete = true },
            enabled = !uiState.isBusy,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text("SUPPRIMER MON COMPTE")
        }

        SectionMessage(error = uiState.deleteError, info = null)
    }

    // Like the Stage 3 mockup: the dialog explains what will be erased
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Supprimer ton compte ?") },
            text = {
                Text(
                    "Ton compte, tes drops et ta liste « Mes likes » seront effacés pour de bon. " +
                        "Les titres que d'autres ont gardés grâce à toi restent dans leurs likes. " +
                        "Cette action est définitive.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("SUPPRIMER")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("ANNULER")
                }
            },
        )
    }
}

// Main button of a form, with a small spinner while the call runs
@Composable
private fun BusyButton(label: String, isBusy: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isBusy,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(label)
        }
    }
}

// Message under a section: an error in red, or good news in mint
@Composable
private fun SectionMessage(error: String?, info: String?) {
    if (error != null) {
        Text(text = error, color = MaterialTheme.colorScheme.error)
    } else if (info != null) {
        Text(text = info, color = MaterialTheme.colorScheme.tertiary)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C10)
@Composable
private fun AccountScreenPreview() {
    MeloDropTheme {
        AccountContent(
            uiState = AccountUiState(
                isLoading = false,
                me = MeOut(
                    id = 1,
                    email = "lito@example.com",
                    username = "litoxam",
                    createdAt = "2026-09-07T12:00:00Z",
                    pendingEmail = "lito.new@example.com",
                ),
                passwordInfo = "Ton mot de passe a été changé.",
            ),
            onBack = {},
            onRetry = {},
            onOpenEmailForm = {},
            onRequestEmailChange = { _, _ -> },
            onConfirmEmail = {},
            onOpenPasswordForm = {},
            onChangePassword = { _, _, _ -> },
            onDeleteAccount = {},
        )
    }
}
