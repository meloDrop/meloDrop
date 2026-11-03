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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun VerifyEmailScreen(
    initialEmail: String,
    onLoginClick: () -> Unit,
    viewModel: VerifyEmailViewModel = viewModel()
) {
    // No LaunchedEffect: the screen shows its own success state,
    // then the user taps "Se connecter"
    VerifyEmailContent(
        uiState = viewModel.uiState,
        initialEmail = initialEmail,
        onVerifyClick = viewModel::verify,
        onResendClick = viewModel::resendCode,
        onLoginClick = onLoginClick,
    )
}

// Pure UI: only displays the state it receives
@Composable
private fun VerifyEmailContent(
    uiState: VerifyEmailUiState,
    initialEmail: String,
    onVerifyClick: (String, String) -> Unit,
    onResendClick: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    // Prefilled only on first display, the user can still change it
    var email by remember { mutableStateOf(initialEmail) }
    var code by remember { mutableStateOf("") }

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
            if (uiState.isVerified) {
                // Success: the account can now log in
                Text(
                    text = "Ton e-mail est confirmé",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = "Ton compte est prêt. Connecte-toi pour déposer ton premier titre.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("SE CONNECTER")
                }
            } else {
                // Form: email + 6-digit code
                Text(
                    text = "Vérifie ton e-mail",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = "Code à 6 chiffres envoyé. Il est valable 15 minutes. Pense à vérifier tes spams !",
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

                uiState.error?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                // Good news, so not in the error color
                uiState.info?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }

                Button(
                    onClick = { onVerifyClick(email, code) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("VALIDER")
                    }
                }

                TextButton(
                    onClick = { onResendClick(email) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Renvoyer le code")
                }

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

@Preview
@Composable
fun VerifyEmailScreenPreview() {
    MeloDropTheme {
        VerifyEmailContent(
            uiState = VerifyEmailUiState(),
            initialEmail = "lito@melodrop.app",
            onVerifyClick = { _, _ -> },
            onResendClick = {},
            onLoginClick = {},
        )
    }
}

@Preview
@Composable
fun VerifyEmailSuccessPreview() {
    MeloDropTheme {
        VerifyEmailContent(
            uiState = VerifyEmailUiState(isVerified = true),
            initialEmail = "lito@melodrop.app",
            onVerifyClick = { _, _ -> },
            onResendClick = {},
            onLoginClick = {},
        )
    }
}