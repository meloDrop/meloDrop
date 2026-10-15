package com.app.melodrop.network

import com.app.melodrop.data.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenStore.token
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        val response = chain.proceed(request)

        // The saved token was refused: the session is over (ADR-0007)
        if (response.code == 401 && token != null) {
            AuthEvents.notifySessionExpired()
        }

        return response
    }
}