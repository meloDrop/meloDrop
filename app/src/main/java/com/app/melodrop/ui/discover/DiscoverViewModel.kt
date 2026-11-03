package com.app.melodrop.ui.discover

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
import com.app.melodrop.data.DiscoverRepository
import com.app.melodrop.network.dto.FeedItem
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


// Everything the Discover screen needs to display
data class DiscoverUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    // Drops not seen yet: the first one is the card on screen
    val drops: List<FeedItem> = emptyList(),
    // Short message when a swipe could not be sent
    val swipeError: String? = null,
    // True when the preview plays (or is about to): pause icon on the button
    val isPlaying: Boolean = false,
    // Short message when the preview of the card cannot be played
    val previewError: String? = null
)

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DiscoverRepository()

    var uiState by mutableStateOf(DiscoverUiState())
        private set

    // Plays the previews, one at a time. It lives as long as the ViewModel,
// so it survives a screen rotation or a change of tab
    private val player = buildPreviewPlayer(application)

    init {
        // The preview starts again at the end, while the card stays on screen
        player.repeatMode = Player.REPEAT_MODE_ONE

        player.addListener(object : Player.Listener {
            // Play or pause asked (by the button or by the code): update the icon
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                uiState = uiState.copy(isPlaying = playWhenReady)
            }

            // Link expired or file unreadable: stop and tell the user
            override fun onPlayerError(error: PlaybackException) {
                player.pause()
                uiState = uiState.copy(previewError = "Extrait indisponible pour ce titre.")
            }
        })

        // Loads a first batch as soon as the screen opens
        loadFeed()
    }

    fun loadFeed() {
        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            uiState = try {
                DiscoverUiState(drops = repository.getFeed())
            } catch (e: HttpException) {
                DiscoverUiState(error = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                // Backend down, no network, or adb reverse missing
                DiscoverUiState(error = "Impossible de joindre le serveur.")
            }

            // New first card: play its preview (or stop if the feed is empty)
            playTop()
        }
    }

    // The card on screen was swiped: right = like, left = skip
    fun onSwiped(drop: FeedItem, liked: Boolean) {
        // The card leaves at once, without waiting for the backend
        uiState = uiState.copy(
            drops = uiState.drops - drop,
            swipeError = null,
            previewError = null,
        )
        // The next card's preview takes over
        playTop()

        viewModelScope.launch {
            try {
                if (liked) repository.save(drop.id) else repository.skip(drop.id)
            } catch (e: HttpException) {
                // 404 (expired) or 409 (already seen): the card is gone anyway
            } catch (e: IOException) {
                uiState = uiState.copy(swipeError = "Pas de connexion : ce choix n'a pas été enregistré.")
            }

            // Last card done: ask for a new batch, once the choice is saved
            if (uiState.drops.isEmpty() && !uiState.isLoading) {
                loadFeed()
            }
        }
    }

    // Play/pause button on the card
    fun togglePlay() {
        if (player.playWhenReady) player.pause() else player.play()
    }

    // Leaving the tab or the app: the music must stop
    fun pausePreview() {
        player.pause()
    }

    // Stops the current preview, then plays the one of the card on top
    private fun playTop() {
        player.pause()
        player.clearMediaItems()
        val drop = uiState.drops.firstOrNull() ?: return

        viewModelScope.launch {
            // try used as a value: the link, or null if the call failed
            val url = try {
                repository.getPreviewUrl(drop.songId)
            } catch (e: HttpException) {
                null
            } catch (e: IOException) {
                null
            }

            // The user swiped while we were waiting: too late for this card
            if (uiState.drops.firstOrNull()?.id != drop.id) return@launch

            if (url == null) {
                uiState = uiState.copy(previewError = "Extrait indisponible pour ce titre.")
                return@launch
            }

            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            // Starts on its own: remove this line to wait for a tap on the button
            player.play()
        }
    }

    // The ViewModel is destroyed (logout): free the player
    override fun onCleared() {
        player.release()
    }
}