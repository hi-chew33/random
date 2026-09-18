package com.vocis

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.vocis.core.data.database.AppDatabase
import com.vocis.core.data.database.VcdDatabase
import com.vocis.digitalarrest.DigitalArrestController
import com.vocis.intelligence.context.AttackContextEngine
import com.vocis.intelligence.hub.InteractionHub
import com.vocis.intelligence.identity.CallerIdentityResolver
import com.vocis.ui.overlay.EmergencyAlertOverlayManager
import com.vocis.ui.overlay.ProtectionOverlayManager

class VocisApplication : Application() {

    lateinit var appDatabase: AppDatabase
        private set
    lateinit var vcdDatabase: VcdDatabase
        private set

    lateinit var callerIdentityResolver: CallerIdentityResolver
        private set
    lateinit var attackContextEngine: AttackContextEngine
        private set
    lateinit var interactionHub: InteractionHub
        private set
    lateinit var digitalArrestController: DigitalArrestController
        private set
    lateinit var protectionOverlayManager: ProtectionOverlayManager
        private set
    lateinit var emergencyAlertOverlayManager: EmergencyAlertOverlayManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Initialize dual databases
        appDatabase = AppDatabase.getInstance(this)
        vcdDatabase = VcdDatabase.getInstance(this)

        // 2. Initialize intelligence and security components
        callerIdentityResolver = CallerIdentityResolver(
            context = this,
            dao = appDatabase.callerIdentityDao()
        )
        attackContextEngine = AttackContextEngine(
            dao = appDatabase.attackContextDao()
        )
        interactionHub = InteractionHub(
            db = appDatabase,
            identityResolver = callerIdentityResolver,
            contextEngine = attackContextEngine
        )
        digitalArrestController = DigitalArrestController()
        protectionOverlayManager = ProtectionOverlayManager(this)
        emergencyAlertOverlayManager = EmergencyAlertOverlayManager(this)

        // 3. Register system notification channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val protectionChannel = NotificationChannel(
                CHANNEL_PROTECTION,
                "VOCIS Protection Active",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifies when real-time biometric and scam protection is active"
            }
            notificationManager.createNotificationChannel(protectionChannel)

            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "VOCIS Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical acoustic and biometric scam emergency alerts"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(emergencyChannel)
        }
    }

    companion object {
        const val CHANNEL_PROTECTION = "vocis_channel_protection"
        const val CHANNEL_EMERGENCY = "vocis_channel_emergency"

        lateinit var instance: VocisApplication
            private set
    }
}
