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
import com.app.melodrop.network.dto.MyDropOut
import com.app.melodrop.network.dto.TrackResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.app.melodrop.notifications.DropReminderWorker
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime

// Waiting time after the last key before searching (ms)
private const val SEARCH_DELAY_MS = 400L

// Fewer letters than this: no search
private const val MIN_QUERY_LENGTH = 2

// Everything the Drop screen needs to display
data class DropUiState(
    // True until the first answer of GET /drops/me: we do not know yet which step to show
    val isCheckingDrop: Boolean = true,
    // The user's drop online today, null if there is none
    val myDrop: MyDropOut? = null,
    // True right after posting: the screen says "C'est en ligne !"
    val justDropped: Boolean = false,
    // Step 1: search
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<TrackResult> = emptyList(),
    val searchError: String? = null,
    // Preview player: track loaded, and play or pause icon
    val playingTrackId: Long? = null,
    val isPlaying: Boolean = false,
    // Song of my drop loaded in the player (meloDrop id), null if none
    val playingSongId: Int? = null,
    // Short message under my drop when its preview cannot be played
    val previewError: String? = null,
    // Step 2: the chosen track, before dropping it
    val selectedTrack: TrackResult? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
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

            // Link expired or file unreadable: stop and tell the user
            override fun onPlayerError(error: PlaybackException) {
                player.pause()
                uiState = uiState.copy(previewError = "Extrait indisponible pour ce titre.")
            }
        })
    }

    // Asks the backend for the user's drop of today (null if none).
    // Called each time the tab opens, so the likes count stays fresh
    fun loadMyDrop() {
        viewModelScope.launch {
            uiState = try {
                val myDrop = repository.getMyDrop()
                // My drop has expired: its preview stops
                if (myDrop == null && uiState.playingSongId != null) stopPreview()
                // NEW: a drop is online, plan its reminder (replaces the old one, nothing doubles)
                if (myDrop != null) scheduleReminder(myDrop)
                uiState.copy(
                    isCheckingDrop = false,
                    myDrop = myDrop,
                    justDropped = false,
                    // A drop is online: no track can be chosen until it expires
                    selectedTrack = if (myDrop != null) null else uiState.selectedTrack,
                )
            } catch (e: HttpException) {
                // The search stays usable: a second drop would get a 409 anyway
                uiState.copy(isCheckingDrop = false)
            } catch (e: IOException) {
                uiState.copy(isCheckingDrop = false)
            }
        }
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

    // A result was tapped: go to the confirm step (the music keeps playing)
    fun selectTrack(track: TrackResult) {
        uiState = uiState.copy(selectedTrack = track, submitError = null)
    }

    // "Changer de titre" or back button: back to the results
    fun clearSelection() {
        uiState = uiState.copy(selectedTrack = null, submitError = null)
    }

    fun submitDrop() {
        val track = uiState.selectedTrack ?: return
        uiState = uiState.copy(isSubmitting = true, submitError = null)

        viewModelScope.launch {
            uiState = try {
                repository.createDrop(track.deezerTrackId)
                // Asks for the new drop again: this answer has its id and its stats
                val myDrop = repository.getMyDrop()
                // NEW: the drop is online, plan its reminder
                if (myDrop != null) scheduleReminder(myDrop)
                // Online: the music stops, the search is cleared for the next time
                stopPreview()
                uiState.copy(
                    isSubmitting = false,
                    selectedTrack = null,
                    query = "",
                    results = emptyList(),
                    myDrop = myDrop,
                    justDropped = true,
                )
            } catch (e: HttpException) {
                // 409: a drop is already online, the screen will show it
                if (e.code() == 409) loadMyDrop()
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

    // Play button of my drop: asks the backend for a fresh link, then plays it.
    // Same steps as in "Mes likes", since MyDropOut has no preview link
    fun toggleMyDropPreview() {
        val drop = uiState.myDrop ?: return

        // Already loaded: play or pause
        if (drop.songId == uiState.playingSongId) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }

        uiState = uiState.copy(playingSongId = drop.songId, playingTrackId = null, previewError = null)

        viewModelScope.launch {
            // try used as a value: the link, or null if the call failed
            val url = try {
                repository.getPreviewUrl(drop.songId)
            } catch (e: HttpException) {
                null
            } catch (e: IOException) {
                null
            }

            // The drop changed while we were waiting: too late for this one
            if (uiState.playingSongId != drop.songId) return@launch

            if (url == null) {
                uiState = uiState.copy(playingSongId = null, previewError = "Extrait indisponible pour ce titre.")
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

    // NEW: plans the "Ton drop expire dans 15 min" notification for this drop
    private fun scheduleReminder(drop: MyDropOut) {
        // Time left before the drop expires, minus 15 minutes
        val expiresAt = OffsetDateTime.parse(drop.expiresAt).toInstant()
        val delay = Duration.between(Instant.now(), expiresAt).minusMinutes(15)

        // Less than 15 minutes left: too late for a reminder
        if (delay.isNegative) return

        // "Run DropReminderWorker once, after this delay"
        val request = OneTimeWorkRequestBuilder<DropReminderWorker>()
            .setInitialDelay(delay)
            .build()

        // Only one reminder at a time: a new one replaces the one already planned
        WorkManager.getInstance(getApplication<Application>())
            .enqueueUniqueWork("drop_reminder", ExistingWorkPolicy.REPLACE, request)
    }

    private fun stopPreview() {
        player.pause()
        player.clearMediaItems()
        uiState = uiState.copy(playingTrackId = null, playingSongId = null)
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