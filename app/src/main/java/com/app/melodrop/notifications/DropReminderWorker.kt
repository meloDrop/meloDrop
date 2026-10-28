package com.app.melodrop.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.app.melodrop.MainActivity
import com.app.melodrop.R

private const val CHANNEL_ID = "drop_reminder"

// Shows the "your drop expires in 15 min" notification.
// WorkManager runs it at the planned time, even if the app is closed
class DropReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // Notifications refused by the user: nothing to show
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val manager = NotificationManagerCompat.from(applicationContext)

        // Every notification belongs to a channel. Creating an existing channel does nothing
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Rappels de drop", NotificationManager.IMPORTANCE_DEFAULT)
        )

        // A tap opens the app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openApp = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("IT'S..... TIIIIIIIIME !!")
            .setContentText("15 min avant ton prochain Drop !")
            .setContentIntent(openApp)
            // Goes away after the tap, or by itself when the drop expires
            .setAutoCancel(true)
            .setTimeoutAfter(15 * 60_000L)
            .build()

        manager.notify(1, notification)
        return Result.success()
    }
}