package org.seniorsigan.musicroom

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import org.jetbrains.anko.toast
import java.util.*

class ServerService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private lateinit var notifications: Notifications
    private lateinit var server: Server

    override fun onCreate() {
        super.onCreate()
        server = Server(assets)
        notifications = Notifications(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        restartServer()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop()
        Log.d(TAG, "ServerService destroyed")
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

        val address = "Listen to http://$formattedIpAddress:${Server.PORT}"
        toast(address)
        Log.i(TAG, address)
        val notification = notifications.serverNotification("http://$formattedIpAddress:${Server.PORT}")
        startForeground(Notifications.SERVER_NOTIFICATION_ID, notification)
    }
}