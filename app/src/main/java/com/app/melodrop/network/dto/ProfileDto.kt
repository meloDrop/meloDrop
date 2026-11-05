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
    @SerializedName("cover_url") val coverUrl: String?,
    // Genre of the song's Deezer album, null when Deezer has none
    val genre: String? = null,
    // Date and time in UTC, like "2026-10-22T09:09:00Z"
    @SerializedName("dropped_at") val droppedAt: String,
    // Number of users who liked this drop
    @SerializedName("saves_count") val savesCount: Int
)

// A song on the podium of the Mixtape, with its place
data class PodiumSongOut(
    // 1, 2 or 3
    val place: Int,
    // Id of the song in meloDrop: used for the preview
    @SerializedName("song_id") val songId: Int,
    // Deezer id of the song: sent back with the others to save the podium again
    @SerializedName("deezer_track_id") val deezerTrackId: Long,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String?
)

// A song of the Mixtape under a label, like "Le son qui me fait chialer"
data class LabeledSongOut(
    // Code of the label, like "cry": its text and its color are in SongLabels.kt
    val label: String,
    // Id of the song in meloDrop: used for the preview
    @SerializedName("song_id") val songId: Int,
    @SerializedName("deezer_track_id") val deezerTrackId: Long,
    val title: String,
    val artist: String,
    @SerializedName("cover_url") val coverUrl: String?
)

// A user's profile: the same for the user's own profile and for the others (ADR-0018)
data class ProfileOut(
    val id: Int,
    // The @handle: unique, used to log in, never changes
    val username: String,
    // Public name chosen by the user, null if none: show the username instead
    @SerializedName("display_name") val displayName: String? = null,
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
    @SerializedName("recent_drops") val recentDrops: List<ProfileDropOut>,
    // The podium of the Mixtape, from the 1st place to the last: empty if not made yet.
    // Default value: the previews that build a ProfileOut by hand do not need to change
    val podium: List<PodiumSongOut> = emptyList(),
    // The labeled songs of the Mixtape, in the order of the labels: empty if none yet
    @SerializedName("labeled_songs") val labeledSongs: List<LabeledSongOut> = emptyList()
)



// Data sent to change the user's own profile: the edit screen sends everything together
data class ProfileUpdate(
    // New display name; an empty text gives back the username
    @SerializedName("display_name") val displayName: String,
    // Name of one of the 5 background colors, like "mint" (AVATAR_COLORS)
    @SerializedName("avatar_color") val avatarColor: String,
    // Name of one of the 7 drop styles, like "rock" (AVATAR_STYLES)
    @SerializedName("avatar_style") val avatarStyle: String
)

// Data sent to replace the whole podium: the first id goes to the 1st place
data class PodiumUpdate(
    // Deezer ids from the search results, 3 at most; an empty list empties the podium
    @SerializedName("deezer_track_ids") val deezerTrackIds: List<Long>
)

// Data sent to put a song under a label: its Deezer id, from the search results
data class LabeledSongUpdate(
    @SerializedName("deezer_track_id") val deezerTrackId: Long
)
