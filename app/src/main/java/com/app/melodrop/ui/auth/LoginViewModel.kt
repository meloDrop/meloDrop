package com.app.melodrop.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.TokenStore
import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.UserLogin
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Everything the login screen needs to display
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

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
                val token = RetrofitClient.api.login(UserLogin(identifier.trim(), password))
                TokenStore.save(getApplication(), token.accessToken)
                LoginUiState(isLoggedIn = true)
            } catch (e: HttpException) {
                val message = if (e.code() == 401) {
                    "Identifiant ou mot de passe incorrect."
                } else {
                    "Erreur du serveur (${e.code()})."
                }
                LoginUiState(error = message)
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
}