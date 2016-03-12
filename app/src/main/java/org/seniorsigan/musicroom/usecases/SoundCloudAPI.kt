package org.seniorsigan.musicroom.usecases

import android.net.Uri
import android.util.Log
import com.google.gson.reflect.TypeToken
import okhttp3.Request
import org.seniorsigan.musicroom.App
import org.seniorsigan.musicroom.TAG
import org.seniorsigan.musicroom.TrackInfo

class SoundCloudAPI(
        val clientID: String
): SearchAPI {
    override val sourceName: String = "soundcloud"
    val baseURL = "api.soundcloud.com"

    private fun searchURL(query: String) =
        with(Uri.Builder(), {
            scheme("https")
            authority(baseURL)
            appendPath("tracks")
            appendQueryParameter("client_id", clientID)
            appendQueryParameter("q", query)
            appendQueryParameter("limit", SEARCH_LIMIT.toString())
        }).build().toString()

    override fun search(query: String): List<TrackInfo> {
        val url = searchURL(query)
        Log.d(TAG, "Search for $query on SoundCloud: $url")
        val req = Request.Builder()
                .url(url)
                .build()
        val res = App.okHttp.newCall(req).execute()
        if (res.isSuccessful) {
            val raw = res.body().string()
            Log.i(TAG, "SoundCloud response: $raw")
            val type = object: TypeToken<List<SCTrack>>(){}
            val tracks = App.parseJson(raw, type)?.map {
                TrackInfo(
                        url = "${it.stream_url}?client_id=$clientID",
                        coverURL = it.artwork_url?.replace("-large", "-t500x500"),
                        artist = it.user.username,
                        title = it.title,
                        source = sourceName)
            }
            Log.d(TAG, "SoundCloud tracks: $tracks")
            return tracks ?: emptyList()
        } else {
            Log.e(TAG, "SoundCloud API error ${res.message()}")
            return emptyList()
        }
    }

    private data class SCTrack(
            val artwork_url: String?,
            val stream_url: String,
            val title: String,
            val user: SCUser
    )

    private data class SCUser(
            val avatar_url: String,
            val username: String
    )
}