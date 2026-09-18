package com.vocis.intelligence.context

import com.vocis.core.data.dao.AttackContextDao
import com.vocis.core.data.entity.AttackContextEntity
import com.vocis.core.domain.model.IncidentType
import com.vocis.core.domain.model.SecurityEvent
import com.vocis.intelligence.linguistic.NotificationSignals
import com.vocis.intelligence.linguistic.SmsSignals
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class AttackContext(
    val hasActiveOtp: Boolean,
    val otpWithinWindow: Boolean,
    val remoteDesktopActive: Boolean,
    val phishingLinkDetected: Boolean,
    val activeCallPhoneNumber: String?,
    val windowStartMs: Long
)

class AttackContextEngine(
    private val dao: AttackContextDao? = null
) {
    companion object {
        const val WINDOW_MS: Long = 300_000L // 5 minutes
    }

    private val mutex = Mutex()

    private var hasActiveOtp: Boolean = false
    private var otpSender: String = ""
    private var otpTimestamp: Long = 0L

    private var hasActiveCall: Boolean = false
    private var activeCallPhoneNumber: String? = null
    private var primaryInteractionId: String? = null
    private var windowStartMs: Long = 0L

    private var remoteDesktopActive: Boolean = false
    private var phishingLinkDetected: Boolean = false

    private val contributingEventIds = mutableListOf<String>()

    suspend fun onSmsReceived(event: SecurityEvent, signals: SmsSignals) = mutex.withLock {
        contributingEventIds.add(event.id)
        if (signals.hasOtp) {
            hasActiveOtp = true
            otpSender = event.identity
            otpTimestamp = event.timestamp
        }
        if (signals.phishingLinks.isNotEmpty()) {
            phishingLinkDetected = true
        }

        // If OTP received during an active call, persist suspicious correlation
        if (hasActiveCall && signals.hasOtp) {
            persistContext(
                patternType = IncidentType.OTP_THEFT,
                description = "OTP received during active call from $activeCallPhoneNumber"
            )
        }
    }

    suspend fun onCallStarted(event: SecurityEvent) = mutex.withLock {
        contributingEventIds.add(event.id)
        hasActiveCall = true
        activeCallPhoneNumber = event.identity
        primaryInteractionId = event.interactionId ?: event.id
        windowStartMs = event.timestamp

        val now = event.timestamp
        val otpValid = hasActiveOtp && (now - otpTimestamp) < WINDOW_MS
        if (otpValid) {
            persistContext(
                patternType = IncidentType.OTP_THEFT,
                description = "Call started within 5 minutes of received OTP from $otpSender"
            )
        }
        if (remoteDesktopActive) {
            persistContext(
                patternType = IncidentType.REMOTE_ACCESS_SCAM,
                description = "Call started while remote desktop tool is running"
            )
        }
    }

    suspend fun onCallEnded() = mutex.withLock {
        hasActiveCall = false
        activeCallPhoneNumber = null
        // ponytail: call state cleared; OTP state retained for its own 5-min lifecycle
    }

    suspend fun onRemoteDesktopDetected(event: SecurityEvent? = null) = mutex.withLock {
        remoteDesktopActive = true
        event?.let { contributingEventIds.add(it.id) }
        if (hasActiveCall) {
            persistContext(
                patternType = IncidentType.REMOTE_ACCESS_SCAM,
                description = "Remote desktop tool activated during active call"
            )
        }
    }

    suspend fun onNotificationEvent(event: SecurityEvent, signals: NotificationSignals) = mutex.withLock {
        contributingEventIds.add(event.id)
        if (signals.isRemoteDesktop) {
            remoteDesktopActive = true
            if (hasActiveCall) {
                persistContext(
                    patternType = IncidentType.REMOTE_ACCESS_SCAM,
                    description = "Remote desktop notification detected during active call"
                )
            }
        }
    }

    suspend fun getActiveContext(nowMs: Long = System.currentTimeMillis()): AttackContext = mutex.withLock {
        val otpWithinWindow = hasActiveOtp && (nowMs - otpTimestamp) < WINDOW_MS
        AttackContext(
            hasActiveOtp = hasActiveOtp,
            otpWithinWindow = otpWithinWindow,
            remoteDesktopActive = remoteDesktopActive,
            phishingLinkDetected = phishingLinkDetected,
            activeCallPhoneNumber = if (hasActiveCall) activeCallPhoneNumber else null,
            windowStartMs = windowStartMs
        )
    }

    suspend fun reset() = mutex.withLock {
        hasActiveOtp = false
        otpSender = ""
        otpTimestamp = 0L
        hasActiveCall = false
        activeCallPhoneNumber = null
        primaryInteractionId = null
        windowStartMs = 0L
        remoteDesktopActive = false
        phishingLinkDetected = false
        contributingEventIds.clear()
    }

    private suspend fun persistContext(patternType: IncidentType, description: String) {
        val currentDao = dao ?: return
        val entity = AttackContextEntity(
            contextId = UUID.randomUUID().toString(),
            patternType = patternType,
            triggeredAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + WINDOW_MS,
            primaryInteractionId = primaryInteractionId ?: UUID.randomUUID().toString(),
            contributingEventIds = contributingEventIds.joinToString(","),
            isActive = true,
            description = description
        )
        try {
            currentDao.insert(entity)
        } catch (_: Exception) {
            // Non-critical cache failure
        }
    }
}
