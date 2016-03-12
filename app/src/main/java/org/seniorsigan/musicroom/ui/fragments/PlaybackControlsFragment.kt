package org.seniorsigan.musicroom.ui.fragments

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.find
import org.jetbrains.anko.image
import org.jetbrains.anko.onClick
import org.seniorsigan.musicroom.*
import org.seniorsigan.musicroom.services.MusicService
import org.seniorsigan.musicroom.ui.NowPlayingActivity

class PlaybackControlsFragment : Fragment() {
    private lateinit var coverView: ImageView
    private lateinit var titleView: TextView
    private lateinit var artistView: TextView
    private lateinit var button: ImageButton

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShowTrack(track: Track) {
        Log.i(TAG, "Received $track")
        drawTrackView(track)
    }

    @Subscribe
    fun onAudioPlayed(msg: AudioPlayedMessage) {
        Log.i(TAG, "Finished playing")
        clearTrackView()
    }

    fun drawTrackView(track: Track?) {
        if (track != null) {
            titleView.text = track.title
            artistView.text = track.artist
            coverView.image = BitmapDrawable(resources, track.cover)
            button.setImageResource(R.drawable.ic_stop_black_24dp)
        } else {
            clearTrackView()
        }
    }

    private fun clearTrackView() {
        titleView.text = "Unknown title"
        artistView.text = "Unknown artist"
        coverView.image = App.defaults.cover
        button.setImageResource(R.drawable.ic_play_arrow_black_24dp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "PlaybackFragment onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "PlaybackFragment onCreateView")
        val rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false)
        rootView.onClick {
            Log.d(TAG, "Click on fragment")
            startActivity(Intent(activity, NowPlayingActivity::class.java))
        }

        with(rootView, {
            coverView = find<ImageView>(R.id.media_album_art)
            titleView = find<TextView>(R.id.media_title)
            artistView = find<TextView>(R.id.media_artist)
            button = find<ImageButton>(R.id.media_play_pause)
        })

        val stopIntent = Intent(context, MusicService::class.java)
        stopIntent.action = MusicService.ACTION_STOP
        button.onClick {
            context.startService(stopIntent)
        }

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "PlaybackFragment onAttach")
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        drawTrackView(App.queue.current())
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
