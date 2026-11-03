package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.LikedSongOut

// Talks to the backend routes used by the "Mes likes" screen
class LikesRepository {

    private val api = RetrofitClient.api

    // The liked songs, the most recent first
    suspend fun getLikes(): List<LikedSongOut> = api.getLikes()

    // Removes a song from the list. The author keeps the like received (ADR-0013)
    suspend fun removeLike(songId: Int) = api.removeLike(songId)

    // Fresh preview link: Deezer links expire after about 15 minutes
    suspend fun getPreviewUrl(songId: Int): String? = api.getPreview(songId).previewUrl
}