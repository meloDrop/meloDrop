package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.EmailChangeConfirm
import com.app.melodrop.network.dto.EmailUpdate
import com.app.melodrop.network.dto.MeOut
import com.app.melodrop.network.dto.PasswordUpdate

// Talks to the backend routes used by the "Mon compte" screen
class AccountRepository {

    private val api = RetrofitClient.api

    // The account: email, @username, and the email waiting for its code
    suspend fun getMe(): MeOut = api.getMe()

    // Step 1 of the email change: a code is sent to the new email
    suspend fun changeEmail(password: String, newEmail: String) =
        api.updateEmail(EmailUpdate(password, newEmail))

    // Step 2: the code received at the new email; the email then changes
    suspend fun confirmEmail(code: String) = api.confirmEmail(EmailChangeConfirm(code))

    suspend fun changePassword(oldPassword: String, newPassword: String) =
        api.updatePassword(PasswordUpdate(oldPassword, newPassword))

    // Deletes the account; the screen then logs out, like the logout button
    suspend fun deleteAccount() = api.deleteMe()
}