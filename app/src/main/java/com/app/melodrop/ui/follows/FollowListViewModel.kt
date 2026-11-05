package com.app.melodrop.ui.follows

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.melodrop.data.FollowsRepository
import com.app.melodrop.network.dto.FollowUserOut
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// The two tabs of the screen, also used in the address of the screen
object FollowTab {
    const val FOLLOWERS = "followers"
    const val FOLLOWING = "following"
}

// Everything the screen of the followers and followed users needs to display
data class FollowListUiState(
    // True until the first answer: nothing to show yet
    val isLoading: Boolean = true,
    // Shown instead of the lists when they could not be loaded
    val error: String? = null,
    // The users who follow, the most recent first
    val followers: List<FollowUserOut> = emptyList(),
    // The users followed, the most recent first
    val following: List<FollowUserOut> = emptyList(),
)

class FollowListViewModel : ViewModel() {

    private val repository = FollowsRepository()

    var uiState by mutableStateOf(FollowListUiState())
        private set

    // Called when the screen opens: the two lists, so the tabs switch without waiting
    fun load(userId: Int) {
        uiState = uiState.copy(error = null)

        viewModelScope.launch {
            uiState = try {
                val followers = repository.getFollowers(userId)
                val following = repository.getFollowing(userId)
                FollowListUiState(isLoading = false, followers = followers, following = following)
            } catch (e: HttpException) {
                // 404: the account was deleted in the meantime
                val message = if (e.code() == 404) "Ce profil n'existe plus."
                else "Erreur du serveur (${e.code()})."
                uiState.copy(isLoading = false, error = message)
            } catch (e: IOException) {
                uiState.copy(isLoading = false, error = "Impossible de joindre le serveur.")
            }
        }
    }
}
