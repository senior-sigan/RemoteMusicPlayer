package org.seniorsigan.musicroom

import android.app.Application
import android.util.Log
import com.google.gson.GsonBuilder
import com.vk.sdk.VKSdk
import okhttp3.OkHttpClient

const val TAG = "MusicRoom"

class App: Application() {
    companion object {
        val CAN_USE_INTERNET = 0x1
        private val gsonBuilder = GsonBuilder()
        private val gson = gsonBuilder.create()
        val okHttp = OkHttpClient()
        lateinit var coverSearch: CoverSearch
        val queue: QueueManager = QueueManager()

        fun toJson(data: Any?): String {
            return gson.toJson(data)
        }

        fun <T> parseJson(data: String?, clazz: Class<T>): T? {
            try {
                return gson.fromJson(data, clazz)
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                return null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        VKSdk.initialize(applicationContext)
        coverSearch = LastfmCoverSearch(getString(R.string.lastfm_key))
    }
}