package com.app.melodrop.data

import android.content.Context
import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.CodeRequest
import com.app.melodrop.network.dto.EmailVerify
import com.app.melodrop.network.dto.PasswordReset
import com.app.melodrop.network.dto.UserLogin
import com.app.melodrop.network.dto.UserRegister

// Talks to the auth routes of the backend and keeps the token
class AuthRepository(private val context: Context) {

    private val api = RetrofitClient.api

    // Logs in, then saves the token on the phone
    suspend fun login(identifier: String, password: String) {
        val token = api.login(UserLogin(identifier, password))
        TokenStore.save(context, token.accessToken)
    }

    // Creates the account: the backend sends a code to the email
    suspend fun register(email: String, username: String, password: String) {
        api.register(UserRegister(email, username, password))
    }

    // Confirms the email with the 6-digit code
    suspend fun verifyEmail(email: String, code: String) {
        api.verifyEmail(EmailVerify(email, code))
    }

    // Asks the backend to send a new code
    suspend fun resendCode(email: String) {
        api.resendCode(CodeRequest(email))
    }

    // Forgotten password, step 1: a code is sent to the email
    suspend fun forgotPassword(email: String) {
        api.forgotPassword(CodeRequest(email))
    }

    // Forgotten password, step 2: the code and the new password
    suspend fun resetPassword(email: String, code: String, newPassword: String) {
        api.resetPassword(PasswordReset(email, code, newPassword))
    }
}