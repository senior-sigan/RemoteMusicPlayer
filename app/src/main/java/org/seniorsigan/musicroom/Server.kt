package org.seniorsigan.musicroom

import android.content.res.AssetManager
import android.util.Log
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKApi
import com.vk.sdk.api.VKParameters
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse
import com.vk.sdk.api.model.VkAudioArray
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import org.greenrobot.eventbus.EventBus

class Server(val manager: AssetManager) : RouterNanoHTTPD(Server.PORT) {
    init {
        addMappings()
    }

    override fun addMappings() {
        super.addMappings()
        addRoute("/api/vk", VkHandler::class.java)
        addRoute("/api/url", BaseHandler::class.java)
        addRoute("/(.)+", StaticHandler::class.java, manager)
        addRoute("/", StaticHandler::class.java, manager)
    }

    class VkHandler: DefaultHandler() {
        override fun getMimeType(): String = "application/json"

        override fun getText(): String? {
            throw UnsupportedOperationException()
        }

        override fun getStatus() = Response.Status.NOT_IMPLEMENTED

        override fun get(uriResource: UriResource?, urlParams: MutableMap<String, String>?, session: IHTTPSession?): Response? {
            Log.d(TAG, "Call VK search")
            val query = urlParams?.get("q") ?: ""
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
            return NanoHTTPD.newFixedLengthResponse(status, mimeType, msg)
        }
    }

    class BaseHandler: DefaultHandler() {
        override fun getStatus() = Response.Status.OK

        override fun getText(): String? {
            throw UnsupportedOperationException()
        }

        override fun getMimeType() = MIME_PLAINTEXT

        override fun get(uriResource: UriResource?, urlParams: MutableMap<String, String>?, session: IHTTPSession?): Response? {
            val url = urlParams?.get("q") ?: return NanoHTTPD.newFixedLengthResponse("Q can't be empty")
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

    open class StaticHandler: DefaultHandler() {
        override fun getStatus()= Response.Status.OK

        override fun getText(): String? {
            throw UnsupportedOperationException()
        }

        override fun getMimeType(): String? {
            throw UnsupportedOperationException()
        }

        override fun get(uriResource: UriResource?, urlParams: MutableMap<String, String>?, session: IHTTPSession?): Response? {
            Log.i(TAG, "Request: ${session?.uri}")
            if (uriResource != null && session != null) {
                val manager = uriResource.initParameter(AssetManager::class.java)
                val uri = if (session.uri == "/") {
                    "index.html"
                } else {
                    session.uri.subSequence(1, session.uri.length).toString()
                }
                val stream = manager.open(uri)
                return NanoHTTPD.newChunkedResponse(status, getMimeTypeForFile(uri), stream)
            }
            return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "not found")
        }
    }

    companion object {
        val PORT = 8765
    }
}
