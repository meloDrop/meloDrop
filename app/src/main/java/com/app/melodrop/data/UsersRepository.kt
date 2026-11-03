package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.UserSearchOut

// Talks to the backend route used by the user search screen
class UsersRepository {

    private val api = RetrofitClient.api

    // Other users whose @username starts with the query (or an empty list)
    suspend fun searchUsers(query: String): List<UserSearchOut> = api.searchUsers(query)
}