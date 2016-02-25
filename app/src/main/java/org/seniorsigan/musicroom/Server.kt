package org.seniorsigan.musicroom

import android.util.Log
import com.vk.sdk.VKSdk
import com.vk.sdk.api.*
import com.vk.sdk.api.model.VkAudioArray

import fi.iki.elonen.NanoHTTPD
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class Server : NanoHTTPD(Server.PORT) {

    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        Log.i(TAG, session.uri)
        when (session.uri.trim()) {
            "/vk" -> {
                Log.d(TAG, "Call VK search")
                val query = session.parms["q"] ?: ""
                val msg = if (VKSdk.isLoggedIn()) {
                    val req = VKApi.audio().search(
                            VKParameters(mapOf(
                                    "auto_complete" to 1,
                                    "sort" to 2,
                                    "q" to query)))
                    req.executeWithListener(object : VKRequest.VKRequestListener() {
                        override fun onComplete(response: VKResponse?) {
                            super.onComplete(response)
                            if (response != null) {
                                Log.i(TAG, "Found info ${response.json}")
                                val data = response.parsedModel as VkAudioArray
                                val info = MusicPlayer.retrieveInfo(data[0].url)
                                EventBus.getDefault().post(info)
                            }
                        }
                    })
                    
                    "Requested '$query' by user"
                } else {
                    "User not found"
                }
                return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, MIME_PLAINTEXT, msg)
            }
            "/soundcloud" -> {
                Log.d(TAG, "Call sound cloud search")
                return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, MIME_PLAINTEXT, "Call soundcloud search")
            }
            else -> {
                val params = session.parms
                val url = params["url"] ?: return NanoHTTPD.newFixedLengthResponse("Url can't be empty")
                try {
                    val info = MusicPlayer.retrieveInfo(url)
                    EventBus.getDefault().post(info)
                    return NanoHTTPD.newFixedLengthResponse(info.name)
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                    return NanoHTTPD.newFixedLengthResponse(e.message)
                }
            }
        }
    }

    companion object {
        val PORT = 8765
    }
}
