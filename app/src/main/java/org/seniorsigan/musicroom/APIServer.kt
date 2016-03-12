package org.seniorsigan.musicroom

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD

class APIServer(val context: Context) : RouterNanoHTTPD(APIServer.PORT) {
    init {
        addMappings()
    }

    override fun addMappings() {
        super.addMappings()
        addRoute("/api/search.json", SearchHandler::class.java)
        addRoute("/api/play.json", PlayHandler::class.java)
        addRoute("/(.)+", StaticHandler::class.java, context.assets)
        addRoute("/", StaticHandler::class.java, context.assets)
    }

    class SearchHandler: JSONHandler() {
        override fun handleGet(query: Map<String, String?>): CommonResponse {
            Log.d(TAG, "Call overall search")
            return try {
                val q = query["q"] ?: ""
                val sources = query["source"]?.split(",")
                val engine = App.searchFactory.engineFor(sources)
                Log.d(TAG, "Will search over ${engine.sourceName} sources")
                val tracks = engine.search(q)
                CommonResponse(true, null, tracks)
            } catch (e: Exception) {
                CommonResponse(false, e.message, null)
            }
        }
    }

    class PlayHandler: JSONHandler() {
        override fun handlePost(query: Map<String, String?>, body: String?): Any {
            try {
                val track = App.parseJson(body, TrackInfo::class.java)
                        ?: return CommonResponse(false, "Form is empty", null)

                App.historyRepository.create(
                        artist = track.artist,
                        title = track.title,
                        url = track.url,
                        source = track.source,
                        coverURL = track.coverURL
                )
                App.queue.add(track)
                return CommonResponse(true, null, track)
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                return CommonResponse(false, e.message, null)
            }
        }
    }

    class StaticHandler: DefaultHandler() {
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

open class JSONHandler: RouterNanoHTTPD.DefaultHandler() {
    override fun getMimeType(): String = "application/json"

    override fun getText(): String? {
        throw UnsupportedOperationException()
    }

    override fun getStatus() = NanoHTTPD.Response.Status.OK

    override fun get(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response? {
        return when (session?.method) {
            NanoHTTPD.Method.GET -> {
                Log.d(TAG, "GET: ${session?.uri}")
                val query = session?.parms ?: mapOf<String, String?>()
                val data = handleGet(query)
                NanoHTTPD.newFixedLengthResponse(status, mimeType, App.toJson(data))
            }
            NanoHTTPD.Method.POST -> {
                Log.d(TAG, "POST: ${session?.uri}")
                val query = session?.parms ?: mapOf<String, String?>()
                val body: MutableMap<String, String> = mutableMapOf()
                session?.parseBody(body)
                val data = handlePost(query, body["postData"])
                NanoHTTPD.newFixedLengthResponse(status, mimeType, App.toJson(data))
            }
            else ->
                NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, mimeType, "")
        }
    }

    open fun handlePost(query: Map<String, String?>, body: String?): Any {
        Log.w(TAG, "POST method not supported")
        return Any()
    }

    open fun handleGet(query: Map<String, String?>): Any {
        Log.w(TAG, "GET method not supported")
        return Any()
    }
}