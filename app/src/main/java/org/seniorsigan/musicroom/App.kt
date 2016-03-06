package org.seniorsigan.musicroom

import android.app.Application
import android.util.Log
import com.google.gson.GsonBuilder
import com.vk.sdk.VKSdk
import okhttp3.OkHttpClient
import org.seniorsigan.musicroom.data.DatabaseOpenHelper
import org.seniorsigan.musicroom.data.HistoryRepository

const val TAG = "MusicRoom"

class App: Application() {
    companion object {
        val CAN_USE_INTERNET = 0x1
        private val gsonBuilder = GsonBuilder()
        private val gson = gsonBuilder.create()
        val okHttp = OkHttpClient()
        lateinit var coverSearch: CoverSearch
        lateinit var queue: QueueManager
        lateinit var historyRepository: HistoryRepository

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
        queue = QueueManager(this)
        val dbHelper = DatabaseOpenHelper.getInstance(this)
        historyRepository = HistoryRepository(dbHelper)
    }
}