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
import com.app.melodrop.network.dto.LikedSongOut
import retrofit2.http.DELETE

import com.app.melodrop.network.dto.EmailChangeConfirm
import com.app.melodrop.network.dto.EmailUpdate
import com.app.melodrop.network.dto.PasswordUpdate

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


    // The user's "Mes likes" list, the most recent first (or an empty list)
    @GET("me/likes")
    suspend fun getLikes(): List<LikedSongOut>

    // Removes a song from "Mes likes". 204 with no body: nothing to read back
    @DELETE("me/likes/{song_id}")
    suspend fun removeLike(@Path("song_id") songId: Int)


    // Asks to change the email: a code is sent to the new one. 204 with no body
    @PATCH("me/email")
    suspend fun updateEmail(@Body body: EmailUpdate)

    // Confirms the new email with the code received there
    @POST("me/email/confirm")
    suspend fun confirmEmail(@Body body: EmailChangeConfirm): UserOut

    // Changes the password. 204 with no body
    @PATCH("me/password")
    suspend fun updatePassword(@Body body: PasswordUpdate)

    // Deletes the account and all its data. 204 with no body
    @DELETE("me")
    suspend fun deleteMe()
}