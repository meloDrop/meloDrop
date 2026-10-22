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

// Everything the profile of another user needs to display
data class UserProfileUiState(
    // True until the first answer: nothing to show yet
    val isLoading: Boolean = true,
    // Shown when the profile could not be loaded
    val error: String? = null,
    val profile: ProfileOut? = null
)

class UserProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    var uiState by mutableStateOf(UserProfileUiState())
        private set

    // Called when the screen opens, with the id taken from the route
    fun load(userId: Int) {
        uiState = uiState.copy(isLoading = uiState.profile == null, error = null)

        viewModelScope.launch {
            uiState = try {
                UserProfileUiState(isLoading = false, profile = repository.getProfile(userId))
            } catch (e: HttpException) {
                // 404: the account was deleted since the drop was seen
                val message = if (e.code() == 404) "Ce profil n'existe plus."
                else "Erreur du serveur (${e.code()})."
                uiState.copy(isLoading = false, error = message)
            } catch (e: IOException) {
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }
    }
}
