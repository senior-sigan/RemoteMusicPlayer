package org.seniorsigan.musicroom.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.app.Fragment
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.find
import org.jetbrains.anko.image
import org.jetbrains.anko.onClick
import org.seniorsigan.musicroom.*

class PlaybackControlsFragment : Fragment() {
    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var coverView: ImageView
    private lateinit var titleView: TextView
    private lateinit var artistView: TextView

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
        } else {
            clearTrackView()
        }
    }

    private fun clearTrackView() {
        titleView.text = "Unknown title"
        artistView.text = "Unknown artist"
        coverView.image = resources.getDrawable(
                R.drawable.default_album_art_big_card, context.theme)
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
        })

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "PlaybackFragment onAttach")
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
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
