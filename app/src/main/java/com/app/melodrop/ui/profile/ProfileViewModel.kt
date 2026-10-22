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

// Everything the Profile screen needs to display
data class ProfileUiState(
    // True until the first answer: nothing to show yet
    val isLoading: Boolean = true,
    // Shown only when there is no profile to display
    val error: String? = null,
    val profile: ProfileOut? = null
)

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    var uiState by mutableStateOf(ProfileUiState())
        private set

    // Called each time the tab opens, so the counters and the drops stay fresh
    fun load() {
        // The old profile stays on screen while the new one arrives
        uiState = uiState.copy(isLoading = uiState.profile == null, error = null)

        viewModelScope.launch {
            uiState = try {
                ProfileUiState(isLoading = false, profile = repository.getMyProfile())
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                // Backend down, no network, or adb reverse missing
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }
    }
}
