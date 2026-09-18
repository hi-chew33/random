package com.vocis.vcd.service

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
import com.vocis.vcd.LiveVerificationPipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android Foreground Service managing microphone capture and live Voice Clone Defence (VCD)
 * during cellular or speakerphone calls.
 */
class LiveVerificationService : Service() {

    companion object {
        const val CHANNEL_ID = "vocis_vcd_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "com.vocis.vcd.action.START_VERIFICATION"
        const val ACTION_STOP = "com.vocis.vcd.action.STOP_VERIFICATION"

        fun start(context: Context) {
            val intent = Intent(context, LiveVerificationService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LiveVerificationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val isRunning = AtomicBoolean(false)
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startVerification()
            ACTION_STOP -> stopVerification()
        }
        return START_NOT_STICKY
    }

    private fun startVerification() {
        if (isRunning.getAndSet(true)) return

        val notification = buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopVerification() {
        isRunning.set(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Clone Defence",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active voice biometric verification monitoring"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VOCIS Voice Clone Defence")
            .setContentText("Active voice biometric verification enabled")
            .setSmallIcon(android.R.drawable.stat_sys_speakerphone)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        isRunning.set(false)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
