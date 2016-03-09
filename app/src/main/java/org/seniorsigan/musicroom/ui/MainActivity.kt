package org.seniorsigan.musicroom.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import org.seniorsigan.musicroom.R
import org.seniorsigan.musicroom.TAG
import org.seniorsigan.musicroom.services.ServerService

class MainActivity : AppCompatActivity() {
    private lateinit var addVkButton: Button

    fun checkVkButton() {
        Log.d(TAG, "Check vk login")
        if (VKSdk.isLoggedIn()) {
            Log.d(TAG, "Logged in VK")
            addVkButton.visibility = View.GONE
        } else {
            Log.d(TAG, "Not logged in VK")
            addVkButton.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity onStop")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "MainActivity onStart")
        startService(Intent(this, ServerService::class.java))
        checkVkButton()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")
        setContentView(R.layout.activity_main)
        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        addVkButton = find<Button>(R.id.button_add_vk)
        addVkButton.onClick { VKSdk.login(this, "audio", "offline") }
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
        if (id == R.id.action_vk_logout) {
            VKSdk.logout()
            checkVkButton()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Get results: $resultCode")

        Log.d(TAG, "Receive data $data")
        if (VKSdk.onActivityResult(requestCode, resultCode, data ?: Intent(), object : VKCallback<VKAccessToken> {
            override fun onError(err: VKError?) {
                Log.e(TAG, err?.errorMessage ?: "Unknown error")
            }

            override fun onResult(token: VKAccessToken?) {
                Log.d(TAG, "Vk user: ${token?.userId}")
                addVkButton.visibility = View.GONE
            }
        })) {
            checkVkButton()
        }
    }
}
