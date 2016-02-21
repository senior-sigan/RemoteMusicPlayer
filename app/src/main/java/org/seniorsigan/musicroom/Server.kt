package org.seniorsigan.musicroom

import android.util.Log

import fi.iki.elonen.NanoHTTPD

class Server : NanoHTTPD(Server.PORT) {

    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        Log.i("MusicRoom", session.uri)
        val params = session.parms
        val url = params["url"] ?: return NanoHTTPD.newFixedLengthResponse("Url can't be empty")
        try {
            return NanoHTTPD.newFixedLengthResponse(MusicPlayer.playMusic(url))
        } catch (e: Exception) {
            Log.e("MusicRoom", e.message, e)
            return NanoHTTPD.newFixedLengthResponse(e.message)
        }
    }

    companion object {
        val PORT = 8765
    }
}
