package org.seniorsigan.musicroom

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShowTrack(track: Track) {
        Log.i(TAG, "Received $track")
        startActivity(Intent(this, NowPlayingActivity::class.java))
    }

    @Subscribe
    fun onAudioPlayed(msg: AudioPlayedMessage) {
        Log.i(TAG, "Finished playing")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity onStop")
        EventBus.getDefault().unregister(this)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "MainActivity onStart")
        EventBus.getDefault().register(this)
        startService(Intent(this, ServerService::class.java))
        val track = App.queue.current()
        if (track != null) {
            onShowTrack(track)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity onDestroy")
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
