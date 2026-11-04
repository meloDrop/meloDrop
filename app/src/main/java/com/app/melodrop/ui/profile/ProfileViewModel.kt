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
import com.app.melodrop.network.dto.LikedSongOut
import com.app.melodrop.network.dto.ProfileDropOut
import com.app.melodrop.network.dto.ProfileOut
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Everything the Profile screen needs to display
data class ProfileUiState(
    // True until the first answer: nothing to show yet
    val isLoading: Boolean = true,
    // Shown only when there is no profile to display
    val error: String? = null,
    val profile: ProfileOut? = null,
    // The user's picks, for the "Mes picks" tab, the most recent like first
    val picks: List<LikedSongOut> = emptyList(),
    // Shown in the "Mes picks" tab when the picks could not be loaded
    val picksError: String? = null,
    // Drop whose preview is loaded in the player, null if none
    val playingDropId: Int? = null,
    // Pick whose preview is loaded in the player (its song id), null if none
    val playingPickId: Int? = null,
    // True when the preview plays: pause icon on its cover
    val isPlaying: Boolean = false,
    // Short message above the drops when a preview cannot be played
    val previewError: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository()

    var uiState by mutableStateOf(ProfileUiState())
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
                uiState = uiState.copy(
                    playingDropId = null,
                    playingPickId = null,
                    previewError = "Extrait indisponible pour ce titre.",
                )
            }
        })
    }

    // Called each time the tab opens, so the counters and the drops stay fresh
    fun load() {
        // The old profile stays on screen while the new one arrives
        uiState = uiState.copy(isLoading = uiState.profile == null, error = null)

        viewModelScope.launch {
            uiState = try {
                // Wait for the answer first, then copy the state of that moment:
                // the picks loaded meanwhile are kept
                val profile = repository.getMyProfile()
                // copy and not a new state: what the player shows is kept
                uiState.copy(isLoading = false, profile = profile)
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                // Backend down, no network, or adb reverse missing
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }

        // Loaded apart: if the picks fail, the profile still shows
        loadPicks()
    }

    // The user's picks, fresh each time the tab opens (a star may have changed in "Mes likes")
    private fun loadPicks() {
        viewModelScope.launch {
            try {
                // Same as for the profile: the answer first, then the copy
                val picks = repository.getMyPicks()
                uiState = uiState.copy(picks = picks, picksError = null)
            } catch (e: HttpException) {
                uiState = uiState.copy(picksError = "Tes picks n'ont pas pu se charger.")
            } catch (e: IOException) {
                uiState = uiState.copy(picksError = "Tes picks n'ont pas pu se charger.")
            }
        }
    }

    // Tap on a drop cover: plays its preview, or pauses it if it is the one loaded
    fun togglePreview(drop: ProfileDropOut) {
        if (drop.id == uiState.playingDropId) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }

        // Another drop: it becomes the one loaded, no pick is loaded any more
        uiState = uiState.copy(playingDropId = drop.id, playingPickId = null, previewError = null)
        startPreview(drop.songId) { uiState.playingDropId == drop.id }
    }

    // Tap on a pick cover: same as for a drop, for a song of "Mes picks"
    fun togglePickPreview(pick: LikedSongOut) {
        if (pick.songId == uiState.playingPickId) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }

        // Another pick: it becomes the one loaded, no drop is loaded any more
        uiState = uiState.copy(playingDropId = null, playingPickId = pick.songId, previewError = null)
        startPreview(pick.songId) { uiState.playingPickId == pick.songId }
    }

    // Stops the current preview, gets a fresh link, then plays the song.
    // stillWanted: false when the user tapped another cover while we were waiting
    private fun startPreview(songId: Int, stillWanted: () -> Boolean) {
        player.pause()
        player.clearMediaItems()

        viewModelScope.launch {
            // try used as a value: the link, or null if the call failed
            val url = try {
                repository.getPreviewUrl(songId)
            } catch (e: HttpException) {
                null
            } catch (e: IOException) {
                null
            }

            // Another cover was tapped meanwhile: too late for this one
            if (!stillWanted()) return@launch

            if (url == null) {
                uiState = uiState.copy(
                    playingDropId = null,
                    playingPickId = null,
                    previewError = "Extrait indisponible pour ce titre.",
                )
                return@launch
            }

            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

    // Leaving the tab or the app: the music must stop
    fun pausePreview() {
        player.pause()
    }

    // The ViewModel is destroyed (logout): free the player
    override fun onCleared() {
        player.release()
    }
}