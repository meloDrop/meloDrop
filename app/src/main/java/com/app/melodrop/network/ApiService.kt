package com.app.melodrop.network

import com.app.melodrop.network.dto.MeOut
import com.app.melodrop.network.dto.Token
import com.app.melodrop.network.dto.UserLogin
import com.app.melodrop.network.dto.UserOut
import com.app.melodrop.network.dto.UserRegister
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // Checks that the backend answers
    @GET("health")
    suspend fun ping(): Response<ResponseBody>

    @POST("auth/login")
    suspend fun login(@Body body: UserLogin): Token

    @POST("auth/register")
    suspend fun register(@Body body: UserRegister): UserOut

    @GET("me")
    suspend fun getMe(): MeOut
}