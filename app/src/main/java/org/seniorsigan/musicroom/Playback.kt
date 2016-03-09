package org.seniorsigan.musicroom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.PlaybackState
import android.os.PowerManager
import android.util.Log

// The volume we set the media player to when we lose audio focus, but are
// allowed to reduce the volume instead of stopping playback.
const val VOLUME_DUCK = 0.2f;
// The volume we set the media player when we have audio focus.
const val VOLUME_NORMAL = 1.0f;

// we don't have audio focus, and can't duck (play at a low volume)
const val AUDIO_NO_FOCUS_NO_DUCK = 0;
// we don't have focus, but can duck (play at a low volume)
const val AUDIO_NO_FOCUS_CAN_DUCK = 1;
// we have full audio focus
const val AUDIO_FOCUSED  = 2;


class Playback(
        private val context: Context,
        private val cb: Callback? = null
): AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    var state: Int = PlaybackState.STATE_NONE
    private var audioFocus: Int = 0
    private var mediaPlayer: MediaPlayer? = null
    private var playOnFocusGain: Boolean = false
    @Volatile private var currentPosition = 0
    @Volatile private var audioNoisyReceiverRegistered: Boolean = false

    private val audioNoisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val audioNoisyReceiver = object: BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            Log.i(TAG, "audioNoiseReceiver called!")
