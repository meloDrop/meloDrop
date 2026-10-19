package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.DropCreate
import com.app.melodrop.network.dto.DropOut
import com.app.melodrop.network.dto.GenreOut
import com.app.melodrop.network.dto.TrackResult

// Talks to the backend routes used by the Drop screen
class DropRepository {

    private val api = RetrofitClient.api

    // Searches tracks on Deezer, through the backend
    suspend fun searchTracks(query: String): List<TrackResult> = api.searchTracks(query)

    // The closed list of genres
    suspend fun getGenres(): List<GenreOut> = api.getGenres()

    // Drops the song of the day with its genres
    suspend fun createDrop(deezerTrackId: Long, genreIds: List<Int>): DropOut =
        api.createDrop(DropCreate(deezerTrackId, genreIds))
}