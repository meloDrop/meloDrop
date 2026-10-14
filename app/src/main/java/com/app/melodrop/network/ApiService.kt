package com.app.melodrop.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    // Health
    @GET("health")
    suspend fun ping(): Response<ResponseBody>
}