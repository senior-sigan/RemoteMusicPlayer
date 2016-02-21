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
            val title = MusicPlayer.retrieveTitle(url)
            EventBus.getDefault().post(AudioMessage(url = url, title = title))
            return NanoHTTPD.newFixedLengthResponse(title)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return NanoHTTPD.newFixedLengthResponse(e.message)
        }
    }

    companion object {
        val PORT = 8765
    }
}
