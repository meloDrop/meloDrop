package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.FeedItem

// Talks to the backend routes used by the Discover screen
class DiscoverRepository {

    private val api = RetrofitClient.api

    // The token is added by AuthInterceptor, nothing to do here
    suspend fun getFeed(): List<FeedItem> = api.getFeed()

    // Right swipe: like the drop, its song goes to "Mes likes"
    suspend fun save(dropId: Int) = api.saveDrop(dropId)

    // Left swipe: skip the drop
    suspend fun skip(dropId: Int) = api.skipDrop(dropId)

    // Fresh preview link: Deezer links expire after about 15 minutes
    suspend fun getPreviewUrl(songId: Int): String? = api.getPreview(songId).previewUrl
}