package com.app.melodrop.ui.auth

import android.app.Application
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.AuthRepository
import com.app.melodrop.network.detail
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Everything the register screen needs to display
data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistered: Boolean = false,
    // Email of the new account, to fill in the verify screen
    val email: String = ""
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        // Same rules as the backend, checked before the call
        val error = checkFields(username.trim(), email.trim(), password, confirmPassword)
        if (error != null) {
            uiState = uiState.copy(error = error)
            return
        }

        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            uiState = try {
                repository.register(email.trim(), username.trim(), password)
                RegisterUiState(isRegistered = true, email = email.trim())
            } catch (e: HttpException) {
                RegisterUiState(error = errorMessage(e))
            } catch (e: IOException) {
                RegisterUiState(error = "Impossible de joindre le serveur.")
            }
        }
    }

    // Called once the screen has reacted to the successful sign-up
    fun onRegisterHandled() {
        uiState = RegisterUiState()
    }
}

// Returns the first problem in the fields, or null if they are valid
private fun checkFields(
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
): String? = when {
    username.length !in 3..30 -> "Le pseudo doit faire entre 3 et 30 caractères."
    '@' in username -> "Le pseudo ne peut pas contenir de @."
    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "L'adresse e-mail n'est pas valide."
    password.length !in 8..128 -> "Le mot de passe doit faire entre 8 et 128 caractères."
    password.none { it.isDigit() } -> "Le mot de passe doit contenir au moins un chiffre."
    password.none { !it.isLetterOrDigit() && it != '_' && !it.isWhitespace() } ->
        "Le mot de passe doit contenir au moins un caractère spécial."
    password != confirmPassword -> "Les deux mots de passe ne correspondent pas."
    else -> null
}

// Turns a backend error into a message for the user
private fun errorMessage(e: HttpException): String = when (e.detail()) {
    "email_taken" -> "Cette adresse e-mail est déjà utilisée."
    "username_taken" -> "Ce pseudo est déjà pris."
    "password_missing_digit" -> "Le mot de passe doit contenir au moins un chiffre."
    "password_missing_special_char" -> "Le mot de passe doit contenir au moins un caractère spécial."
    else -> "Erreur du serveur (${e.code()})."
}