package org.seniorsigan.musicroom

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.drawable.Icon

class Notifications(
        val service: Service
) {
    companion object {
        val SERVER_NOTIFICATION_ID = 1
    }

    fun serverNotification(msg: String): Notification? {
        val intent = Intent(service, MainActivity::class.java)
        val stopIntent = Intent(service, ServerService::class.java)
        stopIntent.action = ServerService.ACTION_STOP

        val stopAction = Notification.Action.Builder(
                Icon.createWithResource(service, android.R.drawable.ic_media_pause),
                "stop",
                PendingIntent.getService(service, 0,stopIntent, 0)
        ).build()

        val notification = Notification.Builder(service)
                .setContentTitle("MusicRoom server")
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_action_note)
                .setContentIntent(PendingIntent.getActivity(service, 0, intent, 0))
                .addAction(stopAction)
                .build()

        return notification
    }
}