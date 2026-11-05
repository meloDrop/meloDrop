package com.app.melodrop.ui.podium

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
import com.app.melodrop.data.ProfileRepository
import com.app.melodrop.network.dto.PodiumSongOut
import com.app.melodrop.network.dto.TrackResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Number of places on a podium (PODIUM_SIZE in the backend)
const val PODIUM_SIZE = 3

// Waiting time after the last key before searching (ms), like on the Drop screen
private const val SEARCH_DELAY_MS = 400L

// Fewer letters than this: no search
private const val MIN_QUERY_LENGTH = 2

// A song on one place of the edit screen: from the saved podium, or chosen in the search
data class PodiumChoice(
    // PUT /me/podium takes Deezer ids
    val deezerTrackId: Long,
    val title: String,
    val artist: String,
    val coverUrl: String?
)

// Everything the podium edit screen needs to display
data class PodiumEditUiState(
    // True until the saved podium arrives
    val isLoading: Boolean = true,
    // Shown instead of the podium when it could not be loaded
    val loadError: String? = null,
    // The 3 places: index 0 is the 1st place, null is an empty place
    val places: List<PodiumChoice?> = List(PODIUM_SIZE) { null },
    // Place being changed (0, 1 or 2): the search shows only then
    val editingIndex: Int? = null,
    // Search
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<TrackResult> = emptyList(),
    val searchError: String? = null,
    // Preview of a search result: track loaded, and play or pause icon
    val playingTrackId: Long? = null,
    val isPlaying: Boolean = false,
    // Saving: the podium is saved as soon as a place changes
    val isSaving: Boolean = false,
    val saveError: String? = null
)

class PodiumEditViewModel(application: Application) : AndroidViewModel(application) {

    private val profileRepository = ProfileRepository()

    // Same search as the Drop screen
    private val dropRepository = DropRepository()

    var uiState by mutableStateOf(PodiumEditUiState())
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
                uiState = uiState.copy(playingTrackId = null, searchError = "Extrait indisponible pour ce titre.")
            }
        })
    }

    // Called when the screen opens: the saved podium comes with the profile (ADR-0018)
    fun load() {
        uiState = uiState.copy(isLoading = true, loadError = null)

        viewModelScope.launch {
            uiState = try {
                val profile = profileRepository.getMyProfile()
                uiState.copy(isLoading = false, places = toPlaces(profile.podium))
            } catch (e: HttpException) {
                uiState.copy(isLoading = false, loadError = "Erreur du serveur (${e.code()}).")
            } catch (e: IOException) {
                uiState.copy(isLoading = false, loadError = "Impossible de joindre le serveur.")
            }
        }
    }

    // Tap on a place: it becomes the one to change. A second tap on it closes the search
    fun selectPlace(index: Int) {
        stopPreview()
        searchJob?.cancel()
        uiState = uiState.copy(
            editingIndex = if (uiState.editingIndex == index) null else index,
            query = "",
            results = emptyList(),
            isSearching = false,
            searchError = null,
            saveError = null,
        )
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
                val results = dropRepository.searchTracks(query.trim())
                uiState.copy(isSearching = false, results = results)
            } catch (e: HttpException) {
                // 502: Deezer did not answer
                uiState.copy(isSearching = false, searchError = "Deezer ne répond pas. Réessaie dans un instant.")
            } catch (e: IOException) {
                uiState.copy(isSearching = false, searchError = "Impossible de joindre le serveur.")
            }
        }
    }

    // A result was tapped: it takes the place being changed, then the podium is saved
    fun chooseTrack(track: TrackResult) {
        val index = uiState.editingIndex ?: return

        // Already at another place: the backend would refuse it (400 duplicate_song)
        val alreadyThere = uiState.places.withIndex().any { (i, choice) ->
            i != index && choice?.deezerTrackId == track.deezerTrackId
        }
        if (alreadyThere) {
            uiState = uiState.copy(saveError = "Ce titre est déjà sur ton podium.")
            return
        }

        val choice = PodiumChoice(track.deezerTrackId, track.title, track.artist, track.coverUrl)
        save(uiState.places.toMutableList().apply { set(index, choice) })
    }

    // Removes the song of a place. The songs below move up: a podium has no empty place in the middle
    fun removePlace(index: Int) {
        save(uiState.places.toMutableList().apply { set(index, null) })
    }

    // Sends the whole podium: the Deezer ids from the 1st place to the last, empty places left out
    private fun save(newPlaces: List<PodiumChoice?>) {
        stopPreview()
        uiState = uiState.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            uiState = try {
                val ids = newPlaces.filterNotNull().map { it.deezerTrackId }
                val podium = profileRepository.updatePodium(ids)
                // The answer is the podium as saved: the places come from it
                uiState.copy(
                    isSaving = false,
                    places = toPlaces(podium),
                    editingIndex = null,
                    query = "",
                    results = emptyList(),
                )
            } catch (e: HttpException) {
                uiState.copy(isSaving = false, saveError = saveErrorMessage(e))
            } catch (e: IOException) {
                uiState.copy(isSaving = false, saveError = "Impossible de joindre le serveur.")
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

    // Leaving the screen or the app: the music must stop
    fun pausePreview() {
        player.pause()
    }

    private fun stopPreview() {
        player.pause()
        player.clearMediaItems()
        uiState = uiState.copy(playingTrackId = null)
    }

    // The ViewModel is destroyed (the screen is closed): free the player
    override fun onCleared() {
        player.release()
    }
}

// The podium from the backend, turned into 3 places: null where nobody is yet
private fun toPlaces(podium: List<PodiumSongOut>): List<PodiumChoice?> =
    List(PODIUM_SIZE) { index ->
        podium.find { it.place == index + 1 }?.let { song ->
            PodiumChoice(song.deezerTrackId, song.title, song.artist, song.coverUrl)
        }
    }

// Turns a backend error into a message for the user
private fun saveErrorMessage(e: HttpException): String = when (e.code()) {
    400 -> "Ce titre est déjà sur ton podium."
    404 -> "Ce titre n'est plus disponible sur Deezer. Choisis-en un autre."
    502 -> "Deezer ne répond pas. Réessaie dans un instant."
    else -> "Erreur du serveur (${e.code()})."
}
