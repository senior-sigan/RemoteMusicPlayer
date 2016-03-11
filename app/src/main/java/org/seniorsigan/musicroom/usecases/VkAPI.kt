package org.seniorsigan.musicroom.usecases

import android.util.Log
import com.vk.sdk.VKSdk
import com.vk.sdk.api.*
import com.vk.sdk.api.model.VkAudioArray
import org.seniorsigan.musicroom.TAG
import org.seniorsigan.musicroom.TrackInfo

class VkAPI(): SearchAPI {
    override val sourceName: String = "vk"

    override fun search(query: String, cb: (Boolean, List<TrackInfo>) -> Unit) {
        if (!VKSdk.isLoggedIn()) throw Exception("User not logged in VK")

        val req = VKApi.audio().search(
                VKParameters(mapOf(
                        "auto_complete" to 1,
                        "sort" to 2,
                        "q" to query)))

        req.executeWithListener(object : VKRequest.VKRequestListener() {
            override fun onError(error: VKError?) {
                super.onError(error)
                Log.e(TAG, "Can't load data from VK: ${error?.errorMessage}")
                cb(false, emptyList())
            }

            override fun onComplete(response: VKResponse?) {
                super.onComplete(response)
                if (response != null) {
                    Log.d(TAG, "Found info ${response.json}")
                    val data = response.parsedModel as VkAudioArray
                    val tracks = data.map {
                        TrackInfo(artist = it.artist, title = it.title, url = it.url, coverURL = null, source = sourceName)
                    }
                    cb(true, tracks)
                }
                cb(false, emptyList())
            }
        })
    }
}