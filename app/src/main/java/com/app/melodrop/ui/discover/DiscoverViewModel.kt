package com.app.melodrop.ui.discover

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val swipeError: String? = null
)

class DiscoverViewModel : ViewModel() {

    private val repository = DiscoverRepository()

    var uiState by mutableStateOf(DiscoverUiState())
        private set

    // Loads a first batch as soon as the screen opens
    init {
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
        }
    }

    // The card on screen was swiped: right = like, left = skip
    fun onSwiped(drop: FeedItem, liked: Boolean) {
        // The card leaves at once, without waiting for the backend
        uiState = uiState.copy(drops = uiState.drops - drop, swipeError = null)

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
}