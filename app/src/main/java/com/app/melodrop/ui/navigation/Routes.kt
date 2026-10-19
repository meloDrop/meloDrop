package com.app.melodrop.ui.navigation

import android.net.Uri

// Names of the screens of the app
object Routes {
    // Before login
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFY_EMAIL = "verify_email?email={email}"

    // Main area, with the bottom bar
    const val DISCOVER = "discover"
    const val DROP = "drop"
    const val PROFILE = "profile"

    // Address of the verify screen, with the email already filled in
    fun verifyEmail(email: String): String = "verify_email?email=${Uri.encode(email)}"
}