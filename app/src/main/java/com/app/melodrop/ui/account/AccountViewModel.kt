package com.app.melodrop.ui.account

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.AccountRepository
import com.app.melodrop.network.detail
import com.app.melodrop.network.dto.MeOut
import com.app.melodrop.ui.auth.newPasswordProblem
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Everything the "Mon compte" screen needs to display
data class AccountUiState(
    // True until the account is loaded
    val isLoading: Boolean = true,
    // Shown instead of the screen when the account could not be loaded
    val loadError: String? = null,
    val me: MeOut? = null,
    // True while a call is running: the buttons are disabled
    val isBusy: Boolean = false,
    // Which form is open
    val isEmailFormOpen: Boolean = false,
    val isPasswordFormOpen: Boolean = false,
    // Message under each section: an error in red, or good news in mint
    val emailError: String? = null,
    val emailInfo: String? = null,
    val passwordError: String? = null,
    val passwordInfo: String? = null,
    val deleteError: String? = null,
    // True once the account is deleted: the screen logs out
    val isDeleted: Boolean = false
)

class AccountViewModel : ViewModel() {

    private val repository = AccountRepository()

    var uiState by mutableStateOf(AccountUiState())
        private set

    init {
        // Loads the account as soon as the screen opens
        load()
    }

    fun load() {
        uiState = uiState.copy(isLoading = uiState.me == null, loadError = null)

        viewModelScope.launch {
            uiState = try {
                uiState.copy(isLoading = false, me = repository.getMe())
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, loadError = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isLoading = false, loadError = "Impossible de joindre le serveur.")
            }
        }
    }

    // ----- Email change, step 1: the new email and the password -----

    fun openEmailForm(open: Boolean) {
        uiState = uiState.copy(isEmailFormOpen = open, emailError = null, emailInfo = null)
    }

    fun requestEmailChange(newEmail: String, password: String) {
        // Same rule as the backend, checked before the call
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail.trim()).matches()) {
            uiState = uiState.copy(emailError = "L'adresse e-mail n'est pas valide.", emailInfo = null)
            return
        }
        if (password.isEmpty()) {
            uiState = uiState.copy(emailError = "Saisis ton mot de passe.", emailInfo = null)
            return
        }

        uiState = uiState.copy(isBusy = true, emailError = null, emailInfo = null)

        viewModelScope.launch {
            uiState = try {
                repository.changeEmail(password, newEmail.trim())
                // The account now has a pending email: the code field shows
                uiState.copy(
                    isBusy = false,
                    isEmailFormOpen = false,
                    me = repository.getMe(),
                    emailInfo = "Un code vient d'être envoyé à ${newEmail.trim()}.",
                )
            } catch (e: HttpException) {
                uiState.copy(isBusy = false, emailError = emailErrorMessage(e))
            } catch (e: IOException) {
                uiState.copy(isBusy = false, emailError = "Impossible de joindre le serveur.")
            }
        }
    }

    // ----- Email change, step 2: the code received at the new email -----

    fun confirmEmail(code: String) {
        if (code.length != 6 || !code.all { it.isDigit() }) {
            uiState = uiState.copy(emailError = "Le code contient 6 chiffres.", emailInfo = null)
            return
        }

        uiState = uiState.copy(isBusy = true, emailError = null, emailInfo = null)

        viewModelScope.launch {
            uiState = try {
                repository.confirmEmail(code)
                uiState.copy(
                    isBusy = false,
                    me = repository.getMe(),
                    emailInfo = "Ton adresse e-mail a été changée.",
                )
            } catch (e: HttpException) {
                // The pending email may have been cleared (409): reload the account
                val me = try {
                    repository.getMe()
                } catch (other: HttpException) {
                    uiState.me
                } catch (other: IOException) {
                    uiState.me
                }
                uiState.copy(isBusy = false, me = me, emailError = emailErrorMessage(e))
            } catch (e: IOException) {
                uiState.copy(isBusy = false, emailError = "Impossible de joindre le serveur.")
            }
        }
    }

    // ----- Password change -----

    fun openPasswordForm(open: Boolean) {
        uiState = uiState.copy(isPasswordFormOpen = open, passwordError = null, passwordInfo = null)
    }

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        // Same rules as the sign-up and the backend, checked before the call
        val problem = when {
            oldPassword.isEmpty() -> "Saisis ton mot de passe actuel."
            else -> newPasswordProblem(newPassword, confirmPassword)
        }
        if (problem != null) {
            uiState = uiState.copy(passwordError = problem, passwordInfo = null)
            return
        }

        uiState = uiState.copy(isBusy = true, passwordError = null, passwordInfo = null)

        viewModelScope.launch {
            uiState = try {
                repository.changePassword(oldPassword, newPassword)
                uiState.copy(
                    isBusy = false,
                    isPasswordFormOpen = false,
                    passwordInfo = "Ton mot de passe a été changé.",
                )
            } catch (e: HttpException) {
                uiState.copy(isBusy = false, passwordError = passwordErrorMessage(e))
            } catch (e: IOException) {
                uiState.copy(isBusy = false, passwordError = "Impossible de joindre le serveur.")
            }
        }
    }

    // ----- Account deletion, after the confirmation of the screen -----

    fun deleteAccount() {
        uiState = uiState.copy(isBusy = true, deleteError = null)

        viewModelScope.launch {
            uiState = try {
                repository.deleteAccount()
                uiState.copy(isBusy = false, isDeleted = true)
            } catch (e: HttpException) {
                uiState.copy(isBusy = false, deleteError = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isBusy = false, deleteError = "Impossible de joindre le serveur.")
            }
        }
    }
}

// Turns a backend error of the email change into a message for the user
private fun emailErrorMessage(e: HttpException): String = when (e.detail()) {
    "invalid_password" -> "Mot de passe incorrect."
    "same_email" -> "C'est déjà ton adresse actuelle."
    "email_taken" -> "Cette adresse e-mail est déjà utilisée."
    "invalid_code" -> "Le code est incorrect."
    "code_expired" -> "Le code a expiré. Demande un nouveau changement d'adresse."
    "too_many_attempts" -> "Trop d'essais. Demande un nouveau changement d'adresse."
    "no_pending_email" -> "Aucun changement d'adresse en attente."
    else -> if (e.code() == 422) "L'adresse e-mail n'est pas valide." else "Erreur du serveur (${e.code()})."
}

// Turns a backend error of the password change into a message for the user
private fun passwordErrorMessage(e: HttpException): String = when (e.detail()) {
    "invalid_password" -> "Le mot de passe actuel est incorrect."
    "password_missing_digit" -> "Le mot de passe doit contenir au moins un chiffre."
    "password_missing_special_char" -> "Le mot de passe doit contenir au moins un caractère spécial."
    else -> "Erreur du serveur (${e.code()})."
}
