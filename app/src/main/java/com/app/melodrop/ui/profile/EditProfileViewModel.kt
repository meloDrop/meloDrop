package com.app.melodrop.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.AccountRepository
import com.app.melodrop.data.ProfileRepository
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.ProfileUpdate
import com.app.melodrop.ui.components.isPremiumAura
import com.app.melodrop.ui.components.isPremiumDrop
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Most characters in a display name, like the backend (ProfileUpdate)
const val DISPLAY_NAME_MAX = 30

// Message shown when a locked cosmetic is tapped, or refused by the backend
private const val PREMIUM_MESSAGE = "Cette option arrive avec l'abonnement meloDrop."

// Everything the edit screen needs to display
data class EditProfileUiState(
    // True until the current profile is loaded
    val isLoading: Boolean = true,
    // The current profile: avatar and @username for the preview
    val profile: ProfileOut? = null,
    // Text typed in the field
    val displayName: String = "",
    // Choices of the avatar: the preview shows them before saving
    val avatarColor: String = "lavender",
    val avatarStyle: String = "simple",
    val avatarDrop: String = "night",
    val avatarDeco: String = "plain",
    val avatarFace: String = "none",
    val avatarAura: String = "none",
    // False when the premium drops and the auras are locked for this account
    val premiumUnlocked: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    // True once saved: the screen goes back to the profile
    val isSaved: Boolean = false
) {
    // A premium drop is locked, except the one already saved (chosen before the lock)
    fun isDropLocked(name: String): Boolean =
        !premiumUnlocked && isPremiumDrop(name) && name != profile?.avatarDrop

    // Same rule for the auras
    fun isAuraLocked(name: String): Boolean =
        !premiumUnlocked && isPremiumAura(name) && name != profile?.avatarAura
}

class EditProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()
    private val accountRepository = AccountRepository()

    var uiState by mutableStateOf(EditProfileUiState())
        private set

    init {
        // Fills the field as soon as the screen opens
        load()
    }

    fun load() {
        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            uiState = try {
                val profile = repository.getMyProfile()
                // Tells whether the premium cosmetics are locked for this account
                val me = accountRepository.getMe()
                // Empty field when no name was chosen: the @username shows instead
                EditProfileUiState(
                    isLoading = false,
                    profile = profile,
                    displayName = profile.displayName ?: "",
                    avatarColor = profile.avatarColor,
                    avatarStyle = profile.avatarStyle,
                    avatarDrop = profile.avatarDrop ?: "night",
                    avatarDeco = profile.avatarDeco ?: "plain",
                    avatarFace = profile.avatarFace ?: "none",
                    avatarAura = profile.avatarAura ?: "none",
                    // An older backend does not say it: nothing is locked
                    premiumUnlocked = me.premiumUnlocked ?: true,
                )
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }
    }

    // Called on every key typed in the field
    fun onDisplayNameChange(value: String) {
        // Same limit as the backend: the field stops at 30 characters
        if (value.length <= DISPLAY_NAME_MAX) {
            uiState = uiState.copy(displayName = value, error = null)
        }
    }

    // Called when the user taps a color
    fun onAvatarColorChange(name: String) {
        uiState = uiState.copy(avatarColor = name, error = null)
    }

    // Called when the user taps a drop style
    fun onAvatarStyleChange(name: String) {
        uiState = uiState.copy(avatarStyle = name, error = null)
    }

    // Called when the user taps a drop color. A locked one only shows the message
    fun onAvatarDropChange(name: String) {
        uiState = if (uiState.isDropLocked(name)) {
            uiState.copy(error = PREMIUM_MESSAGE)
        } else {
            uiState.copy(avatarDrop = name, error = null)
        }
    }

    // Called when the user taps a background decoration
    fun onAvatarDecoChange(name: String) {
        uiState = uiState.copy(avatarDeco = name, error = null)
    }

    // Called when the user taps a face
    fun onAvatarFaceChange(name: String) {
        uiState = uiState.copy(avatarFace = name, error = null)
    }

    // Called when the user taps an aura. A locked one only shows the message
    fun onAvatarAuraChange(name: String) {
        uiState = if (uiState.isAuraLocked(name)) {
            uiState.copy(error = PREMIUM_MESSAGE)
        } else {
            uiState.copy(avatarAura = name, error = null)
        }
    }

    fun save() {
        uiState = uiState.copy(isSaving = true, error = null)

        viewModelScope.launch {
            uiState = try {
                // The backend removes the spaces around the name, and an empty name
                // gives back the @username (ADR-0019)
                val profile = repository.updateProfile(
                    ProfileUpdate(
                        displayName = uiState.displayName,
                        avatarColor = uiState.avatarColor,
                        avatarStyle = uiState.avatarStyle,
                        avatarDrop = uiState.avatarDrop,
                        avatarDeco = uiState.avatarDeco,
                        avatarFace = uiState.avatarFace,
                        avatarAura = uiState.avatarAura,
                    )
                )
                uiState.copy(isSaving = false, profile = profile, isSaved = true)
            } catch (e: HttpException) {
                // 403: a premium cosmetic, and the cosmetics got locked meanwhile
                val message = if (e.code() == 403) PREMIUM_MESSAGE else "Erreur du serveur (${e.code()})."
                uiState.copy(isSaving = false, error = message)
            } catch (e: IOException) {
                uiState.copy(isSaving = false, error = "Impossible de joindre le serveur.")
            }
        }
    }
}
