package com.app.melodrop.ui.mypicks

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
import com.app.melodrop.data.ProfileRepository
import com.app.melodrop.network.dto.LikedSongOut
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Everything the "Mes picks" screen needs to display
data class MyPicksUiState(
    // True until the first answer: nothing to show yet
    val isLoading: Boolean = true,
    // Shown instead of the grid when the list could not be loaded
    val error: String? = null,
    // All the user's picks, the most recent like first
    val picks: List<LikedSongOut> = emptyList(),
    // Pick whose preview is loaded in the player (its song id), null if none
    val playingPickId: Int? = null,
    // True when the preview plays: pause icon on its cover
    val isPlaying: Boolean = false,
    // Short message above the grid when a preview cannot be played
    val previewError: String? = null
)

class MyPicksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository()

    var uiState by mutableStateOf(MyPicksUiState())
        private set

    // Plays the previews, one at a time
    private val player = buildPreviewPlayer(application)

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
                uiState = uiState.copy(playingPickId = null, previewError = "Extrait indisponible pour ce titre.")
            }
        })

        // Loads the list as soon as the screen opens
        load()
    }

    fun load() {
        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            uiState = try {
                uiState.copy(isLoading = false, picks = repository.getMyPicks())
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }
    }

    // Tap on a cover: plays its preview, or pauses it if it is the one loaded
    fun togglePreview(pick: LikedSongOut) {
        if (pick.songId == uiState.playingPickId) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }

        // Another pick: the current preview stops first
        player.pause()
        player.clearMediaItems()
        uiState = uiState.copy(playingPickId = pick.songId, previewError = null)

        viewModelScope.launch {
            // try used as a value: the link, or null if the call failed
            val url = try {
                repository.getPreviewUrl(pick.songId)
            } catch (e: HttpException) {
                null
            } catch (e: IOException) {
                null
            }

            // The user tapped another cover while we were waiting: too late for this one
            if (uiState.playingPickId != pick.songId) return@launch

            if (url == null) {
                uiState = uiState.copy(playingPickId = null, previewError = "Extrait indisponible pour ce titre.")
                return@launch
            }

            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

    // Leaving the screen or the app: the music must stop
    fun pausePreview() {
        player.pause()
    }

    // The ViewModel is destroyed (the screen is closed): free the player
    override fun onCleared() {
        player.release()
    }
}
