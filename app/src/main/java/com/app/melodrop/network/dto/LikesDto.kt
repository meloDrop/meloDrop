package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// A song in the user's "Mes likes" list
data class LikedSongOut(
    // Id of the song in meloDrop: used for the preview and to remove it
    @SerializedName("song_id") val songId: Int,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String,
    // When the user liked it, in UTC, like "2026-10-22T09:09:00Z"
    @SerializedName("liked_at") val likedAt: String
)