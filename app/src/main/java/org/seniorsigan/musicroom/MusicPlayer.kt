package org.seniorsigan.musicroom

import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.util.Log
import org.greenrobot.eventbus.EventBus

import java.io.IOException
import java.io.Serializable
import java.util.HashMap

object MusicPlayer {
    private var player: MediaPlayer = MediaPlayer()

    fun playPause(): Boolean {
        return if (player.isPlaying) {
            player.pause()
            false
        } else {
            player.start()
            true
        }
    }

    fun dispose() {
        player.stop()
        player.release()
    }

    fun playMusic(audio: TrackForm) {
        player.reset()
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setDataSource(audio.url)
        player.prepareAsync()
        player.setOnPreparedListener {
            Log.i(TAG, "Playing '${audio.name}'")
            player.start()
        }
        player.setOnCompletionListener {
            Log.i(TAG, "End playing '${audio.name}'")
            EventBus.getDefault().post(AudioPlayedMessage(audio.name))
        }
    }

    fun retrieveInfo(url: String): AudioInfo {
        Log.i(TAG, "Get info about $url")
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(url, HashMap<String, String>())
        val info = AudioInfo(
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                picture = retriever.embeddedPicture,
                url = url)
        retriever.release()
        return info
    }
}