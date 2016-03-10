package org.seniorsigan.musicroom.usecases

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.seniorsigan.musicroom.App
import org.seniorsigan.musicroom.TAG
import java.io.IOException

class SoundCloudAPI(
        val clientID: String
) {
    val baseURL = "https://api.soundcloud.com"

    private fun searchURL(query: String) =
            "$baseURL/tracks?client_id=$clientID&q=$query"

    fun search(query: String, cb: (String) -> Unit) {
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
                    cb(raw)
                } else {
                    Log.i(TAG, res.message())
                }
            }
        })
    }
}