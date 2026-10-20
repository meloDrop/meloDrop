package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// A drop shown as a card in the Discover feed
data class FeedItem(
    // Id of the drop: used later for /save and /skip
    val id: Int,
    // Id of the song in meloDrop: used later for the preview
    @SerializedName("song_id") val songId: Int,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String,
    // Genre of the song's Deezer album, null when Deezer has none
    val genre: String? = null,
    @SerializedName("author_id") val authorId: Int,
    @SerializedName("author_username") val authorUsername: String,
    @SerializedName("saves_count") val savesCount: Int
)