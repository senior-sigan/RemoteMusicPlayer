package org.seniorsigan.musicroom

import android.net.Uri
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

interface CoverSearch {
    fun search(title: String, artist: String, cb: (String?) -> Unit)
}

class LastfmCoverSearch(val apiSecret: String): CoverSearch {
    override fun search(title: String, artist: String, cb: (String?) -> Unit) {
        val url = url(title, artist)
        Log.d(TAG, "Search for $title - $artist : $url")
        val req = Request.Builder()
                .url(url)
                .build()
        App.okHttp.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e(TAG, e?.message ?: "Can't load data from last.fm", e)
            }

            override fun onResponse(call: Call?, res: Response?) {
                if (res == null) {
                    Log.e(TAG, "Response is empty")
                    return
                }
                if (res.isSuccessful) {
                    val raw = res.body().string().replace("#text", "text")
                    val data = App.parseJson(raw, LastFM::class.java)
                    cb(data?.track?.album?.image?.lastOrNull()?.text)
                } else {
                    Log.i(TAG, res.message())
                }
            }
        })
    }

    fun url(title: String, artist: String) =
        with(Uri.Builder(), {
            scheme("http")
            authority("ws.audioscrobbler.com")
            appendPath("2.0")
            appendQueryParameter("method", "track.getInfo")
            appendQueryParameter("autocorrect", "1")
            appendQueryParameter("artist", artist)
            appendQueryParameter("track", title)
            appendQueryParameter("api_key", apiSecret)
            appendQueryParameter("format", "json")
        }).build().toString()

    data class LastFM(
            val track: Track = Track()
    )

    data class Track(
            val name: String = "",
            val url: String = "",
            val album: Album = Album()
    )

    data class Album(
            val artist: String = "",
            val title: String = "",
            val image: List<AlbumImage> = emptyList()
    )

    data class AlbumImage(
            val size: String = "",
            val text: String = ""
    )
}