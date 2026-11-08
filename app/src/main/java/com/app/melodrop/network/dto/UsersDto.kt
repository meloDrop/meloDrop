package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// A user in a short list: who dropped a song, who liked it
data class UserBriefOut(
    // Used to open the profile (GET users/{id}/profile)
    val id: Int,
    // The @handle, without the @
    val username: String,
    // Public name chosen by the user, null if none: show the username instead
    @SerializedName("display_name") val displayName: String? = null,
    // Same names as in Avatar.kt
    @SerializedName("avatar_color") val avatarColor: String = "lavender",
    @SerializedName("avatar_style") val avatarStyle: String = "simple"
)

// A user found by the search, as shown in the results list
data class UserSearchOut(
    // Used to open the profile (GET users/{id}/profile)
    val id: Int,
    // The @handle, without the @
    val username: String,
    // Public name chosen by the user, null if none: show the username instead
    @SerializedName("display_name") val displayName: String? = null,
    // Same names as in Avatar.kt
    @SerializedName("avatar_color") val avatarColor: String,
    @SerializedName("avatar_style") val avatarStyle: String
)