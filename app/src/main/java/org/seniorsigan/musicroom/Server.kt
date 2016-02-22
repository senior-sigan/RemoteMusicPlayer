package org.seniorsigan.musicroom

import android.util.Log

import fi.iki.elonen.NanoHTTPD
import org.greenrobot.eventbus.EventBus

class Server : NanoHTTPD(Server.PORT) {

    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        Log.i(TAG, session.uri)
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

    companion object {
        val PORT = 8765
    }
}
