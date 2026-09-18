package com.vocis.webrtc.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Android Foreground Service holding WebRTC VoIP call lifecycle and audio wake-locks.
 */
class VoipCallService : Service() {

    companion object {
        const val CHANNEL_ID = "vocis_voip_channel"
        const val NOTIFICATION_ID = 2002
        const val EXTRA_PEER_NAME = "extra_peer_name"

        fun start(context: Context, peerName: String) {
            val intent = Intent(context, VoipCallService::class.java).apply {
                putExtra(EXTRA_PEER_NAME, peerName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, VoipCallService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val peerName = intent?.getStringExtra(EXTRA_PEER_NAME) ?: "VOCIS Peer"
        val notification = buildForegroundNotification(peerName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VOCIS VoIP Call",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing encrypted peer-to-peer VoIP call"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(peerName: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VOCIS Encrypted Call")
            .setContentText("Connected to $peerName")
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
