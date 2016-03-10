package org.seniorsigan.musicroom.usecases

import android.net.Uri
import android.util.Log
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.seniorsigan.musicroom.App
import org.seniorsigan.musicroom.TAG
import org.seniorsigan.musicroom.TrackInfo
import java.io.IOException

class SoundCloudAPI(
        val clientID: String
): SearchAPI {
    val baseURL = "api.soundcloud.com"

    private fun searchURL(query: String) =
        with(Uri.Builder(), {
            scheme("https")
            authority(baseURL)
            appendPath("tracks")
            appendQueryParameter("client_id", clientID)
            appendQueryParameter("q", query)
        }).build().toString()

    override fun search(query: String, cb: (List<TrackInfo>) -> Unit) {
        val url = searchURL(query)
        Log.d(TAG, "Search for $query on SoundCloud: $url")
        val req = Request.Builder()
                .url(url)
                .build()
        App.okHttp.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e(TAG, "Can't load data from SoundCLoud: ${e?.message}", e)
            }

            override fun onResponse(call: Call?, res: Response?) {
                if (res == null) {
                    Log.e(TAG, "Response from SoundCloud is empty")
                    return
                }

                if (res.isSuccessful) {
                    val raw = res.body().string()
                    Log.i(TAG, raw)
                    val type = object: TypeToken<List<SCTrack>>(){}
                    val tracks = App.parseJson(raw, type)?.map {
                        TrackInfo(url = it.stream_url, coverURL = it.artwork_url, artist = it.user.username, title = it.title)
                    }
                    cb(tracks ?: emptyList())
                } else {
                    Log.i(TAG, res.message())
                }
            }
        })
    }

    private data class SCTrack(
            val artwork_url: String,
            val stream_url: String,
            val title: String,
            val user: SCUser
    )

    private data class SCUser(
            val avatar_url: String,
            val username: String
    )
}