package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// A song shown as a card in the Discover feed (ADR-0021).
// When several users dropped the same song, the card shows it once
data class FeedItem(
    // Id of the first drop of the song: used later for /save and /skip,
    // and the swipe counts for every drop of the song
    val id: Int,
    // Id of the song in meloDrop: used later for the preview
    @SerializedName("song_id") val songId: Int,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String?,
    // Genre of the song's Deezer album, null when Deezer has none
    val genre: String? = null,
    // First user in droppers: shown alone when only one user dropped the song
    @SerializedName("author_id") val authorId: Int,
    @SerializedName("author_username") val authorUsername: String,
    // Users who liked the song on its drops online, each one counted once
    @SerializedName("saves_count") val savesCount: Int,
    // True when the logged-in user follows one of the droppers: "Abonné" on the card
    @SerializedName("is_followed") val isFollowed: Boolean = false,
    // Everyone who dropped the song and is online: the followed users first.
    // Nullable: Gson leaves a missing list to null, whatever the default value
    val droppers: List<UserBriefOut>? = null
)