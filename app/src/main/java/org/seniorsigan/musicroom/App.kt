package org.seniorsigan.musicroom

import android.app.Application
import com.vk.sdk.VKSdk

const val TAG = "MusicRoom"

class App: Application() {
    companion object {
        val CAN_USE_INTERNET = 0x1
    }

    override fun onCreate() {
        super.onCreate()
        VKSdk.initialize(applicationContext)
    }
}