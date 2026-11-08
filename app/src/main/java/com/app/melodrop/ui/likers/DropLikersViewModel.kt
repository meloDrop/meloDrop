package com.app.melodrop.ui.likers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.DropRepository
import com.app.melodrop.network.dto.DropLikerOut
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// What the list of likes shows while it loads, once loaded, or after an error
data class DropLikersUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    // Everyone who liked the drop, picks included, the most recent first
    val likers: List<DropLikerOut> = emptyList(),
)

// Loads who liked and who picked one of the user's drops
class DropLikersViewModel : ViewModel() {

    private val repository = DropRepository()

    var uiState by mutableStateOf(DropLikersUiState())
        private set

    // The loading in progress: cancelled when the list of another drop opens
    private var loadJob: Job? = null

    // Called each time the sheet opens: the list is always fresh
    fun load(dropId: Int) {
        loadJob?.cancel()
        uiState = DropLikersUiState(isLoading = true)

        loadJob = viewModelScope.launch {
            uiState = try {
                DropLikersUiState(likers = repository.getLikers(dropId))
            } catch (e: HttpException) {
                DropLikersUiState(error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                DropLikersUiState(error = "Impossible de joindre le serveur.")
            }
        }
    }
}
