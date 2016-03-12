package org.seniorsigan.musicroom.services

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.IBinder
import android.util.Log
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.onUiThread
import org.seniorsigan.musicroom.*

class MusicService: Service() {
    private lateinit var notifications: Notifications
    private lateinit var playback: Playback

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notifications = Notifications(this)
        playback = Playback(this)
        EventBus.getDefault().register(this)
        Log.d(TAG, "MusicService onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        playback.release()
        EventBus.getDefault().unregister(this)
        Log.d(TAG, "MusicService destroyed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "MusicService receive action: $action")

        when(action) {
            ACTION_STOP -> {
                App.queue.stop()
                EventBus.getDefault().post(AudioPlayedMessage())
                notificationManager.cancel(Notifications.MUSIC_NOTIFICATION_ID)
                stopSelf()
            }
            else -> {
                handlePlay()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun handlePlay() {
        val track = App.queue.current() ?: return
        EventBus.getDefault().post(track)
        playback.play()
        updateNotification(track)
    }

    @Subscribe
    fun updateNotification(track: Track) {
        Log.d(TAG, "Update notification $track")
        val notification = notifications.musicNotification(track)
        startForeground(Notifications.MUSIC_NOTIFICATION_ID, notification)
    }

    companion object {
        val ACTION_STOP = "org.seniorsigan.musicroom.ACTION_STOP_MUSIC"
    }
}