package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.ProfileUpdate

// Talks to the backend routes used by the profile screens
class ProfileRepository {

    private val api = RetrofitClient.api

    // The logged-in user's profile: GET /me gives the id, then the profile route
    suspend fun getMyProfile(): ProfileOut {
        val me = api.getMe()
        return api.getProfile(me.id)
    }

    // Another user's profile, opened from a drop
    suspend fun getProfile(userId: Int): ProfileOut = api.getProfile(userId)


    // Saves the new display name, and gives back the updated profile
    suspend fun updateProfile(displayName: String): ProfileOut =
        api.updateProfile(ProfileUpdate(displayName))
}