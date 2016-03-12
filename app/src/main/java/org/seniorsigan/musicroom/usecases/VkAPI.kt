package org.seniorsigan.musicroom.usecases

import android.util.Log
import com.vk.sdk.VKSdk
import com.vk.sdk.api.*
import com.vk.sdk.api.model.VkAudioArray
import org.seniorsigan.musicroom.TAG
import org.seniorsigan.musicroom.TrackInfo

class VkAPI(): SearchAPI {
    override val sourceName: String = "vk"

    override fun search(query: String): List<TrackInfo> {
        if (!VKSdk.isLoggedIn()) {
            Log.w(TAG, "User not logged in VK")
            return emptyList()
        }

        val req = VKApi.audio().search(
                VKParameters(mapOf(
                        "auto_complete" to 1,
                        "sort" to 2,
                        "q" to query)))

        var tracks: MutableList<TrackInfo> = arrayListOf()

        req.executeSyncWithListener(object : VKRequest.VKRequestListener() {
            override fun onError(error: VKError?) {
                super.onError(error)
                Log.e(TAG, "Can't load data from VK: ${error?.errorMessage}")
            }

            override fun onComplete(response: VKResponse?) {
                super.onComplete(response)
                if (response != null) {
                    Log.d(TAG, "Found info ${response.json}")
                    val data = response.parsedModel as VkAudioArray
                    tracks.addAll(data.map {
                        TrackInfo(artist = it.artist, title = it.title, url = it.url, coverURL = null, source = sourceName)
                    })
                    Log.d(TAG, "VK tracks: $tracks")
                }
            }
        })

        return tracks
    }
}