package org.seniorsigan.musicroom

import android.util.Log
import com.vk.sdk.VKSdk

import fi.iki.elonen.NanoHTTPD
import org.greenrobot.eventbus.EventBus

class Server : NanoHTTPD(Server.PORT) {

    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        Log.i(TAG, session.uri)
        when (session.uri.trim()) {
            "/vk" -> {
                Log.d(TAG, "Call VK search")
                return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, MIME_PLAINTEXT, "Call vk search")
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
