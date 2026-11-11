package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.ProfileDropOut
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.ProfileUpdate
import com.app.melodrop.network.dto.LikedSongOut
import com.app.melodrop.network.dto.LabeledSongOut
import com.app.melodrop.network.dto.LabeledSongUpdate
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

    // All the drops of another user, for "Ses drops", opened from their profile
    suspend fun getUserDrops(userId: Int): List<ProfileDropOut> = api.getUserDrops(userId)

    // The logged-in user's picks, for the "Mes picks" tab (private: only on their own profile)
    suspend fun getMyPicks(): List<LikedSongOut> = api.getPicks()

    // Saves the display name and the avatar, and gives back the updated profile
    suspend fun updateProfile(update: ProfileUpdate): ProfileOut = api.updateProfile(update)

    // Saves the whole podium of the Mixtape, the first id at the 1st place, and gives back the new podium
    suspend fun updatePodium(deezerTrackIds: List<Long>): List<PodiumSongOut> =
        api.updatePodium(PodiumUpdate(deezerTrackIds))

    // Puts a song under a label of the Mixtape, and gives back all the labeled songs
    suspend fun setLabeledSong(label: String, deezerTrackId: Long): List<LabeledSongOut> =
        api.setLabeledSong(label, LabeledSongUpdate(deezerTrackId))

    // Removes the song of a label from the Mixtape
    suspend fun removeLabeledSong(label: String) {
        api.removeLabeledSong(label)
    }

    // Fresh preview link of a song: Deezer links expire after about 15 minutes
    suspend fun getPreviewUrl(songId: Int): String? = api.getPreview(songId).previewUrl
}