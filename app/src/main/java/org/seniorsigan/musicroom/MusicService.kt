package org.seniorsigan.musicroom

import android.app.PendingIntent
import android.content.Intent
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.util.Log

class MusicService: MediaBrowserService(), PlaybackManager.PlaybackServiceCallback {
    init {
        Log.d("TAG", "MusicService constructor")
    }

    override fun onPlaybackStart() {
        if (!session.isActive) {
            session.isActive = true
        }

        startService(Intent(applicationContext, MusicService::class.java))
    }

    override fun onNotificationRequired() {
        throw UnsupportedOperationException()
    }

    override fun onPlaybackStop() {
        throw UnsupportedOperationException()
    }

    override fun onPlaybackStateUpdated(newState: PlaybackState) {
        throw UnsupportedOperationException()
    }

    lateinit var queueManager: QueueManager
    lateinit var playback: Playback
    lateinit var playbackManager: PlaybackManager
    lateinit var session: MediaSession
    lateinit var mediaNotification: MediaNotificationManager

    override fun onLoadChildren(parentMediaId: String?, result: Result<MutableList<MediaBrowser.MediaItem>>?) {
        Log.d(TAG, "OnLoadChildren parentMediaId=$parentMediaId")
    }

    override fun onGetRoot(clientPackageName: String?, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        Log.d(TAG, "OnGetRoot clientPackageName=$clientPackageName, clientUid=$clientUid")
        return BrowserRoot("__ROOT__", null)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate from MusicService")
        queueManager = QueueManager()
        playback = Playback(this)
        playbackManager = PlaybackManager(this, playback)
        session = MediaSession(this, "MusicService")
        sessionToken = session.sessionToken
        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pi = PendingIntent.getActivity(applicationContext, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        session.setSessionActivity(pi)
        mediaNotification = MediaNotificationManager(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy from MusicService")
        mediaNotification.stopNotification()
        session.release()
        // TODO: may be some playback manager do it?
        playback.stop(false)
    }

    companion object {
        // The action of the incoming Intent indicating that it contains a command
        // to be executed (see {@link #onStartCommand})
        val ACTION_CMD = "org.seniorsigan.musicroom.ACTION_CMD"
        // The key in the extras of the incoming Intent indicating the command that
        // should be executed (see {@link #onStartCommand})
        val CMD_NAME = "CMD_NAME"
        // A value of a CMD_NAME key in the extras of the incoming Intent that
        // indicates that the music playback should be paused (see {@link #onStartCommand})
        val CMD_PAUSE = "CMD_PAUSE"
        // Delay stopSelf by using a handler.
        val STOP_DELAY = 30000
    }
}