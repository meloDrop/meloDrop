package com.app.melodrop.ui.labels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.app.melodrop.audio.buildPreviewPlayer
import com.app.melodrop.data.DropRepository
import com.app.melodrop.data.ProfileRepository
import com.app.melodrop.network.dto.LabeledSongOut
import com.app.melodrop.network.dto.TrackResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Waiting time after the last key before searching (ms), like on the Drop screen
private const val SEARCH_DELAY_MS = 400L

// Fewer letters than this: no search
private const val MIN_QUERY_LENGTH = 2

// Everything the "Mes chansons" edit screen needs to display
data class LabelsEditUiState(
    // True until the saved songs arrive
    val isLoading: Boolean = true,
    // Shown instead of the labels when they could not be loaded
    val loadError: String? = null,
    // The song of each label, by label code: a label missing here is empty
    val songs: Map<String, LabeledSongOut> = emptyMap(),
    // Label being changed (its code): the search shows only then
    val editingLabel: String? = null,
    // Search
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<TrackResult> = emptyList(),
    val searchError: String? = null,
    // Preview of a search result: track loaded, and play or pause icon
    val playingTrackId: Long? = null,
    val isPlaying: Boolean = false,
    // Saving: each change is saved right away
    val isSaving: Boolean = false,
    val saveError: String? = null
)

class LabelsEditViewModel(application: Application) : AndroidViewModel(application) {

    private val profileRepository = ProfileRepository()

    // Same search as the Drop screen
    private val dropRepository = DropRepository()

    var uiState by mutableStateOf(LabelsEditUiState())
        private set

    private val player = buildPreviewPlayer(application)

    // The search waiting to run: cancelled when the user types again
    private var searchJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            // Play or pause asked: update the icon
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                uiState = uiState.copy(isPlaying = playWhenReady)
            }

            // End of the 30 seconds: back to the start, play icon again
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    player.pause()
                    player.seekTo(0)
                }
            }

            // Link expired or file unreadable: stop and tell the user
            override fun onPlayerError(error: PlaybackException) {
                player.pause()
                uiState = uiState.copy(playingTrackId = null, searchError = "Extrait indisponible pour ce titre.")
            }
        })
    }

    // Called when the screen opens: the saved songs come with the profile (ADR-0018)
    fun load() {
        uiState = uiState.copy(isLoading = true, loadError = null)

        viewModelScope.launch {
            uiState = try {
                val profile = profileRepository.getMyProfile()
                uiState.copy(isLoading = false, songs = profile.labeledSongs.associateBy { it.label })
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, loadError = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isLoading = false, loadError = "Impossible de joindre le serveur.")
            }
        }
    }

    // Tap on a label: it becomes the one to change. A second tap on it (or back) closes the search
    fun selectLabel(code: String) {
        stopPreview()
        searchJob?.cancel()
        uiState = uiState.copy(
            editingLabel = if (uiState.editingLabel == code) null else code,
            query = "",
            results = emptyList(),
            isSearching = false,
            searchError = null,
            saveError = null,
        )
    }

    // Called on every key typed in the search field
    fun onQueryChange(query: String) {
        uiState = uiState.copy(query = query)
        // A new search is coming: the old one and its music stop
        searchJob?.cancel()
        stopPreview()

        if (query.trim().length < MIN_QUERY_LENGTH) {
            uiState = uiState.copy(results = emptyList(), isSearching = false, searchError = null)
            return
        }

        searchJob = viewModelScope.launch {
            // Waits for the user to stop typing (debounce)
            delay(SEARCH_DELAY_MS)
            uiState = uiState.copy(isSearching = true, searchError = null)

            uiState = try {
                val results = dropRepository.searchTracks(query.trim())
                uiState.copy(isSearching = false, results = results)
            } catch (e: HttpException) {
                // 502: Deezer did not answer
                uiState.copy(isSearching = false, searchError = "Deezer ne répond pas. Réessaie dans un instant.")
            } catch (e: IOException) {
                uiState.copy(isSearching = false, searchError = "Impossible de joindre le serveur.")
            }
        }
    }

    // A result was tapped: it goes under the label being changed, saved right away
    fun chooseTrack(track: TrackResult) {
        val label = uiState.editingLabel ?: return
        stopPreview()
        uiState = uiState.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            uiState = try {
                val songs = profileRepository.setLabeledSong(label, track.deezerTrackId)
                // The answer is all the songs as saved: the screen shows them
                uiState.copy(
                    isSaving = false,
                    songs = songs.associateBy { it.label },
                    editingLabel = null,
                    query = "",
                    results = emptyList(),
                )
            } catch (e: HttpException) {
                uiState.copy(isSaving = false, saveError = saveErrorMessage(e))
            } catch (e: IOException) {
                uiState.copy(isSaving = false, saveError = "Impossible de joindre le serveur.")
            }
        }
    }

    // Removes the song of a label
    fun removeLabel(code: String) {
        uiState = uiState.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            uiState = try {
                profileRepository.removeLabeledSong(code)
                uiState.copy(isSaving = false, songs = uiState.songs - code)
            } catch (e: HttpException) {
                // 404: already empty (removed from another phone), the screen just catches up
                if (e.code() == 404) uiState.copy(isSaving = false, songs = uiState.songs - code)
                else uiState.copy(isSaving = false, saveError = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isSaving = false, saveError = "Impossible de joindre le serveur.")
            }
        }
    }

    // Play button of a result: plays its preview, or pauses it if it is the one loaded
    fun togglePreview(track: TrackResult) {
        if (track.deezerTrackId == uiState.playingTrackId) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }

        val url = track.previewUrl ?: return
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
        uiState = uiState.copy(playingTrackId = track.deezerTrackId)
    }

    // Leaving the screen or the app: the music must stop
    fun pausePreview() {
        player.pause()
    }

    private fun stopPreview() {
        player.pause()
        player.clearMediaItems()
        uiState = uiState.copy(playingTrackId = null)
    }

    // The ViewModel is destroyed (the screen is closed): free the player
    override fun onCleared() {
        player.release()
    }
}

// Turns a backend error into a message for the user
private fun saveErrorMessage(e: HttpException): String = when (e.code()) {
    404 -> "Ce titre n'est plus disponible sur Deezer. Choisis-en un autre."
    502 -> "Deezer ne répond pas. Réessaie dans un instant."
    else -> "Erreur du serveur (${e.code()})."
}
