package com.app.melodrop.ui.skips

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
import com.app.melodrop.data.SkipsRepository
import com.app.melodrop.network.dto.SkippedSongOut
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Everything the "Mes skips" screen needs to display
data class SkipsUiState(
    // True until the first answer: nothing to show yet
    val isLoading: Boolean = true,
    // Shown instead of the list when it could not be loaded
    val error: String? = null,
    val skips: List<SkippedSongOut> = emptyList(),
    // Song loaded in the player, null if none
    val playingSongId: Int? = null,
    // True when the preview plays: pause icon on its button
    val isPlaying: Boolean = false,
    // Short message above the list: what happened to a rescued song, or a problem
    val message: String? = null,
    // True when the message is a problem: shown in red
    val isErrorMessage: Boolean = false
)

class SkipsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SkipsRepository()

    var uiState by mutableStateOf(SkipsUiState())
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
                showError("Extrait indisponible pour ce titre.")
            }
        })

        // Loads the list as soon as the screen opens
        load()
    }

    fun load() {
        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            uiState = try {
                uiState.copy(isLoading = false, skips = repository.getSkips())
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }
    }

    // Play button of a song: plays its preview, or pauses it if it is the one loaded
    fun togglePreview(song: SkippedSongOut) {
        if (song.songId == uiState.playingSongId) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }

        // Another song: the current one stops first
        player.pause()
        player.clearMediaItems()
        uiState = uiState.copy(playingSongId = song.songId, message = null)

        viewModelScope.launch {
            // try used as a value: the link, or null if the call failed
            val url = try {
                repository.getPreviewUrl(song.songId)
            } catch (e: HttpException) {
                null
            } catch (e: IOException) {
                null
            }

            // The user tapped another song while we were waiting: too late for this one
            if (uiState.playingSongId != song.songId) return@launch

            if (url == null) {
                uiState = uiState.copy(playingSongId = null)
                showError("Extrait indisponible pour ce titre.")
                return@launch
            }

            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

    // Heart button of a song: moves it to "Mes likes"
    fun rescue(song: SkippedSongOut) {
        // The song being rescued is the one playing: the music stops
        if (song.songId == uiState.playingSongId) stopPreview()

        // The song leaves the list at once: no second tap on the same song
        val before = uiState.skips
        uiState = uiState.copy(skips = before - song, message = null)

        viewModelScope.launch {
            try {
                val result = repository.rescue(song.dropId)
                // The backend checks the drop again: it may have expired in the meantime
                val message = if (result.likedLate) {
                    "« ${song.title} » est dans tes likes. Le drop de @${song.authorUsername} avait expiré : pas de like pour lui."
                } else {
                    "« ${song.title} » est dans tes likes. @${song.authorUsername} gagne un like."
                }
                uiState = uiState.copy(message = message, isErrorMessage = false)
            } catch (e: HttpException) {
                // 404: no skip any more, the song is already in the likes
                if (e.code() != 404) {
                    uiState = uiState.copy(skips = before)
                    showError("Le titre n'a pas pu être repêché.")
                }
            } catch (e: IOException) {
                // No network: the song comes back in the list
                uiState = uiState.copy(skips = before)
                showError("Pas de connexion : le titre n'a pas été repêché.")
            }
        }
    }

    // Leaving the screen or the app: the music must stop
    fun pausePreview() {
        player.pause()
    }

    private fun showError(message: String) {
        uiState = uiState.copy(message = message, isErrorMessage = true)
    }

    private fun stopPreview() {
        player.pause()
        player.clearMediaItems()
        uiState = uiState.copy(playingSongId = null)
    }

    // The ViewModel is destroyed (back to "Mes likes", or logout): free the player
    override fun onCleared() {
        player.release()
    }
}