package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

data class UserLogin(
    val identifier: String,
    val password: String
)

data class Token(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class LinkedAccountOut(
    val id: Int,
    val provider: String
)

data class MeOut(
    val id: Int,
    val email: String,
    val username: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("linked_accounts") val linkedAccounts: List<LinkedAccountOut>,
    // New email waiting for its code, null the rest of the time
    @SerializedName("pending_email") val pendingEmail: String? = null
)

data class UserRegister(
    val email: String,
    val username: String,
    val password: String
)

data class UserOut(
    val id: Int,
    val email: String,
    val username: String
)

data class EmailVerify(
    val email: String,
    val code: String
)

data class CodeRequest(
    val email: String
)


// Data sent to change the email: the current password and the new email
data class EmailUpdate(
    val password: String,
    @SerializedName("new_email") val newEmail: String
)

// The 6-digit code received at the new email
data class EmailChangeConfirm(
    val code: String
)

// Data sent to change the password
data class PasswordUpdate(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String
)

// Data sent to reset a forgotten password with the code received by email
data class PasswordReset(
    val email: String,
    val code: String,
    @SerializedName("new_password") val newPassword: String
)