package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.ProfileDropOut
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.ProfileUpdate
import com.app.melodrop.network.dto.LikedSongOut
import com.app.melodrop.network.dto.PodiumSongOut
import com.app.melodrop.network.dto.PodiumUpdate

// Talks to the backend routes used by the profile screens
class ProfileRepository {

    private val api = RetrofitClient.api

    // The logged-in user's profile: GET /me gives the id, then the profile route
    suspend fun getMyProfile(): ProfileOut {
        val me = api.getMe()
        return api.getProfile(me.id)
    }

    // Another user's profile, opened from a drop or from the search
    suspend fun getProfile(userId: Int): ProfileOut = api.getProfile(userId)

    // All the logged-in user's drops, for "Mes drops": GET /me gives the id, then the drops route
    suspend fun getMyDrops(): List<ProfileDropOut> {
        val me = api.getMe()
        return api.getUserDrops(me.id)
    }

    // The logged-in user's picks, for the "Mes picks" tab (private: only on their own profile)
    suspend fun getMyPicks(): List<LikedSongOut> = api.getPicks()

    // Saves the display name and the avatar, and gives back the updated profile
    suspend fun updateProfile(displayName: String, avatarColor: String, avatarStyle: String): ProfileOut =
        api.updateProfile(ProfileUpdate(displayName, avatarColor, avatarStyle))

    // Saves the whole podium of the Mixtape, the first id at the 1st place, and gives back the new podium
    suspend fun updatePodium(deezerTrackIds: List<Long>): List<PodiumSongOut> =
        api.updatePodium(PodiumUpdate(deezerTrackIds))

    // Fresh preview link of a song: Deezer links expire after about 15 minutes
    suspend fun getPreviewUrl(songId: Int): String? = api.getPreview(songId).previewUrl
}