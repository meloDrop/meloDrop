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

// Everything the Drop screen needs to display
data class DropUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<TrackResult> = emptyList(),
    val searchError: String? = null,
    // Track loaded in the player, null if none
    val playingTrackId: Long? = null,
    // True when the preview plays (or is about to): pause icon on its button
    val isPlaying: Boolean = false
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

    // Leaving the tab or the app: the music must stop
    fun pausePreview() {
        player.pause()
    }

    private fun stopPreview() {
        player.pause()
        player.clearMediaItems()
        uiState = uiState.copy(playingTrackId = null)
    }

    // The ViewModel is destroyed (logout): free the player
    override fun onCleared() {
        player.release()
    }
}