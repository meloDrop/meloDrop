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
import com.app.melodrop.network.dto.LabeledSongOut
import com.app.melodrop.network.dto.PodiumSongOut
import com.app.melodrop.network.dto.ProfileDropOut
import com.app.melodrop.network.dto.ProfileOut
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.app.melodrop.data.FollowsRepository

// Everything the profile of another user needs to display
data class UserProfileUiState(
    // True until the first answer: nothing to show yet
    val isLoading: Boolean = true,
    // Shown when the profile could not be loaded
    val error: String? = null,
    val profile: ProfileOut? = null,
    // Drop whose preview is loaded in the player, null if none
    val playingDropId: Int? = null,
    // Song of the podium whose preview is loaded in the player (its song id), null if none
    val playingPodiumSongId: Int? = null,
    // Labeled song whose preview is loaded in the player (its label code), null if none
    val playingLabel: String? = null,
    // True when the preview plays: pause icon on its cover
    val isPlaying: Boolean = false,
    // Short message above the drops when a preview cannot be played
    val previewError: String? = null,
    // True while the backend has not answered the "Suivre" button: a second tap waits
    val isFollowBusy: Boolean = false,
    // Shown under the button when the follow could not be saved
    val followError: String? = null
)

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository()

    private val followsRepository = FollowsRepository()

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
                uiState = uiState.copy(
                    playingDropId = null,
                    playingPodiumSongId = null,
                    playingLabel = null,
                    previewError = "Extrait indisponible pour ce titre.",
                )
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

    // "Suivre" or "Abonné" button: the button and the number of followers change at once,
    // then the backend is told. If it fails, the profile goes back as it was
    fun toggleFollow() {
        val profile = uiState.profile ?: return
        if (uiState.isFollowBusy) return

        val follow = !profile.isFollowed
        uiState = uiState.copy(
            isFollowBusy = true,
            followError = null,
            profile = profile.copy(
                isFollowed = follow,
                followersCount = profile.followersCount + if (follow) 1 else -1,
            ),
        )

        viewModelScope.launch {
            uiState = try {
                if (follow) followsRepository.follow(profile.id) else followsRepository.unfollow(profile.id)
                uiState.copy(isFollowBusy = false)
            } catch (e: HttpException) {
                uiState.copy(isFollowBusy = false, profile = profile, followError = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isFollowBusy = false, profile = profile, followError = "Impossible de joindre le serveur.")
            }
        }
    }

    // Tap on a drop cover: plays its preview, or pauses it if it is the one loaded
    fun togglePreview(drop: ProfileDropOut) {
        if (drop.id == uiState.playingDropId) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }

        // Another drop: it becomes the one loaded, no podium song is loaded any more
        uiState = uiState.copy(playingDropId = drop.id, playingPodiumSongId = null, playingLabel = null, previewError = null)
        startPreview(drop.songId) { uiState.playingDropId == drop.id }
    }

    // Tap on a cover of the podium: same as for a drop, for a song of the Mixtape
    fun togglePodiumPreview(song: PodiumSongOut) {
        if (song.songId == uiState.playingPodiumSongId) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }

        // Another song: it becomes the one loaded, no drop is loaded any more
        uiState = uiState.copy(playingDropId = null, playingPodiumSongId = song.songId, playingLabel = null, previewError = null)
        startPreview(song.songId) { uiState.playingPodiumSongId == song.songId }
    }

    // Tap on a cover of "Ses chansons": same as for a drop, for a labeled song of the Mixtape
    fun toggleLabeledSongPreview(song: LabeledSongOut) {
        if (song.label == uiState.playingLabel) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }

        // Another song: it becomes the one loaded, nothing else is loaded any more
        uiState = uiState.copy(
            playingDropId = null,
            playingPodiumSongId = null,
            playingLabel = song.label,
            previewError = null,
        )
        startPreview(song.songId) { uiState.playingLabel == song.label }
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
                    playingPodiumSongId = null,
                    playingLabel = null,
                    previewError = "Extrait indisponible pour ce titre.",
                )
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

    // The ViewModel is destroyed (the profile is closed): free the player
    override fun onCleared() {
        player.release()
    }
}