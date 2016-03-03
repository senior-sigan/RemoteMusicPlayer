package org.seniorsigan.musicroom

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.IBinder
import android.util.Log
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.onUiThread

class MusicService: Service() {
    private lateinit var notifications: Notifications
    private lateinit var playback: Playback

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notifications = Notifications(this)
        playback = Playback(this)
        Log.d(TAG, "MusicService onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        playback.release()
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
        updateNotification()
        try {
            App.coverSearch.search(track.title, track.artist, { url ->
                Log.d(TAG, "Found cover: $url")
                url ?: return@search
                onUiThread {
                    Picasso.with(this).load(url).into(object : Target {
                        override fun onPrepareLoad(drawable: Drawable?) {

                        }

                        override fun onBitmapFailed(drawable: Drawable?) {

                        }

                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                            if (bitmap == null) return
                            Log.d(TAG, "Cover loaded")
                            App.queue.updateCover(track, bitmap)
                            updateNotification()
                        }
                    })
                }
            })
        } catch(e: Exception) {
            Log.e(TAG, "MusicService handlePlay: ${e.message}", e)
        }
    }

    private fun updateNotification() {
        val track = App.queue.current() ?: return
        Log.d(TAG, "Update notification $track")
        val notification = notifications.musicNotification(track)
        startForeground(Notifications.MUSIC_NOTIFICATION_ID, notification)
    }

    companion object {
        val ACTION_STOP = "org.seniorsigan.musicroom.ACTION_STOP_MUSIC"
    }
}