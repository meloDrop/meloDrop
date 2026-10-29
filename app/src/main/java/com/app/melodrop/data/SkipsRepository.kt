package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.RescueOut
import com.app.melodrop.network.dto.SkippedSongOut

// Talks to the backend routes used by the "Mes skips" screen
class SkipsRepository {

    private val api = RetrofitClient.api

    // The songs skipped in the last 7 days, the most recent first
    suspend fun getSkips(): List<SkippedSongOut> = api.getSkips()

    // Moves a skipped song to "Mes likes". The author gets a like only if the drop is still active
    suspend fun rescue(dropId: Int): RescueOut = api.rescueSkip(dropId)

    // Fresh preview link: Deezer links expire after about 15 minutes
    suspend fun getPreviewUrl(songId: Int): String? = api.getPreview(songId).previewUrl
}