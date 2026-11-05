package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.SongDetailOut

// Talks to the backend routes of the songs, for their poster
class SongRepository {

    private val api = RetrofitClient.api

    // The song, its Deezer details and its meloDrop counters
    suspend fun getSong(songId: Int): SongDetailOut = api.getSong(songId)
}
