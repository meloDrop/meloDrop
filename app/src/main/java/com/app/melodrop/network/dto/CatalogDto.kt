package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// A track found on Deezer, as shown in the search results
data class TrackResult(
    // Long: some Deezer ids are above the Int limit (about 2.1 billion)
    @SerializedName("deezer_track_id") val deezerTrackId: Long,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String,
    // null when Deezer has no preview for this track
    @SerializedName("preview_url") val previewUrl: String?
)