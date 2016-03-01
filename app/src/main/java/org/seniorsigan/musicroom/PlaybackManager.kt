package org.seniorsigan.musicroom

import android.media.session.PlaybackState
import android.util.Log

class PlaybackManager(
        val serviceCallback: PlaybackServiceCallback,
        val playback: Playback
): Playback.Callback {
    init {
        playback.cb = this
    }

    override fun onCompletion() {
        throw UnsupportedOperationException()
    }

    override fun onPlaybackStatusChanged(state: Int) {
        Log.d(TAG, "onPlaybackStatusChanged state=$state")
    }

    override fun onError(error: String) {
        throw UnsupportedOperationException()
    }

    override fun setCurrentMediaId(mediaId: String) {
        throw UnsupportedOperationException()
    }

    fun handlePlayRequest(media: String) {
        Log.d(TAG, "handlePlayRequest state=${playback.state}")
        serviceCallback.onPlaybackStart()
        playback.play(media)
    }

    interface PlaybackServiceCallback {
        fun onPlaybackStart()

        fun onNotificationRequired()

        fun onPlaybackStop()

        fun onPlaybackStateUpdated(newState: PlaybackState)
    }
}