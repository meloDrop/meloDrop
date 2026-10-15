package com.app.melodrop.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Tells the UI that the backend refused the saved token (401)
object AuthEvents {

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    // Called from the network thread, so it must not suspend
    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}