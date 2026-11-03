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

// Everything the forgotten password screen needs to display
data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    // Email the code was sent to: null on step 1, filled on step 2
    val email: String? = null,
    val error: String? = null,
    // Short message after "resend the code"
    val info: String? = null,
    // True once the password is changed: the screen shows the success
    val isReset: Boolean = false
)

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    var uiState by mutableStateOf(ForgotPasswordUiState())
        private set

    // Step 1: sends a code to the email
    fun sendCode(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            uiState = uiState.copy(error = "L'adresse e-mail n'est pas valide.", info = null)
            return
        }

        uiState = uiState.copy(isLoading = true, error = null, info = null)

        viewModelScope.launch {
            uiState = try {
                repository.forgotPassword(email.trim())
                // The backend answers the same way for any email: we go to step 2
                // without telling if an account exists (no account guessing)
                uiState.copy(
                    isLoading = false,
                    email = email.trim(),
                    info = "Si un compte utilise cette adresse, un code vient d'y être envoyé.",
                )
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }
    }

    // Step 2: the code and the new password
    fun resetPassword(code: String, newPassword: String, confirmPassword: String) {
        val email = uiState.email ?: return

        val problem = when {
            code.length != 6 || !code.all { it.isDigit() } -> "Le code contient 6 chiffres."
            else -> newPasswordProblem(newPassword, confirmPassword)
        }
        if (problem != null) {
            uiState = uiState.copy(error = problem, info = null)
            return
        }

        uiState = uiState.copy(isLoading = true, error = null, info = null)

        viewModelScope.launch {
            uiState = try {
                repository.resetPassword(email, code, newPassword)
                ForgotPasswordUiState(isReset = true)
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, error = resetErrorMessage(e))
            } catch (e: IOException) {
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }
    }

    // "Renvoyer le code" on step 2: same call as step 1
    fun resendCode() {
        val email = uiState.email ?: return
        sendCode(email)
    }

    // "Changer d'adresse" on step 2: back to step 1
    fun changeEmail() {
        uiState = ForgotPasswordUiState()
    }
}

// Turns a backend error of the reset into a message for the user
private fun resetErrorMessage(e: HttpException): String = when (e.detail()) {
    "invalid_code" -> "Le code est incorrect."
    "code_expired" -> "Le code a expiré. Demande un nouveau code."
    "too_many_attempts" -> "Trop d'essais. Demande un nouveau code."
    "password_missing_digit" -> "Le mot de passe doit contenir au moins un chiffre."
    "password_missing_special_char" -> "Le mot de passe doit contenir au moins un caractère spécial."
    else -> "Erreur du serveur (${e.code()})."
}
