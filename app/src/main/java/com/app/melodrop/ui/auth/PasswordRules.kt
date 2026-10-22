package com.app.melodrop.ui.auth

// Same rules as the backend (check_password_complexity), checked before the call.
// Shared by "Mot de passe oublié" and "Mon compte".
// Returns the problem with the new password, or null if it is valid
internal fun newPasswordProblem(password: String, confirmPassword: String): String? = when {
    password.length !in 8..128 -> "Le mot de passe doit faire entre 8 et 128 caractères."
    password.none { it.isDigit() } -> "Le mot de passe doit contenir au moins un chiffre."
    password.none { !it.isLetterOrDigit() && it != '_' && !it.isWhitespace() } ->
        "Le mot de passe doit contenir au moins un caractère spécial."
    password != confirmPassword -> "Les deux mots de passe ne correspondent pas."
    else -> null
}
