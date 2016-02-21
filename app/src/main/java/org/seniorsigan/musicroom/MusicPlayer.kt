package org.seniorsigan.musicroom

import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.util.Log

import java.io.IOException
import java.util.HashMap

object MusicPlayer {
    private val player: MediaPlayer = MediaPlayer()

    fun playMusic(url: String): String {
        val title = retrieveTitle(url)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setDataSource(url)
        player.prepareAsync()
        player.setOnPreparedListener {
            Log.i("MusicRoom", "Playing '$title'")
            player.start()
        }
        return title
    }

    private fun retrieveTitle(url: String): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(url, HashMap<String, String>())
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val track = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val title = artist + " - " + track
        retriever.release()
        return title
    }
}
