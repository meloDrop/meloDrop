package com.app.melodrop.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.UsersRepository
import com.app.melodrop.network.dto.UserSearchOut
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Waiting time after the last key before searching (ms), same as the Drop search
private const val SEARCH_DELAY_MS = 400L

// Fewer letters than this: no search (the backend asks for 2)
private const val MIN_QUERY_LENGTH = 2

// A username is never longer than this: no need to ask the backend
private const val MAX_QUERY_LENGTH = 30

// Everything the user search screen needs to display
data class UserSearchUiState(
    // Text in the search field, as typed
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<UserSearchOut> = emptyList(),
    // True once an answer came back for the current text: allows "Aucun utilisateur trouvé"
    val hasSearched: Boolean = false,
    val error: String? = null
)

class UserSearchViewModel : ViewModel() {

    private val repository = UsersRepository()

    var uiState by mutableStateOf(UserSearchUiState())
        private set

    // The search waiting to run: cancelled when the user types again
    private var searchJob: Job? = null

    // Called on every key typed in the search field
    fun onQueryChange(query: String) {
        uiState = uiState.copy(query = query, hasSearched = false, error = null)
        // A new search is coming: the old one stops
        searchJob?.cancel()

        // Same text as the backend expects: no spaces around, no @ at the start
        val cleaned = query.trim().removePrefix("@")

        if (cleaned.length < MIN_QUERY_LENGTH) {
            uiState = uiState.copy(results = emptyList(), isSearching = false)
            return
        }

        // Too long to be a username: nobody can match
        if (cleaned.length > MAX_QUERY_LENGTH) {
            uiState = uiState.copy(results = emptyList(), isSearching = false, hasSearched = true)
            return
        }

        searchJob = viewModelScope.launch {
            // Waits for the user to stop typing (debounce)
            delay(SEARCH_DELAY_MS)
            uiState = uiState.copy(isSearching = true)

            uiState = try {
                val results = repository.searchUsers(cleaned)
                uiState.copy(isSearching = false, results = results, hasSearched = true)
            } catch (e: HttpException) {
                uiState.copy(
                    isSearching = false,
                    results = emptyList(),
                    error = "La recherche n'a pas abouti. Réessaie dans un instant.",
                )
            } catch (e: IOException) {
                uiState.copy(
                    isSearching = false,
                    results = emptyList(),
                    error = "Impossible de joindre le serveur.",
                )
            }
        }
    }
}