package com.app.melodrop.ui.song

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.SongRepository
import com.app.melodrop.network.dto.SongDetailOut
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// What the poster of a song shows while it loads, once loaded, or after an error
data class SongPosterUiState(
    // Song asked for: the poster ignores the state of another song
    val songId: Int? = null,
    val isLoading: Boolean = false,
    val details: SongDetailOut? = null,
    val error: String? = null,
)

// Loads the poster of a song. One per screen: the songs already opened are kept
class SongPosterViewModel : ViewModel() {

    private val repository = SongRepository()

    var uiState by mutableStateOf(SongPosterUiState())
        private set

    // Songs already loaded on this screen: opened again, their poster shows at once
    private val loaded = mutableMapOf<Int, SongDetailOut>()

    // The loading in progress: cancelled when another poster opens
    private var loadJob: Job? = null

    // Called when a poster opens
    fun load(songId: Int) {
        val known = loaded[songId]
        if (known != null) {
            uiState = SongPosterUiState(songId = songId, details = known)
            return
        }

        loadJob?.cancel()
        uiState = SongPosterUiState(songId = songId, isLoading = true)

        loadJob = viewModelScope.launch {
            uiState = try {
                val details = repository.getSong(songId)
                loaded[songId] = details
                SongPosterUiState(songId = songId, details = details)
            } catch (e: HttpException) {
                SongPosterUiState(songId = songId, error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                SongPosterUiState(songId = songId, error = "Impossible de joindre le serveur.")
            }
        }
    }
}
