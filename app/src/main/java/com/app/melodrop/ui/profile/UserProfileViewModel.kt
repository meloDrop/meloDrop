package com.app.melodrop.ui.profile

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
import com.app.melodrop.network.dto.ProfileDropOut
import com.app.melodrop.network.dto.ProfileOut
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Everything the profile of another user needs to display
data class UserProfileUiState(
    // True until the first answer: nothing to show yet
    val isLoading: Boolean = true,
    // Shown when the profile could not be loaded
    val error: String? = null,
    val profile: ProfileOut? = null,
    // Drop whose preview is loaded in the player, null if none
    val playingDropId: Int? = null,
    // True when the preview plays: pause icon on its cover
    val isPlaying: Boolean = false,
    // Short message above the drops when a preview cannot be played
    val previewError: String? = null
)

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository()

    var uiState by mutableStateOf(UserProfileUiState())
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
                uiState = uiState.copy(playingDropId = null, previewError = "Extrait indisponible pour ce titre.")
            }
        })
    }

    // Called when the screen opens, with the id taken from the route
    fun load(userId: Int) {
        uiState = uiState.copy(isLoading = uiState.profile == null, error = null)

        viewModelScope.launch {
            uiState = try {
                // copy and not a new state: what the player shows is kept
                uiState.copy(isLoading = false, profile = repository.getProfile(userId))
            } catch (e: HttpException) {
                // 404: the account was deleted since the drop was seen
                val message = if (e.code() == 404) "Ce profil n'existe plus."
                else "Erreur du serveur (${e.code()})."
                uiState.copy(isLoading = false, error = message)
            } catch (e: IOException) {
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }
    }

    // Tap on a cover: plays its preview, or pauses it if it is the one loaded
    fun togglePreview(drop: ProfileDropOut) {
        if (drop.id == uiState.playingDropId) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }

        // Another drop: the current preview stops first
        player.pause()
        player.clearMediaItems()
        uiState = uiState.copy(playingDropId = drop.id, previewError = null)

        viewModelScope.launch {
            // try used as a value: the link, or null if the call failed
            val url = try {
                repository.getPreviewUrl(drop.songId)
            } catch (e: HttpException) {
                null
            } catch (e: IOException) {
                null
            }

            // The user tapped another cover while we were waiting: too late for this one
            if (uiState.playingDropId != drop.id) return@launch

            if (url == null) {
                uiState = uiState.copy(playingDropId = null, previewError = "Extrait indisponible pour ce titre.")
                return@launch
            }

            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

// Leaving the screen or the app: the music