//            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent?.action)) {
//                Log.d(TAG, "Headphones disconnected.");
//                if (isPlaying()) {
//                    val i = Intent(context, MusicService::class.java)
//                    i.action = MusicService.ACTION_CMD
//                    i.putExtra(MusicService.CMD_NAME, MusicService.CMD_PAUSE)
//                    context.startService(i)
//                }
//            }
        }
    }
    /**
     * Called by AudioManager on audio focus changes.
     * Implementation of {@link android.media.AudioManager.OnAudioFocusChangeListener}
     */
    override fun onAudioFocusChange(focusChange: Int) {
        Log.d(TAG, "onAudioFocusChange. focusChange=$focusChange")
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // We have gained focus:
            audioFocus = AUDIO_FOCUSED

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // We have lost focus. If we can duck (low playback volume), we can keep playing.
            // Otherwise, we need to pause the playback.
            val canDuck = (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
            audioFocus = if (canDuck) AUDIO_NO_FOCUS_CAN_DUCK else AUDIO_NO_FOCUS_NO_DUCK

            // If we are playing, we need to reset media player by calling configMediaPlayerState
            // with mAudioFocus properly set.
            if (state == PlaybackState.STATE_PLAYING && !canDuck) {
                // If we don't have audio focus and can't duck, we save the information that
                // we were playing, so that we can resume playback once we get the focus back.
                playOnFocusGain = true
            }
        } else {
            Log.e(TAG, "onAudioFocusChange: Ignoring unsupported focusChange: $focusChange")
        }
        configMediaPlayerState()
    }

    /**
     * Called when media player is done playing current song.
     */
    override fun onCompletion(mp: MediaPlayer?) {
        Log.d(TAG, "onCompletion from MediaPlayer")
        cb?.onCompletion()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.e(TAG, "Media player error: what=$what, extra=$extra")
        return true
    }

    override fun onPrepared(mp: MediaPlayer?) {
        Log.d(TAG, "onPrepared from MediaPlayer")
        configMediaPlayerState()
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        currentPosition = mp!!.currentPosition
        if (state == PlaybackState.STATE_BUFFERING) {
            mediaPlayer?.start()
            state = PlaybackState.STATE_PLAYING
        }

        cb?.onPlaybackStatusChanged(state)
    }

    fun isPlaying(): Boolean {
        return playOnFocusGain || mediaPlayer?.isPlaying ?: false
    }

    fun getCurrentStreamPosition(): Int {
        return mediaPlayer?.currentPosition ?: currentPosition
    }

    fun updateLastKnownStreamPosition() {
        currentPosition = mediaPlayer?.currentPosition ?: currentPosition
    }

    fun pause() {
        if (state == PlaybackState.STATE_PLAYING) {
            // Pause media player and cancel the 'foreground service' state.
            if (mediaPlayer?.isPlaying ?: false) {
                mediaPlayer?.pause()
                currentPosition = mediaPlayer!!.currentPosition
            }
            // while paused, retain the MediaPlayer but give up audio focus
            relaxResources(false)
            giveUpAudioFocus()
        }
        state = PlaybackState.STATE_PAUSED

        cb?.onPlaybackStatusChanged(state)
        unregisterAudioNoisyReceiver()
    }

    fun play() {
        val source = App.queue.current()?.url ?: return
        playOnFocusGain = true
        tryToGetAudioFocus()
        registerAudioNoisyReceiver()
        currentPosition = 0

        if (state == PlaybackState.STATE_PAUSED && mediaPlayer != null) {
            configMediaPlayerState();
        } else {
            state = PlaybackState.STATE_STOPPED;
            relaxResources(false) // release everything except MediaPlayer

            try {
                createMediaPlayerIfNeeded()

                state = PlaybackState.STATE_BUFFERING

                mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer?.setDataSource(source)

                // Starts preparing the media player in the background. When
                // it's done, it will call our OnPreparedListener (that is,
                // the onPrepared() method on this class, since we set the
                // listener to 'this'). Until the media player is prepared,
                // we *cannot* call start() on it!
                mediaPlayer?.prepareAsync()

                cb?.onPlaybackStatusChanged(state)

            } catch (e: Exception) {
                Log.e(TAG, "Exception playing song: ${e.message}", e)
                cb?.onError(e.message ?: "Unknown exception playing song")
            }
        }
    }

    fun seekTo(position: Int) {
        Log.d(TAG, "seekTo called with $position")

        if (mediaPlayer == null) {
            // If we do not have a current media player, simply update the current position
            currentPosition = position
        } else {
            if (mediaPlayer?.isPlaying ?: false) {
                state = PlaybackState.STATE_BUFFERING
            }
            mediaPlayer?.seekTo(position)

            cb?.onPlaybackStatusChanged(state)

        }
    }

    fun stop(notifyListeners: Boolean) {
        state = PlaybackState.STATE_STOPPED;
        if (notifyListeners) {
            cb?.onPlaybackStatusChanged(state);
        }
        currentPosition = getCurrentStreamPosition()
        // Give up Audio focus
        giveUpAudioFocus()
        // Relax all resources
        relaxResources(true)
    }

    fun release() {
        relaxResources(true)
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and
     * starts/restarts it. This method starts/restarts the MediaPlayer
     * respecting the current audio focus state. So if we have focus, it will
     * play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is
     * allowed by the current focus settings. This method assumes mPlayer !=
     * null, so if you are calling it, you have to do so from a context where
     * you are sure this is the case.
     */
    private fun configMediaPlayerState() {
        Log.d(TAG, "configMediaPlayerState: audioFocus=$audioFocus")
        if (audioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            if (state == PlaybackState.STATE_PLAYING) {
                pause()
            }
        } else {
            if (audioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
                mediaPlayer?.setVolume(VOLUME_DUCK, VOLUME_DUCK)
            } else {
                mediaPlayer?.setVolume(VOLUME_NORMAL, VOLUME_NORMAL)
            }
        }
        if (playOnFocusGain) {
            if (mediaPlayer?.isPlaying != true) {
                if (currentPosition == mediaPlayer?.currentPosition) {
                    mediaPlayer?.start()
                    state = PlaybackState.STATE_PLAYING
                } else {
                    mediaPlayer?.seekTo(currentPosition)
                    state = PlaybackState.STATE_BUFFERING
                }
            }
            playOnFocusGain = false
        }

        cb?.onPlaybackStatusChanged(state)
    }

    private fun tryToGetAudioFocus() {
        Log.d(TAG, "tryToGetAudioFocus")
        if (audioFocus != AUDIO_FOCUSED) {
            val result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = AUDIO_FOCUSED;
            }
        }
    }

    private fun giveUpAudioFocus() {
        Log.d(TAG, "giveUpAudioFocus");
        if (audioFocus == AUDIO_FOCUSED) {
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = AUDIO_NO_FOCUS_NO_DUCK;
            }
        }
    }

    /**
     * Makes sure the media player exists and has been reset. This will create
     * the media player if needed, or reset the existing media player if one
     * already exists.
     */
    private fun createMediaPlayerIfNeeded() {
        Log.d(TAG, "createMediaPlayerIfNeed. need=${mediaPlayer == null}")
        if (mediaPlayer == null) {
            mediaPlayer = with(MediaPlayer(), {
                // Make sure the media player will acquire a wake-lock while
                // playing. If we don't do that, the CPU might go to sleep while the
                // song is playing, causing playback to stop.
                setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                // we want the media player to notify us when it's ready preparing,
                // and when it's done playing:
                setOnPreparedListener(this@Playback)
                setOnCompletionListener(this@Playback)
                setOnErrorListener(this@Playback)
                setOnSeekCompleteListener(this@Playback)
                this
            })
        } else {
            mediaPlayer!!.reset()
        }
    }

    /**
     * Releases resources used by the service for playback. This includes the
     * "foreground service" status, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also
     *            be released or not
     */
    private fun relaxResources(releaseMediaPlayer: Boolean) {
        Log.d(TAG, "relaxResources. releaseMediaPlayer=$releaseMediaPlayer");
        unregisterAudioNoisyReceiver()

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mediaPlayer != null) {
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    private fun registerAudioNoisyReceiver() {
        if (!audioNoisyReceiverRegistered) {
            context.registerReceiver(audioNoisyReceiver, audioNoisyIntentFilter);
            audioNoisyReceiverRegistered = true;
        }
    }

    private fun unregisterAudioNoisyReceiver() {
        if (audioNoisyReceiverRegistered) {
            context.unregisterReceiver(audioNoisyReceiver);
            audioNoisyReceiverRegistered = false;
        }
    }


    interface Callback {
        /**
         * On current music completed.
         */
        fun onCompletion()
        /**
         * on Playback status changed
         * Implementations can use this callback to update
         * playback state on the media sessions.
         */
        fun onPlaybackStatusChanged(state: Int)

        /**
         * @param error to be added to the PlaybackState
         */
        fun onError(error: String)

        /**
         * @param mediaId being currently played
         */
        fun setCurrentMediaId(mediaId: String)
    }
}