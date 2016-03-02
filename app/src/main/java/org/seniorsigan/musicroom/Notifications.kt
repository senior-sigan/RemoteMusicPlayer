package org.seniorsigan.musicroom

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent

class Notifications(
        val service: Service
) {
    companion object {
        val SERVER_NOTIFICATION_ID = 1
    }

    fun serverNotification(msg: String): Notification? {
        val intent = Intent(service, MainActivity::class.java)
        val pending = PendingIntent.getActivity(service, 0, intent, 0)
        val notification = Notification.Builder(service)
                .setContentTitle("MusicRoom server")
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_action_note)
                .setContentIntent(pending)
                .build()

        return notification
    }
}