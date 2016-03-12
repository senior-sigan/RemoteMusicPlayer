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
        addRoute("/api/soundcloud.json", SoundCloudHandler::class.java)
        addRoute("/api/vk.json", VkHandler::class.java)
        addRoute("/api/play.json", BaseHandler::class.java)
        addRoute("/(.)+", StaticHandler::class.java, context.assets)
        addRoute("/", StaticHandler::class.java, context.assets)
    }



    class VkHandler: DefaultHandler() {
        override fun getMimeType(): String = "application/json"

        override fun getText(): String? {
            throw UnsupportedOperationException()
        }

        override fun getStatus() = Response.Status.OK

        override fun get(uriResource: UriResource?, urlParams: MutableMap<String, String>?, session: IHTTPSession?): Response? {
            Log.d(TAG, "Call VK search")
            val query = session?.parms?.get("q") ?: ""
            val tracks = App.vkAPI.search(query)
            return NanoHTTPD.newFixedLengthResponse(status, mimeType, App.toJson(CommonResponse(true, null, tracks)))
        }
    }

    class SoundCloudHandler: DefaultHandler() {
        override fun getMimeType(): String = "application/json"

        override fun getText(): String? {
            throw UnsupportedOperationException()
        }

        override fun getStatus() = Response.Status.OK

        override fun get(uriResource: UriResource?, urlParams: MutableMap<String, String>?, session: IHTTPSession?): Response? {
            Log.d(TAG, "Call SoundCloud search")
            val query = session?.parms?.get("q") ?: ""
            val tracks = App.soundCloud.search(query)
            return NanoHTTPD.newFixedLengthResponse(status, mimeType, App.toJson(CommonResponse(true, null, tracks)))
        }
    }

    class SearchHandler: DefaultHandler() {
        override fun getMimeType(): String = "application/json"

        override fun getText(): String? {
            throw UnsupportedOperationException()
        }

        override fun getStatus() = Response.Status.OK

        override fun get(uriResource: UriResource?, urlParams: MutableMap<String, String>?, session: IHTTPSession?): Response? {
            Log.d(TAG, "Call overall search")
            val query = session?.parms?.get("q") ?: ""
            val tracks = App.soundCloud.search(query) + App.vkAPI.search(query)
            return NanoHTTPD.newFixedLengthResponse(status, mimeType, App.toJson(CommonResponse(true, null, tracks)))
        }
    }

    class BaseHandler: DefaultHandler() {
        override fun getStatus() = Response.Status.OK

        override fun getText(): String? {
            throw UnsupportedOperationException()
        }

        override fun getMimeType() = MIME_PLAINTEXT

        override fun get(uriResource: UriResource?, urlParams: MutableMap<String, String>?, session: IHTTPSession?): Response? {
            if (session?.method == Method.POST) {
                val body: MutableMap<String, String> = mutableMapOf()
                session?.parseBody(body)
                val data = App.parseJson(body["postData"], TrackInfo::class.java) ?: return NanoHTTPD.newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Bad form data")
                try {
                    App.historyRepository.create(
                            artist = data.artist,
                            title = data.title,
                            url = data.url,
                            source = "vk"
                    )
                    App.queue.add(TrackForm(
                            url = data.url,
                            artist = data.artist,
                            title = data.title
                    ))
                    return NanoHTTPD.newFixedLengthResponse(data.name)
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                    return NanoHTTPD.newFixedLengthResponse(e.message)
                }
            }

            return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
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
