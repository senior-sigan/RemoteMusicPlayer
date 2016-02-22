package org.seniorsigan.musicroom

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val server: Server = Server()
    private lateinit var coverView: ImageView
    private lateinit var titleView: TextView
    private lateinit var artistView: TextView
    private lateinit var playPauseBtn: ImageButton

    @Subscribe
    fun onAudioAdded(audioInfo: AudioInfo) {
        Log.i(TAG, "Received $audioInfo")
        onUiThread {
            with(audioInfo, {
                if (picture != null) {
                    coverView.image = BitmapDrawable(
                            resources,
                            BitmapFactory.decodeByteArray(picture, 0, picture.size))
                } else {
                    coverView.image = resources.getDrawable(
                            R.drawable.default_album_art_big_card, theme)
                }

                titleView.text = (title ?: "Unknown title")
                artistView.text = (artist ?: "Unknown artist")
                playPauseBtn.image = resources.getDrawable(
                        android.R.drawable.ic_media_pause, theme)
            })
        }
        try {
            MusicPlayer.playMusic(audioInfo)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }

    @Subscribe
    fun onAudioPlayed(msg: AudioPlayedMessage) {
        Log.i(TAG, "Finished $msg")
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        coverView = find<ImageView>(R.id.coverView)
        titleView = find<TextView>(R.id.titleText)
        artistView = find<TextView>(R.id.artistText)
        playPauseBtn = find<ImageButton>(R.id.playPauseButton)
        playPauseBtn.onClick {
            if (MusicPlayer.playPause()) {
                playPauseBtn.image = resources.getDrawable(
                        android.R.drawable.ic_media_pause, theme)
            } else {
                playPauseBtn.image = resources.getDrawable(
                        android.R.drawable.ic_media_play, theme)
            }
        }
    }

    private fun restartServer() {
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        val formattedIpAddress = String.format(
                Locale.ENGLISH, "%d.%d.%d.%d",
                ipAddress and 0xff, ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff, ipAddress shr 24 and 0xff)

        try {
            server.stop()
            server.start()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }

        toast("Listen http://" + formattedIpAddress + ":" + Server.PORT)
        Log.i(TAG, "Listen http://" + formattedIpAddress + ":" + Server.PORT)
    }

    private fun stopServer() {
        server.stop()
        Log.i(TAG, "Server was stopped")
        toast("Server was stopped")
    }

    override fun onResume() {
        super.onResume()
        restartServer()
    }

    override fun onPause() {
        super.onPause()
        stopServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayer.dispose()
    }
}
