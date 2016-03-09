package org.seniorsigan.musicroom

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.seniorsigan.musicroom.services.MusicService

class QueueManager(val context: Context) {
    private var queue: Track? = null

    fun add(track: TrackForm) {
        queue = Track(
                url = track.url,
                title = track.title ?: "Unknown title",
                artist = track.artist ?: "Unknown artist",
                cover = BitmapFactory.decodeResource(context.resources, R.drawable.default_album_art_big_card),
                artistCover = BitmapFactory.decodeResource(context.resources, R.drawable.default_album_art_big_card))
        Log.d(TAG, "Added track to queue $queue")
        context.startService(Intent(context, MusicService::class.java))
    }

    fun current(): Track? {
        return queue
    }

    fun next() {
        queue = null
    }

    fun updateCover(track: Track, bitmap: Bitmap) {
        if (current()?.url == track.url) {
            queue = current()?.copy(cover = bitmap)
            EventBus.getDefault().post(current())
        }
    }

    fun stop() {
        queue = null
    }

    fun updateArtist(track: Track, bitmap: Bitmap) {
        if (current()?.url == track.url) {
            queue = current()?.copy(artistCover = bitmap)
            EventBus.getDefault().post(current())
        }
    }
}