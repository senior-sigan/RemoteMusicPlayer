package org.seniorsigan.musicroom

import android.app.Service
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.toast
import org.jetbrains.anko.wifiManager
import java.util.*

class ServerService: Service() {
    @Volatile private var isRunning: Boolean = false
    private lateinit var notifications: Notifications
    private lateinit var server: Server
    val wifiLock: WifiManager.WifiLock by lazy {
        wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "serverService_lock")
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        server = Server(assets)
        notifications = Notifications(this)
        Log.d(TAG, "ServerService onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "ServerService receive action: $action")

        when(action) {
            ACTION_STOP -> {
                notificationManager.cancel(Notifications.SERVER_NOTIFICATION_ID)
                stopSelf()
            }
            else -> restartServer()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        server.stop()
        wifiLock.release()
        Log.d(TAG, "ServerService destroyed")
    }

    private fun restartServer() {
        synchronized(this, {
            if (isRunning) return
            wifiLock.acquire()
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
                return
            }

            val address = "Listen to http://$formattedIpAddress:${Server.PORT}"
            toast(address)
            Log.i(TAG, address)
            val notification = notifications.serverNotification("http://$formattedIpAddress:${Server.PORT}")
            startForeground(Notifications.SERVER_NOTIFICATION_ID, notification)
            isRunning = true
        })
    }

    companion object {
        val ACTION_STOP = "org.seniorsigan.musicroom.ACTION_STOP"
    }
}