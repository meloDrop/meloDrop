package com.app.melodrop.network

import com.app.melodrop.network.dto.MeOut
import com.app.melodrop.network.dto.MyDropOut
import com.app.melodrop.network.dto.Token
import com.app.melodrop.network.dto.UserLogin
import com.app.melodrop.network.dto.UserOut
import com.app.melodrop.network.dto.UserRegister
import com.app.melodrop.network.dto.EmailVerify
import com.app.melodrop.network.dto.CodeRequest
import com.app.melodrop.network.dto.DropCreate
import com.app.melodrop.network.dto.DropOut
import com.app.melodrop.network.dto.FeedItem
import com.app.melodrop.network.dto.PreviewOut
import com.app.melodrop.network.dto.TrackResult
import com.app.melodrop.network.dto.ProfileOut
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.app.melodrop.network.dto.ProfileUpdate
import retrofit2.http.PATCH

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

    // 204 with no body: nothing to read back
    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body body: EmailVerify)

    @POST("auth/resend-code")
    suspend fun resendCode(@Body body: CodeRequest)

    // Up to 10 drops not seen yet, or an empty list
    @GET("feed")
    suspend fun getFeed(): List<FeedItem>

    // 204 with no body: nothing to read back
    @POST("drops/{drop_id}/save")
    suspend fun saveDrop(@Path("drop_id") dropId: Int)

    @POST("drops/{drop_id}/skip")
    suspend fun skipDrop(@Path("drop_id") dropId: Int)

    // song_id is the meloDrop id of the song, not the Deezer id
    @GET("songs/{song_id}/preview")
    suspend fun getPreview(@Path("song_id") songId: Int): PreviewOut

    // Tracks found on Deezer, for example catalog/search?q=daft+punk
    @GET("catalog/search")
    suspend fun searchTracks(@Query("q") query: String): List<TrackResult>

    // 201 with the new drop, 409 if a drop is already active
    @POST("drops")
    suspend fun createDrop(@Body body: DropCreate): DropOut

    // Response<...>: lets us read a null body (no drop today) without an error
    @GET("drops/me")
    suspend fun getMyDrop(): Response<MyDropOut>

    // A user's profile: counters, drop of the day and last drops (ADR-0018)
    @GET("users/{user_id}/profile")
    suspend fun getProfile(@Path("user_id") userId: Int): ProfileOut

    // Changes the logged-in user's profile, sends back the updated profile
    @PATCH("me/profile")
    suspend fun updateProfile(@Body body: ProfileUpdate): ProfileOut
}