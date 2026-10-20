package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// Data sent to drop the song of the day (the genre comes from Deezer)
data class DropCreate(
    @SerializedName("deezer_track_id") val deezerTrackId: Long
)

// A drop as sent back by the backend
data class DropOut(
    @SerializedName("song_id") val songId: Int,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String,
    // Genre of the song's Deezer album, null when Deezer has none
    val genre: String? = null,
    // Date and time in UTC, like "2026-10-20T20:35:00+00:00"
    @SerializedName("expires_at") val expiresAt: String
)

// The logged-in user's drop online today, with its stats
data class MyDropOut(
    // Id of the drop (not of the song)
    val id: Int,
    @SerializedName("song_id") val songId: Int,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String,
    // Genre of the song's Deezer album, null when Deezer has none
    val genre: String? = null,
    // Date and time in UTC, like "2026-10-20T20:35:00+00:00"
    @SerializedName("expires_at") val expiresAt: String,
    // Number of users who liked this drop
    @SerializedName("saves_count") val savesCount: Int
)