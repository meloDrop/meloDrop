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

// Everything the verify screen needs to display
data class VerifyEmailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    // Short message after "resend the code"
    val info: String? = null,
    val isVerified: Boolean = false
)

class VerifyEmailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    var uiState by mutableStateOf(VerifyEmailUiState())
        private set

    fun verify(email: String, code: String) {
        // Same rules as the backend, checked before the call
        val error = checkCodeFields(email.trim(), code.trim())
        if (error != null) {
            uiState = uiState.copy(error = error, info = null)
            return
        }

        uiState = uiState.copy(isLoading = true, error = null, info = null)

        viewModelScope.launch {
            uiState = try {
                repository.verifyEmail(email.trim(), code.trim())
                VerifyEmailUiState(isVerified = true)
            } catch (e: HttpException) {
                VerifyEmailUiState(error = codeErrorMessage(e))
            } catch (e: IOException) {
                VerifyEmailUiState(error = "Impossible de joindre le serveur.")
            }
        }
    }

    fun resendCode(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            uiState = uiState.copy(error = "L'adresse e-mail n'est pas valide.", info = null)
            return
        }

        uiState = uiState.copy(isLoading = true, error = null, info = null)

        viewModelScope.launch {
            uiState = try {
                repository.resendCode(email.trim())
                // The backend answers the same way for any email
                VerifyEmailUiState(info = "Si cette adresse attend une confirmation, un nouveau code vient d'être envoyé.")
            } catch (e: HttpException) {
                VerifyEmailUiState(error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                VerifyEmailUiState(error = "Impossible de joindre le serveur.")
            }
        }
    }
}

// Returns the first problem in the fields, or null if they are valid
private fun checkCodeFields(email: String, code: String): String? = when {
    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "L'adresse e-mail n'est pas valide."
    code.length != 6 || !code.all { it.isDigit() } -> "Le code contient 6 chiffres."
    else -> null
}

// Turns a backend error into a message for the user
private fun codeErrorMessage(e: HttpException): String = when (e.detail()) {
    "invalid_code" -> "Ce code n'est pas le bon."
    "code_expired" -> "Ce code a expiré. Demande un nouveau code."
    "too_many_attempts" -> "Trop d'essais. Demande un nouveau code."
    else -> "Erreur du serveur (${e.code()})."
}