package com.app.melodrop.network.dto

import com.google.gson.annotations.SerializedName

// Fresh 30-second preview link, null if Deezer has none for this song
data class PreviewOut(
    @SerializedName("preview_url") val previewUrl: String?
)