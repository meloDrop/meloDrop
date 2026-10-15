package com.app.melodrop.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.melodrop.R
import com.app.melodrop.ui.components.MeloDropTitle
import com.app.melodrop.ui.theme.MeloDropTheme

// Connects the screen to its ViewModel
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            viewModel.onLoginHandled()
            onLoggedIn()
        }
    }

    LoginContent(
        uiState = uiState,
        onLoginClick = viewModel::login
    )
}

// Pure UI: only displays the state it receives
@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onLoginClick: (String, String) -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LoginBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_melodrop_logo),
                    contentDescription = "meloDrop",
                    modifier = Modifier.width(200.dp)
                )

                MeloDropTitle()

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("E-mail ou pseudo") },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )

                uiState.error?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = { onLoginClick(identifier, password) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("SE CONNECTER")
                    }
                }

                Text("Pas encore de compte ? Créer un compte")
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    MeloDropTheme {
        LoginContent(uiState = LoginUiState(), onLoginClick = { _, _ -> })
    }
}

@Composable
private fun LoginBackground() {
    Image(
        painter = painterResource(R.drawable.login_background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize(),
    )
}