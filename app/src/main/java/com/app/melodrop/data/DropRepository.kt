package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.TrackResult

// Talks to the backend routes used by the Drop screen
class DropRepository {

    private val api = RetrofitClient.api

    // Searches tracks on Deezer, through the backend
    suspend fun searchTracks(query: String): List<TrackResult> = api.searchTracks(query)
}