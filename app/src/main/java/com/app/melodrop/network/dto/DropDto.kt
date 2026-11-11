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
    @SerializedName("cover_url") val coverUrl: String?,
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
    @SerializedName("cover_url") val coverUrl: String?,
    // Genre of the song's Deezer album, null when Deezer has none
    val genre: String? = null,
    // Date and time in UTC, like "2026-10-20T20:35:00+00:00"
    @SerializedName("expires_at") val expiresAt: String,
    // Number of users who liked this drop, picks included
    @SerializedName("saves_count") val savesCount: Int,
    // Number of users who picked it (part of savesCount)
    @SerializedName("picks_count") val picksCount: Int = 0,
    // Number of users who swiped it: likes, picks and skips
    @SerializedName("views_count") val viewsCount: Int = 0
)

// A user who liked one of my drops, in the list opened from its counters
data class DropLikerOut(
    // Used to open the profile (GET users/{id}/profile)
    val id: Int,
    // The @handle, without the @
    val username: String,
    // Public name chosen by the user, null if none: show the username instead
    @SerializedName("display_name") val displayName: String? = null,
    // Same names as in Avatar.kt
    @SerializedName("avatar_color") val avatarColor: String,
    @SerializedName("avatar_style") val avatarStyle: String,
    // Cosmetics of the avatar: color of the drop, decoration, face and aura.
    // Nullable: Gson leaves a missing field to null, whatever the default value
    @SerializedName("avatar_drop") val avatarDrop: String? = null,
    @SerializedName("avatar_deco") val avatarDeco: String? = null,
    @SerializedName("avatar_face") val avatarFace: String? = null,
    @SerializedName("avatar_aura") val avatarAura: String? = null,
    // True: the like was a pick
    @SerializedName("is_pick") val isPick: Boolean = false
)