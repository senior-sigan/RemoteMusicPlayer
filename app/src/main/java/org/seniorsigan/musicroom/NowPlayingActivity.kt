package org.seniorsigan.musicroom

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.find
import org.jetbrains.anko.image

class NowPlayingActivity : AppCompatActivity() {
    private lateinit var coverView: ImageView
    private lateinit var titleView: TextView
    private lateinit var artistView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)
        coverView = find<ImageView>(R.id.coverView)
        titleView = find<TextView>(R.id.titleText)
        artistView = find<TextView>(R.id.artistText)
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
        coverView.image = resources.getDrawable(
                R.drawable.default_album_art_big_card, theme)
    }

}
