package com.app.melodrop.data

import android.content.Context
import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.UserLogin
import com.app.melodrop.network.dto.UserRegister

// Talks to the auth routes of the backend and keeps the token
class AuthRepository(private val context: Context) {

    private val api = RetrofitClient.api

    // Logs in, then saves the token on the phone
    suspend fun login(identifier: String, password: String) {
        val token = api.login(UserLogin(identifier, password))
        TokenStore.save(context, token.accessToken)
    }

    // Creates the account, then logs in with the same password
    suspend fun register(email: String, username: String, password: String) {
        api.register(UserRegister(email, username, password))
        login(username, password)
    }
}