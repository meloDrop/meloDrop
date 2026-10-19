package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.DropCreate
import com.app.melodrop.network.dto.DropOut
import com.app.melodrop.network.dto.GenreOut
import com.app.melodrop.network.dto.MyDropOut
import com.app.melodrop.network.dto.TrackResult
import retrofit2.HttpException

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

    // The user's drop online today with its stats, or null if there is none
    suspend fun getMyDrop(): MyDropOut? {
        val response = api.getMyDrop()
        // With Response, Retrofit does not throw on errors: we do it, like the other calls
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()
    }
}