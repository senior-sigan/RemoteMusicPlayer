package org.seniorsigan.musicroom

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.session.MediaSession
import android.util.Log

class MediaNotificationManager(val musicService: MusicService): BroadcastReceiver() {
    companion object {
        val ACTION_PAUSE = "org.seniorsigan.musicroom.pause"
        val ACTION_PLAY = "org.seniorsigan.musicroom.play"
        val NOTIFICATION_ID = 3124 // MAGIC NUMBER
        val REQIEST_CODE = 100
    }

    var started: Boolean = false
    val notificationManager: NotificationManager by lazy {
        musicService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    val pauseIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
                musicService,
                REQIEST_CODE,
                Intent(ACTION_PAUSE).setPackage(musicService.packageName),
                PendingIntent.FLAG_CANCEL_CURRENT)
    }
    val playIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
                musicService,
                REQIEST_CODE,
                Intent(ACTION_PLAY).setPackage(musicService.packageName),
                PendingIntent.FLAG_CANCEL_CURRENT)
    }

    init {
        notificationManager.cancelAll()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action
        Log.i(TAG, "Receive intent with action: $action")
    }

    fun startNotification() {
        if (!started) {
            val notification = createNotification()
            if (notification != null) {
                val filter = IntentFilter()
                filter.addAction(ACTION_PAUSE)
                filter.addAction(ACTION_PLAY)
                musicService.registerReceiver(this, filter)

                musicService.startForeground(NOTIFICATION_ID, notification)
                started = true
            }
        }
    }

    fun stopNotification() {
        if (started) {
            started = false
            try {
                notificationManager.cancel(NOTIFICATION_ID)
                musicService.unregisterReceiver(this)
            } catch(e: Exception) {
                Log.w(TAG, "stopNotification error: ${e.message}", e)
            }
            musicService.stopForeground(true)
        }
    }

    private fun createNotification(): Notification? {
        val session = MediaSession(musicService, TAG)
        val art = BitmapFactory.decodeResource(musicService.resources,
                R.drawable.default_album_art_big_card)
        val nb = Notification.Builder(musicService)
                .setSmallIcon(R.drawable.ic_action_note)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentTitle("Track title")
                .setContentText("Artist - Album")
                .setLargeIcon(art)
                .setStyle(Notification.MediaStyle()
                        .setMediaSession(session.sessionToken))

        nb.setOngoing(true)
        return nb.build()
    }
}