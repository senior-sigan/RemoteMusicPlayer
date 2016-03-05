package org.seniorsigan.musicroom

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon

class Notifications(
        val context: Context
) {
    companion object {
        val SERVER_NOTIFICATION_ID = 1
        val MUSIC_NOTIFICATION_ID = 2
    }

    fun serverNotification(msg: String): Notification? {
        val intent = Intent(context, MainActivity::class.java)
        val stopIntent = Intent(context, ServerService::class.java)
        stopIntent.action = ServerService.ACTION_STOP

        val stopAction = Notification.Action.Builder(
                Icon.createWithResource(context, android.R.drawable.ic_media_pause),
                "stop",
                PendingIntent.getService(context, 0,stopIntent, 0)
        ).build()

        val notification = Notification.Builder(context)
                .setContentTitle("MusicRoom server")
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_action_note)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                .addAction(stopAction)
                .setOngoing(true)
                .build()

        return notification
    }

    fun musicNotification(track: Track): Notification? {
        val intent = Intent(context, MainActivity::class.java)
        val stopIntent = Intent(context, MusicService::class.java)
        stopIntent.action = MusicService.ACTION_STOP

        val stopAction = Notification.Action.Builder(
                Icon.createWithResource(context, android.R.drawable.ic_media_pause),
                "stop",
                PendingIntent.getService(context, 0, stopIntent, 0)
        ).build()

        val notification = Notification.Builder(context)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentTitle(track.title)
                .setContentText(track.artist)
                .setSmallIcon(R.drawable.ic_action_note)
                .setLargeIcon(track.cover)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                .addAction(stopAction)
                .setOngoing(true)
                .setStyle(Notification.MediaStyle()
                        .setShowActionsInCompactView(0))
                .build()

        return notification
    }
}