package com.app.melodrop.network

import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException

// Reads the "detail" field of a FastAPI error, like "email_taken"
fun HttpException.detail(): String? {
    val body = response()?.errorBody()?.string() ?: return null
    return try {
        JSONObject(body).optString("detail").ifEmpty { null }
    } catch (e: JSONException) {
        null
    }
}