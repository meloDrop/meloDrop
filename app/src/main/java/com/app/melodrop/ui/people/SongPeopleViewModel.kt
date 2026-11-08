package com.app.melodrop.ui.people

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.DiscoverRepository
import com.app.melodrop.network.dto.UserBriefOut
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// The likes of a Discover card while they load, once loaded, or after an error
data class SongPeopleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    // Who liked the song on its drops online, the most recent first
    val likers: List<UserBriefOut> = emptyList(),
)

// Loads who liked the song of a Discover card. The droppers come with the card
class SongPeopleViewModel : ViewModel() {

    private val repository = DiscoverRepository()

    var uiState by mutableStateOf(SongPeopleUiState())
        private set

    // The loading in progress: cancelled when the list of another card opens
    private var loadJob: Job? = null

    // Called each time the sheet opens: the list is always fresh
    fun load(songId: Int) {
        loadJob?.cancel()
        uiState = SongPeopleUiState(isLoading = true)

        loadJob = viewModelScope.launch {
            uiState = try {
                SongPeopleUiState(likers = repository.getSongLikers(songId))
            } catch (e: HttpException) {
                SongPeopleUiState(error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                SongPeopleUiState(error = "Impossible de joindre le serveur.")
            }
        }
    }
}
