package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// A song in the user's "Mes skips" list
data class SkippedSongOut(
    // Id of the skipped drop: used to rescue it
    @SerializedName("drop_id") val dropId: Int,
    // Id of the song in meloDrop: used for the preview
    @SerializedName("song_id") val songId: Int,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String?,
    // When the user skipped it, in UTC, like "2026-10-22T09:09:00Z"
    @SerializedName("skipped_at") val skippedAt: String,
    @SerializedName("author_id") val authorId: Int,
    @SerializedName("author_username") val authorUsername: String,
    // True while the drop is online: a rescue then gives a like to the author
    @SerializedName("drop_active") val dropActive: Boolean
)

// Result of a rescue from "Mes skips"
data class RescueOut(
    @SerializedName("song_id") val songId: Int,
    // False: the drop was online, the author got a like.
    // True: the drop had expired, no like: "Récupéré dans les skips"
    @SerializedName("liked_late") val likedLate: Boolean
)