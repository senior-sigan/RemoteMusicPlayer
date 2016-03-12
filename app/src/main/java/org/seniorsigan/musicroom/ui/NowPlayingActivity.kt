package org.seniorsigan.musicroom.ui

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.find
import org.jetbrains.anko.image
import org.seniorsigan.musicroom.*

class NowPlayingActivity : AppCompatActivity() {
    private lateinit var coverView: ImageView
    private lateinit var titleView: TextView
    private lateinit var artistView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        coverView = find<ImageView>(R.id.coverView)
        titleView = find<TextView>(R.id.titleText)
        artistView = find<TextView>(R.id.artistText)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShowTrack(track: Track) {
        Log.i(TAG, "Received $track")
        titleView.text = track.title
        artistView.text = track.artist
        coverView.image = BitmapDrawable(resources, track.cover)
    }

    @Subscribe
    fun onAudioPlayed(msg: AudioPlayedMessage) {
        Log.i(TAG, "Finished playing")
        clearView()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        val track = App.queue.current()
        if (track == null) {
            clearView()
        } else {
            onShowTrack(track)
        }
    }

    private fun clearView() {
        titleView.text = "Unknown title"
        artistView.text = "Unknown artist"
        coverView.image = App.defaults.cover
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d(TAG, "NowPlaying option selected: ${item?.itemId}")
        return when(item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
