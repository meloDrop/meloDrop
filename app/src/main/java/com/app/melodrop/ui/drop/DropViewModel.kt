package com.app.melodrop.ui.drop

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
import com.app.melodrop.network.dto.DropOut
import com.app.melodrop.network.dto.GenreOut
import com.app.melodrop.network.dto.TrackResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Waiting time after the last key before searching (ms)
private const val SEARCH_DELAY_MS = 400L

// Fewer letters than this: no search
private const val MIN_QUERY_LENGTH = 2

// Most genres for one drop (public: the screen shows it in its text)
const val MAX_GENRES = 3

// Everything the Drop screen needs to display
data class DropUiState(
    // Step 1: search
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<TrackResult> = emptyList(),
    val searchError: String? = null,
    // Preview player: track loaded, and play or pause icon
    val playingTrackId: Long? = null,
    val isPlaying: Boolean = false,
    // Step 2: genres of the chosen track
    val selectedTrack: TrackResult? = null,
    val genres: List<GenreOut> = emptyList(),
    val selectedGenreIds: Set<Int> = emptySet(),
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    // Step 3: the drop is online
    val createdDrop: DropOut? = null
)

class DropViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DropRepository()

    var uiState by mutableStateOf(DropUiState())
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

            // Link expired or file unreadable: just stop
            override fun onPlayerError(error: PlaybackException) {
                player.pause()
            }
        })
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
                val results = repository.searchTracks(query.trim())
                uiState.copy(isSearching = false, results = results)
            } catch (e: HttpException) {
                // 502: Deezer did not answer
                uiState.copy(isSearching = false, searchError = "Deezer ne répond pas. Réessaie dans un instant.")
            } catch (e: IOException) {
                uiState.copy(isSearching = false, searchError = "Impossible de joindre le serveur.")
            }
        }
    }

    // A result was tapped: go to the genre step (the music keeps playing)
    fun selectTrack(track: TrackResult) {
        uiState = uiState.copy(
            selectedTrack = track,
            selectedGenreIds = emptySet(),
            submitError = null,
        )
        // The list never changes: loaded once, the first time it is needed
        if (uiState.genres.isEmpty()) {
            loadGenres()
        }
    }

    // "Changer de titre" or back button: back to the results
    fun clearSelection() {
        uiState = uiState.copy(selectedTrack = null, submitError = null)
    }

    // Tap on a genre: selects it, or unselects it
    fun toggleGenre(genreId: Int) {
        val ids = uiState.selectedGenreIds
        uiState = when {
            genreId in ids -> uiState.copy(selectedGenreIds = ids - genreId)
            ids.size < MAX_GENRES -> uiState.copy(selectedGenreIds = ids + genreId)
            // Already at the limit: nothing happens
            else -> uiState
        }
    }

    fun submitDrop() {
        val track = uiState.selectedTrack ?: return
        uiState = uiState.copy(isSubmitting = true, submitError = null)

        viewModelScope.launch {
            uiState = try {
                val drop = repository.createDrop(track.deezerTrackId, uiState.selectedGenreIds.toList())
                // Online: the music stops, the screen shows the confirmation
                stopPreview()
                uiState.copy(isSubmitting = false, selectedTrack = null, createdDrop = drop)
            } catch (e: HttpException) {
                uiState.copy(isSubmitting = false, submitError = submitErrorMessage(e))
            } catch (e: IOException) {
                uiState.copy(isSubmitting = false, submitError = "Impossible de joindre le serveur.")
            }
        }
    }

    // Play button of a track: plays its preview, or pauses it if it is the one loaded
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

    // Leaving the tab or the app: the music must stop
    fun pausePreview() {
        player.pause()
    }

    private fun stopPreview() {
        player.pause()
        player.clearMediaItems()
        uiState = uiState.copy(playingTrackId = null)
    }

    private fun loadGenres() {
        viewModelScope.launch {
            try {
                val genres = repository.getGenres()
                uiState = uiState.copy(genres = genres)
            } catch (e: HttpException) {
                uiState = uiState.copy(submitError = "Les genres n'ont pas pu se charger.")
            } catch (e: IOException) {
                uiState = uiState.copy(submitError = "Impossible de joindre le serveur.")
            }
        }
    }

    // The ViewModel is destroyed (logout): free the player
    override fun onCleared() {
        player.release()
    }
}

// Turns a backend error into a message for the user
private fun submitErrorMessage(e: HttpException): String = when (e.code()) {
    409 -> "Tu as déjà un drop en ligne aujourd'hui. Reviens quand il aura expiré."
    404 -> "Ce titre n'est plus disponible sur Deezer. Choisis-en un autre."
    502 -> "Deezer ne répond pas. Réessaie dans un instant."
    else -> "Erreur du serveur (${e.code()})."
}