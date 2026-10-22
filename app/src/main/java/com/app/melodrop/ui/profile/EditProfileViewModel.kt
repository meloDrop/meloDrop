package com.app.melodrop.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.ProfileRepository
import com.app.melodrop.network.dto.ProfileOut
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Most characters in a display name, like the backend (ProfileUpdate)
const val DISPLAY_NAME_MAX = 30

// Everything the edit screen needs to display
data class EditProfileUiState(
    // True until the current profile is loaded
    val isLoading: Boolean = true,
    // The current profile: avatar and @username for the preview
    val profile: ProfileOut? = null,
    // Text typed in the field
    val displayName: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    // True once saved: the screen goes back to the profile
    val isSaved: Boolean = false
)

class EditProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

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
                // Empty field when no name was chosen: the @username shows instead
                EditProfileUiState(
                    isLoading = false,
                    profile = profile,
                    displayName = profile.displayName ?: "",
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

    fun save() {
        uiState = uiState.copy(isSaving = true, error = null)

        viewModelScope.launch {
            uiState = try {
                // The backend removes the spaces around the name, and an empty name
                // gives back the @username (ADR-0019)
                val profile = repository.updateProfile(uiState.displayName)
                uiState.copy(isSaving = false, profile = profile, isSaved = true)
            } catch (e: HttpException) {
                uiState.copy(isSaving = false, error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isSaving = false, error = "Impossible de joindre le serveur.")
            }
        }
    }
}
