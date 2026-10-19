package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.FeedItem

// Talks to the backend routes used by the Discover screen
class DiscoverRepository {

    private val api = RetrofitClient.api

    // The token is added by AuthInterceptor, nothing to do here
    suspend fun getFeed(): List<FeedItem> = api.getFeed()
}