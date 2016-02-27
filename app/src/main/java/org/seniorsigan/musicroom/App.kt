package org.seniorsigan.musicroom

import android.app.Application
import android.util.Log
import com.google.gson.GsonBuilder
import com.vk.sdk.VKSdk

const val TAG = "MusicRoom"

class App: Application() {
    companion object {
        val CAN_USE_INTERNET = 0x1
        private val gsonBuilder = GsonBuilder()
        private val gson = gsonBuilder.create()

        fun toJson(data: Any?): String {
            return gson.toJson(data)
        }
    }

    override fun onCreate() {
        super.onCreate()
        VKSdk.initialize(applicationContext)
    }
}