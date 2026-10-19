package com.app.melodrop.audio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer

// Builds the player for the 30-second previews, with the same settings on every screen
fun buildPreviewPlayer(context: Context): ExoPlayer =
    ExoPlayer.Builder(context)
        // Music: pauses by itself during a call or when another app plays sound
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true,
        )
        // Pauses when the headphones are unplugged
        .setHandleAudioBecomingNoisy(true)
        .build()