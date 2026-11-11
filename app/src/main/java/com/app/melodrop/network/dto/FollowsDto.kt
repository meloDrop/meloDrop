package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// A user in a list of followers or of followed users
data class FollowUserOut(
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
    @SerializedName("avatar_aura") val avatarAura: String? = null
)
