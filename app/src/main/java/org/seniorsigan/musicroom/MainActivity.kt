package org.seniorsigan.musicroom

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.find
import org.jetbrains.anko.image
import org.jetbrains.anko.onClick
import org.jetbrains.anko.onUiThread

class MainActivity : AppCompatActivity() {
    private lateinit var coverView: ImageView
    private lateinit var titleView: TextView
    private lateinit var artistView: TextView
    private lateinit var playPauseBtn: ImageButton
    private lateinit var playback: Playback

    fun clearView() {
        titleView.text = "Unknown title"
        artistView.text = "Unknown artist"
        coverView.image = resources.getDrawable(
                R.drawable.default_album_art_big_card, theme)
        playPauseBtn.image = resources.getDrawable(
                android.R.drawable.ic_media_play, theme)
    }

    @Subscribe
    fun onAudioAdded(t: TrackForm) {
        val track = App.queue.current() ?: return
        Log.i(TAG, "Received $track")
        onUiThread {
            titleView.text = track.title
            artistView.text = track.artist
            coverView.image = resources.getDrawable(
                    R.drawable.default_album_art_big_card, theme)
            playPauseBtn.image = resources.getDrawable(
                    android.R.drawable.ic_media_pause, theme)
        }
        try {
            playback.play()

            App.coverSearch.search(track.title, track.artist, { url ->
                Log.i(TAG, url ?: "empty cover")
                if (url != null) {
                    onUiThread {
                        Picasso.with(applicationContext)
                                .load(url)
                                .placeholder(R.drawable.default_album_art_big_card)
                                .error(R.drawable.default_album_art_big_card)
                                .into(coverView)
                    }
                }
            })
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
        playback.stop(false)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        startService(Intent(this, ServerService::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        playback = Playback(baseContext)
        coverView = find<ImageView>(R.id.coverView)
        titleView = find<TextView>(R.id.titleText)
        artistView = find<TextView>(R.id.artistText)
        playPauseBtn = find<ImageButton>(R.id.playPauseButton)
        playPauseBtn.onClick {
            if (playback.isPlaying()) {
                playback.stop(true)
                clearView()
            } else {
                playPauseBtn.image = resources.getDrawable(
                        android.R.drawable.ic_media_play, theme)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playback.release()
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
        Log.d(TAG, "Get results: $resultCode")
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
