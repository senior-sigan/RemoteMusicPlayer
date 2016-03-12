package org.seniorsigan.musicroom

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.async
import org.jetbrains.anko.onUiThread
import org.seniorsigan.musicroom.services.MusicService

class QueueManager(val context: Context) {
    private var queue: Track? = null

    fun add(track: TrackInfo) {
        val t = Track(
                url = track.url,
                title = track.title,
                artist = track.artist,
                cover = BitmapFactory.decodeResource(context.resources, R.drawable.default_album_art_big_card))
        queue = t
        Log.d(TAG, "Added track to queue $queue")
        context.startService(Intent(context, MusicService::class.java))
        async() {
            if (track.coverURL == null) {
                App.coverSearch.search(track.title, track.artist, {url ->
                    loadCover(t, url)
                })
            } else {
                loadCover(t, track.coverURL)
            }
        }
    }

    private fun loadCover(track: Track, url: String?) {
        if (url == null) return
        context.onUiThread {
            Picasso.with(this).load(url).into(object : Target {
                override fun onPrepareLoad(drawable: Drawable?) {

                }

                override fun onBitmapFailed(drawable: Drawable?) {

                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    if (bitmap == null) return
                    Log.d(TAG, "Cover loaded")
                    updateCover(track, bitmap)
                }
            })
        }
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
}