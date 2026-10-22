package com.app.melodrop.ui.likes

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
import com.app.melodrop.data.LikesRepository
import com.app.melodrop.network.dto.LikedSongOut
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Everything the "Mes likes" screen needs to display
data class LikesUiState(
    // True until the first answer: nothing to show yet
    val isLoading: Boolean = true,
    // Shown instead of the list when it could not be loaded
    val error: String? = null,
    val likes: List<LikedSongOut> = emptyList(),
    // Song loaded in the player, null if none
    val playingSongId: Int? = null,
    // True when the preview plays: pause icon on its button
    val isPlaying: Boolean = false,
    // Short message above the list: preview or removal problem
    val message: String? = null
)

class LikesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LikesRepository()

    var uiState by mutableStateOf(LikesUiState())
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
                uiState = uiState.copy(message = "Extrait indisponible pour ce titre.")
            }
        })

        // Loads the list as soon as the screen opens
        load()
    }

    fun load() {
        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            uiState = try {
                uiState.copy(isLoading = false, likes = repository.getLikes())
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }
    }

    // Play button of a song: plays its preview, or pauses it if it is the one loaded
    fun togglePreview(song: LikedSongOut) {
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
                uiState = uiState.copy(playingSongId = null, message = "Extrait indisponible pour ce titre.")
                return@launch
            }

            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

    // Removes a song from the list, after the confirmation of the screen
    fun remove(song: LikedSongOut) {
        // The song being removed is the one playing: the music stops
        if (song.songId == uiState.playingSongId) stopPreview()

        // The song leaves the list at once, without waiting for the backend
        val before = uiState.likes
        uiState = uiState.copy(likes = before - song, message = null)

        viewModelScope.launch {
            try {
                repository.removeLike(song.songId)
            } catch (e: HttpException) {
                // 404: already removed, so the list is right anyway
                if (e.code() != 404) {
                    uiState = uiState.copy(likes = before, message = "Le titre n'a pas pu être retiré.")
                }
            } catch (e: IOException) {
                // No network: the song comes back in the list
                uiState = uiState.copy(likes = before, message = "Pas de connexion : le titre n'a pas été retiré.")
            }
        }
    }

    // Leaving the screen or the app: the music must stop
    fun pausePreview() {
        player.pause()
    }

    private fun stopPreview() {
        player.pause()
        player.clearMediaItems()
        uiState = uiState.copy(playingSongId = null)
    }

    // The ViewModel is destroyed (back to the profile, or logout): free the player
    override fun onCleared() {
        player.release()
    }
}
