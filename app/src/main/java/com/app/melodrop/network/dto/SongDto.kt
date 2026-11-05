package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// Fresh 30-second preview link, null if Deezer has none for this song
data class PreviewOut(
    @SerializedName("preview_url") val previewUrl: String?
)

// What a song did on meloDrop, all the users together
data class SongStatsOut(
    // Drops of this song, expired ones included
    @SerializedName("drops_count") val dropsCount: Int,
    // Users who have it in "Mes likes", picks included
    @SerializedName("likes_count") val likesCount: Int,
    // Users who made it a pick
    @SerializedName("picks_count") val picksCount: Int
)

// Everything shown on the poster of a song (GET /songs/{song_id})
data class SongDetailOut(
    @SerializedName("song_id") val songId: Int,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String?,
    // Genre of the Deezer album, null when Deezer has none
    val genre: String? = null,
    // From Deezer: null when Deezer does not answer or no longer has the track
    val album: String? = null,
    // Release date, like "2011-08-16"
    @SerializedName("release_date") val releaseDate: String? = null,
    // Length in seconds
    val duration: Int? = null,
    // True for explicit lyrics
    val explicit: Boolean? = null,
    val stats: SongStatsOut
)
