package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// A report sent from the profile of another user
data class ReportCreate(
    // Code of the reason: "name", "content", "spam", "harassment" or "other"
    val reason: String,
    // What the user explains, null if nothing was written (needed for "other")
    val message: String?
)

// What the backend sends back once the report is saved
data class ReportSentOut(
    val id: Int,
    // Date and time in UTC, like "2026-11-06T09:09:00Z"
    @SerializedName("created_at") val createdAt: String
)
