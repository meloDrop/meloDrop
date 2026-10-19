package com.app.melodrop.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Everything the login screen needs to display
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    // The account exists, but its email is not confirmed yet (403)
    val needsVerification: Boolean = false,
    // Known only if the user typed an email to log in
    val email: String = ""
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun login(identifier: String, password: String) {
        // Avoid a useless call to the backend
        if (identifier.isBlank() || password.isBlank()) {
            uiState = uiState.copy(error = "Remplis tous les champs.")
            return
        }

        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            uiState = try {
                repository.login(identifier.trim(), password)
                LoginUiState(isLoggedIn = true)
            } catch (e: HttpException) {
                when (e.code()) {
                    401 -> LoginUiState(error = "Identifiant ou mot de passe incorrect.")
                    // Email not confirmed: the screen opens the code screen
                    403 -> LoginUiState(
                        needsVerification = true,
                        email = if ('@' in identifier) identifier.trim() else "",
                    )
                    else -> LoginUiState(error = "Erreur du serveur (${e.code()}).")
                }
            } catch (e: IOException) {
                // Backend down, no network, or adb reverse missing
                LoginUiState(error = "Impossible de joindre le serveur.")
            }
        }
    }

    // Called once the screen has reacted to the successful login
    fun onLoginHandled() {
        uiState = LoginUiState()
    }

    // Called once the screen has opened the code screen
    fun onVerificationHandled() {
        uiState = LoginUiState()
    }
}