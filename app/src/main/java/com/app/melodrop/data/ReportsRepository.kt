package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.ReportCreate
import com.app.melodrop.network.dto.ReportSentOut

// Talks to the backend route of the reports
class ReportsRepository {

    private val api = RetrofitClient.api

    // "Signaler le profil": the report waits in the dashboard of the meloDrop team
    suspend fun reportUser(userId: Int, reason: String, message: String?): ReportSentOut =
        api.reportUser(userId, ReportCreate(reason, message))
}
