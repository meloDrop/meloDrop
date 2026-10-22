package com.app.melodrop.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun ForgotPasswordScreen(
    onLoginClick: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel(),
) {
    // No LaunchedEffect: the screen shows its own success state,
    // then the user taps "Se connecter", like the verify screen
    ForgotPasswordContent(
        uiState = viewModel.uiState,
        onSendCode = viewModel::sendCode,
        onReset = viewModel::resetPassword,
        onResendCode = viewModel::resendCode,
        onChangeEmail = viewModel::changeEmail,
        onLoginClick = onLoginClick,
    )
}

// Pure UI: step 1 (email), step 2 (code and new password), or the success
@Composable
private fun ForgotPasswordContent(
    uiState: ForgotPasswordUiState,
    onSendCode: (email: String) -> Unit,
    onReset: (code: String, newPassword: String, confirmPassword: String) -> Unit,
    onResendCode: () -> Unit,
    onChangeEmail: () -> Unit,
    onLoginClick: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // Stays above the keyboard when it is open
                .imePadding()
                // Scrolls when the fields do not fit on the screen
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Local copy: Kotlin can then treat it as not null in its branch
            val email = uiState.email

            when {
                uiState.isReset -> SuccessStep(onLoginClick = onLoginClick)

                email != null -> ResetStep(
                    email = email,
                    uiState = uiState,
                    onReset = onReset,
                    onResendCode = onResendCode,
                    onChangeEmail = onChangeEmail,
                )

                else -> EmailStep(uiState = uiState, onSendCode = onSendCode)
            }

            if (!uiState.isReset) {
                TextButton(
                    onClick = onLoginClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Retour à la connexion")
                }
            }
        }
    }
}

// Step 1: the email of the account
@Composable
private fun EmailStep(
    uiState: ForgotPasswordUiState,
    onSendCode: (email: String) -> Unit,
) {
    var email by remember { mutableStateOf("") }

    Text(
        text = "Mot de passe oublié",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Text(
        text = "Saisis l'adresse e-mail de ton compte. On t'envoie un code à 6 chiffres pour choisir un nouveau mot de passe.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Adresse e-mail") },
        singleLine = true,
        enabled = !uiState.isLoading,
        modifier = Modifier.fillMaxWidth(),
        // Shows a keyboard with the @ key
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    )

    Messages(uiState = uiState)

    LoadingButton(label = "ENVOYER LE CODE", isLoading = uiState.isLoading, onClick = { onSendCode(email) })
}

// Step 2: the code received and the new password
@Composable
private fun ResetStep(
    email: String,
    uiState: ForgotPasswordUiState,
    onReset: (code: String, newPassword: String, confirmPassword: String) -> Unit,
    onResendCode: () -> Unit,
    onChangeEmail: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Text(
        text = "Nouveau mot de passe",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Text(
        text = "Saisis le code envoyé à $email. Il est valable 15 minutes.",
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
        enabled = !uiState.isLoading,
        modifier = Modifier.fillMaxWidth(),
        // Digit-only keyboard
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    )

    OutlinedTextField(
        value = newPassword,
        onValueChange = { newPassword = it },
        label = { Text("Nouveau mot de passe") },
        singleLine = true,
        enabled = !uiState.isLoading,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        label = { Text("Confirme le mot de passe") },
        singleLine = true,
        enabled = !uiState.isLoading,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )

    Text(
        text = "8 caractères minimum, avec au moins un chiffre et un caractère spécial.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Messages(uiState = uiState)

    LoadingButton(
        label = "CHANGER LE MOT DE PASSE",
        isLoading = uiState.isLoading,
        onClick = { onReset(code, newPassword, confirmPassword) },
    )

    TextButton(onClick = onResendCode, enabled = !uiState.isLoading) {
        Text("Renvoyer le code")
    }

    TextButton(onClick = onChangeEmail, enabled = !uiState.isLoading) {
        Text("Changer d'adresse e-mail")
    }
}

// Shown once the password is changed
@Composable
private fun SuccessStep(onLoginClick: () -> Unit) {
    Text(
        text = "Ton mot de passe est changé",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Text(
        text = "Tu peux te connecter avec ton nouveau mot de passe.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Button(
        onClick = onLoginClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("SE CONNECTER")
    }
}

// Error in red, or good news in mint
@Composable
private fun Messages(uiState: ForgotPasswordUiState) {
    uiState.error?.let { message ->
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
    uiState.info?.let { message ->
        Text(text = message, color = MaterialTheme.colorScheme.tertiary)
    }
}

// Main button, with a small spinner while the call runs
@Composable
private fun LoadingButton(label: String, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(label)
        }
    }
}

@Preview
@Composable
private fun ForgotPasswordResetPreview() {
    MeloDropTheme {
        ForgotPasswordContent(
            uiState = ForgotPasswordUiState(
                email = "lito@example.com",
                info = "Si un compte utilise cette adresse, un code vient d'y être envoyé.",
            ),
            onSendCode = {},
            onReset = { _, _, _ -> },
            onResendCode = {},
            onChangeEmail = {},
            onLoginClick = {},
        )
    }
}
