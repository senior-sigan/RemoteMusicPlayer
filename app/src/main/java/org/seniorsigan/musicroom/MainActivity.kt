package org.seniorsigan.musicroom

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
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
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
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
        restartServer()
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

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
        MusicPlayer.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        if (id == R.id.action_add_vk) {
            VKSdk.login(this, "audio", "offline")
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
                override fun onError(err: VKError?) {
                    Log.e(TAG, err?.errorMessage ?: "Unknown error")
                }

                override fun onResult(token: VKAccessToken?) {
                    Log.d(TAG, token?.email)
                }
            })
        }
    }
}
