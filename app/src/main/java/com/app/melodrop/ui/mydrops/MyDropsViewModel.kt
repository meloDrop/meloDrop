package com.app.melodrop.ui.mydrops

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
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.OffsetDateTime

// A drop stays online 24 hours after it was posted
private const val DROP_LIFETIME_HOURS = 24L

// Everything the "Mes drops" screen needs to display
data class MyDropsUiState(
    // True until the first answer: nothing to show yet
    val isLoading: Boolean = true,
    // Shown instead of the grid when the list could not be loaded
    val error: String? = null,
    // All the user's drops, the most recent first
    val drops: List<ProfileDropOut> = emptyList(),
    // The drop online now, marked "EN LIGNE" on its cover, null if none
    val onlineDropId: Int? = null,
    // Drop whose preview is loaded in the player, null if none
    val playingDropId: Int? = null,
    // True when the preview plays: pause icon on its cover
    val isPlaying: Boolean = false,
    // Short message above the grid when a preview cannot be played
    val previewError: String? = null
)

class MyDropsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository()

    var uiState by mutableStateOf(MyDropsUiState())
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

        // Loads the list as soon as the screen opens
        load()
    }

    fun load() {
        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            uiState = try {
                val drops = repository.getMyDrops()
                uiState.copy(isLoading = false, drops = drops, onlineDropId = findOnlineDrop(drops))
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, error = "Erreur du serveur (${e.code()}).")
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

    // Leaving the screen or the app: the music must stop
    fun pausePreview() {
        player.pause()
    }

    // The ViewModel is destroyed (the screen is closed): free the player
    override fun onCleared() {
        player.release()
    }
}

// The list is sorted from the most recent: only the first drop can still be online
private fun findOnlineDrop(drops: List<ProfileDropOut>): Int? {
    val first = drops.firstOrNull() ?: return null
    val expiresAt = OffsetDateTime.parse(first.droppedAt).plusHours(DROP_LIFETIME_HOURS)
    return if (expiresAt.isAfter(OffsetDateTime.now())) first.id else null
}