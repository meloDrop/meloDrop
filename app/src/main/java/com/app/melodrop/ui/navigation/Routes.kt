package com.app.melodrop.ui.navigation

import android.net.Uri

// Names of the screens of the app
object Routes {
    // Before login
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFY_EMAIL = "verify_email?email={email}"
    const val FORGOT_PASSWORD = "forgot_password"

    // Main area, with the bottom bar
    const val DISCOVER = "discover"
    const val DROP = "drop"
    const val PROFILE = "profile"

    // Opened from the Profile tab, without the bottom bar
    const val EDIT_PROFILE = "edit_profile"
    const val LIKES = "likes"
    const val ACCOUNT = "account"
    // All the user's drops, opened from the "drops" counter
    const val MY_DROPS = "my_drops"
    // All the user's picks, opened from the "Tout voir" tile of the "Mes picks" tab
    const val MY_PICKS = "my_picks"
    // The podium of the Mixtape, opened from "Modifier" on the podium
    const val EDIT_PODIUM = "edit_podium"
    // The labeled songs of the Mixtape, opened from "Mes chansons"
    const val EDIT_LABELS = "edit_labels"

    // Opened from "Mes likes"
    const val SKIPS = "skips"

    // Opened from the magnifying glass of Discover, without the bottom bar
    const val SEARCH_USERS = "search_users"

    // Profile of another user, opened from a drop: "user/12"
    const val USER_PROFILE = "user/{userId}"

    // Followers and followed users of a user, opened from the counters of a profile:
    // "follows/12?tab=followers"
    const val FOLLOWS = "follows/{userId}?tab={tab}"

    // Address of the verify screen, with the email already filled in
    fun verifyEmail(email: String): String = "verify_email?email=${Uri.encode(email)}"

    // Address of the profile of a user, with their id
    fun userProfile(userId: Int): String = "user/$userId"

    // Address of the lists of a user, opened on the tab of the counter tapped
    fun follows(userId: Int, tab: String): String = "follows/$userId?tab=$tab"
}