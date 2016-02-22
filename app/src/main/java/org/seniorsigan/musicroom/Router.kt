package org.seniorsigan.musicroom

import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.Serializable

open class Router {
    @Subscribe
    open fun onAudioAdded(audioInfo: AudioInfo) {
        Log.i(TAG, "Received $audioInfo")
        try {
            MusicPlayer.playMusic(audioInfo)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }

    @Subscribe
    open fun onAudioPlayed(msg: AudioPlayedMessage) {
        Log.i(TAG, "Finished $msg")
    }

    fun onStart() {
        EventBus.getDefault().register(this)
    }

    fun onStop() {
        EventBus.getDefault().unregister(this)
    }
}

data class AudioPlayedMessage(val title: String): Serializable