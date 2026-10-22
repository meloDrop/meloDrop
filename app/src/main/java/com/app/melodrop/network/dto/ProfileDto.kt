package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// The three counters of a profile
data class StatsOut(
    // Drops shared, expired ones included
    @SerializedName("drops_count") val dropsCount: Int,
    // Likes received on the user's drops
    @SerializedName("likes_received") val likesReceived: Int,
    // Songs in the user's "Mes likes" list
    @SerializedName("likes_count") val likesCount: Int
)

// A drop shown on a profile: the drop of the day or a past one
data class ProfileDropOut(
    // Id of the drop (not of the song)
    val id: Int,
    // Id of the song in meloDrop: used for the preview
    @SerializedName("song_id") val songId: Int,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String,
    // Genre of the song's Deezer album, null when Deezer has none
    val genre: String? = null,
    // Date and time in UTC, like "2026-10-22T09:09:00Z"
    @SerializedName("dropped_at") val droppedAt: String,
    // Number of users who liked this drop
    @SerializedName("saves_count") val savesCount: Int
)

// A user's profile: the same for the user's own profile and for the others (ADR-0018)
data class ProfileOut(
    val id: Int,
    val username: String,
    // Sign-up date in UTC, for "membre depuis"
    @SerializedName("created_at") val createdAt: String,
    // Name of the pastel background, like "lavender"
    @SerializedName("avatar_color") val avatarColor: String,
    // Name of the drop style, like "simple"
    @SerializedName("avatar_style") val avatarStyle: String,
    val stats: StatsOut,
    // The drop online today, null if there is none
    @SerializedName("current_drop") val currentDrop: ProfileDropOut? = null,
    // The 5 last expired drops, the most recent first
    @SerializedName("recent_drops") val recentDrops: List<ProfileDropOut>
)