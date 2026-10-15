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
    @SerializedName("linked_accounts") val linkedAccounts: List<LinkedAccountOut>
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