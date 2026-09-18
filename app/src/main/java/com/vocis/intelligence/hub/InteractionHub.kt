package com.vocis.intelligence.hub

import com.vocis.core.data.database.AppDatabase
import com.vocis.core.data.entity.InteractionEntity
import com.vocis.core.data.entity.SecurityIncidentEntity
import com.vocis.core.domain.model.EventType
import com.vocis.core.domain.model.IncidentType
import com.vocis.core.domain.model.ProtectionAction
import com.vocis.core.domain.model.RiskLevel
import com.vocis.core.domain.model.SecurityEvent
import com.vocis.intelligence.context.AttackContextEngine
import com.vocis.intelligence.identity.CallerIdentity
import com.vocis.intelligence.identity.CallerIdentityResolver
import com.vocis.intelligence.identity.ReputationLevel
import com.vocis.intelligence.linguistic.LocalScamClassifier
import com.vocis.intelligence.linguistic.NotificationSignalExtractor
import com.vocis.intelligence.linguistic.ScamClassification
import com.vocis.intelligence.linguistic.SmsSignalExtractor
import com.vocis.emergency.FamilyAlertDispatcher
import com.vocis.intelligence.incident.SecurityIncidentManager
import com.vocis.intelligence.policy.ProtectionPolicyEngine
import com.vocis.intelligence.risk.EvidenceFusionEngine
import com.vocis.intelligence.risk.RiskEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InteractionHub(
    private val db: AppDatabase? = null,
    private val identityResolver: CallerIdentityResolver,
    private val contextEngine: AttackContextEngine,
    private val fusionEngine: EvidenceFusionEngine = EvidenceFusionEngine(),
    private val policyEngine: ProtectionPolicyEngine = ProtectionPolicyEngine(db?.protectionPolicyDao()),
    private val incidentManager: SecurityIncidentManager = SecurityIncidentManager(db?.securityIncidentDao()),
    private val familyAlertDispatcher: FamilyAlertDispatcher = FamilyAlertDispatcher(db?.familyContactDao()),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _interactions = MutableStateFlow<List<InteractionEntity>>(emptyList())
    val interactions: StateFlow<List<InteractionEntity>> = _interactions.asStateFlow()

    val activeIncident: StateFlow<SecurityIncidentEntity?> = incidentManager.activeIncident

    @Volatile
    private var isVoiceCloneCritical: Boolean = false

    fun onVoiceCloneVerdict(isCritical: Boolean) {
        this.isVoiceCloneCritical = isCritical
    }

    fun processEventAsync(event: SecurityEvent) {
        scope.launch {
            processEvent(event)
        }
    }

    suspend fun processEvent(event: SecurityEvent): InteractionEntity {
        return try {
            handleEventInternal(event)
        } catch (e: Throwable) {
            // Fail-open invariant: Never block or crash on exception, allow communication
            createFailOpenInteraction(event, e.message ?: "Unknown error")
        }
    }

    private suspend fun handleEventInternal(event: SecurityEvent): InteractionEntity {
        // 1. Resolve Identity
        val identity = when (event.type) {
            EventType.INCOMING_CALL, EventType.OUTGOING_CALL, EventType.SMS_RECEIVED -> {
                identityResolver.resolve(event.identity)
            }
            else -> {
                CallerIdentity(
                    phoneNumber = event.identity,
                    displayName = null,
                    reputationLevel = ReputationLevel.NEUTRAL,
                    isKnownContact = false,
                    source = "system"
                )
            }
        }

        // 2. Extract linguistic signals and update attack context
        var scamClassification: ScamClassification? = null

        when (event.type) {
            EventType.SMS_RECEIVED -> {
                val smsSignals = SmsSignalExtractor.extractAll(event.metadata)
                contextEngine.onSmsReceived(event, smsSignals)
                scamClassification = LocalScamClassifier.classify(event.metadata)
            }
            EventType.NOTIFICATION_POSTED -> {
                val notifSignals = NotificationSignalExtractor.extractAll(
                    category = null,
                    actions = emptyList(),
                    packageName = event.identity,
                    title = "",
                    text = event.metadata
                )
                contextEngine.onNotificationEvent(event, notifSignals)
                scamClassification = LocalScamClassifier.classify(event.metadata)
            }
            EventType.INCOMING_CALL -> {
                contextEngine.onCallStarted(event)
            }
            EventType.OUTGOING_CALL -> {
                // Outgoing call start
            }
            EventType.PACKAGE_ADDED -> {
                if (NotificationSignalExtractor.isRemoteDesktopActive(event.identity)) {
                    contextEngine.onRemoteDesktopDetected(event)
                }
            }
            EventType.SYSTEM_EVENT -> {
                // Handle lifecycle reset if needed
            }
        }

        // 3. Obtain attack context snapshot
        val attackContext = contextEngine.getActiveContext(event.timestamp)

        // 4. Evidence Fusion & Risk Calculation
        val assessment = fusionEngine.evaluate(
            identity = identity,
            context = attackContext,
            scamClassification = scamClassification,
            isVoiceCloneCritical = isVoiceCloneCritical
        )

        // 5. Protection Policy Decision
        val protectionAction = policyEngine.decide(
            riskScore = assessment.score,
            riskLevel = assessment.level,
            callerIdentity = identity
        )

        // 6. Security Incident reporting if score >= 50 or action is blocking
        val incidentType = determineIncidentType(scamClassification, attackContext)
        val interactionId = event.interactionId ?: event.id

        if (assessment.score >= 50 || protectionAction == ProtectionAction.BLOCK_CALL) {
            incidentManager.reportThreat(
                riskScore = assessment.score,
                riskLevel = assessment.level,
                incidentType = incidentType,
                interactionId = interactionId,
                callerIdentity = identity,
                evidenceFactors = assessment.factors
            )
        }

        // Phase 14: Emergency Family Alert Dispatcher strictly on score > 50
        if (assessment.score > 50) {
            scope.launch {
                familyAlertDispatcher.sendAlert(
                    riskScore = assessment.score,
                    incidentType = incidentType,
                    callerNumber = identity.phoneNumber,
                    interactionId = interactionId
                )
            }
        }

        // 7. Assemble Interaction Entity
        val interaction = InteractionEntity(
            id = interactionId,
            title = "${event.type.name}: ${identity.displayName ?: identity.phoneNumber}",
            timestamp = formatTimestamp(event.timestamp),
            timestampMs = event.timestamp,
            riskLevel = assessment.level,
            summary = assessment.factors.joinToString(separator = "; ") { it.description },
            callerPhoneNumber = identity.phoneNumber,
            callerDisplayName = identity.displayName ?: "",
            callerIdentityType = identity.source,
            callerIdentitySource = identity.source,
            repLevel = identity.reputationLevel.name,
            groqIsScam = scamClassification?.isScam ?: false,
            groqScamScore = scamClassification?.scamScore ?: 0,
            groqScamCategory = scamClassification?.scamCategory ?: "",
            groqUrgencyTactics = scamClassification?.urgencyTactics?.joinToString() ?: "",
            groqAnalysisRationale = scamClassification?.rationale ?: "",
            protectionDecision = protectionAction,
            incidentType = incidentType,
            isBlocked = (protectionAction == ProtectionAction.BLOCK_CALL)
        )

        // 8. Persist and emit
        db?.interactionDao()?.insert(interaction)
        updateInteractionsState(interaction)

        return interaction
    }

    private fun determineIncidentType(
        scam: ScamClassification?,
        context: com.vocis.intelligence.context.AttackContext
    ): IncidentType {
        return when {
            context.remoteDesktopActive -> IncidentType.REMOTE_ACCESS_SCAM
            context.otpWithinWindow -> IncidentType.OTP_THEFT
            scam?.scamCategory == "DIGITAL_ARREST" -> IncidentType.DIGITAL_ARREST
            scam?.scamCategory == "REMOTE_ACCESS_SCAM" -> IncidentType.REMOTE_ACCESS_SCAM
            scam?.scamCategory == "OTP_THEFT" -> IncidentType.OTP_THEFT
            scam?.scamCategory == "FINANCIAL_FRAUD" -> IncidentType.FINANCIAL_FRAUD
            isVoiceCloneCritical -> IncidentType.VOICE_CLONE
            else -> IncidentType.OTHER
        }
    }

    private fun updateInteractionsState(newInteraction: InteractionEntity) {
        val current = _interactions.value.toMutableList()
        val index = current.indexOfFirst { it.id == newInteraction.id }
        if (index >= 0) {
            current[index] = newInteraction
        } else {
            current.add(0, newInteraction)
        }
        _interactions.value = current
    }

    private fun createFailOpenInteraction(event: SecurityEvent, reason: String): InteractionEntity {
        return InteractionEntity(
            id = event.interactionId ?: event.id,
            title = "${event.type.name}: ${event.identity}",
            timestamp = formatTimestamp(event.timestamp),
            timestampMs = event.timestamp,
            riskLevel = RiskLevel.LOW,
            summary = "Fail-open fallback triggered: $reason",
            callerPhoneNumber = event.identity,
            protectionDecision = ProtectionAction.MONITOR_ONLY,
            isBlocked = false
        )
    }

    private fun formatTimestamp(timestampMs: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return sdf.format(Date(timestampMs))
    }

    companion object {
        @Volatile
        private var instance: InteractionHub? = null

        fun getInstance(context: android.content.Context): InteractionHub {
            return instance ?: synchronized(this) {
                instance ?: createDefault(context.applicationContext).also { instance = it }
            }
        }

        fun setInstance(hub: InteractionHub) {
            instance = hub
        }

        private fun createDefault(context: android.content.Context): InteractionHub {
            val db = AppDatabase.getInstance(context)
            val identityResolver = CallerIdentityResolver(
                context = context,
                dao = db.callerIdentityDao()
            )
            val contextEngine = AttackContextEngine(dao = db.attackContextDao())
            val incidentManager = SecurityIncidentManager(dao = db.securityIncidentDao())
            val policyEngine = ProtectionPolicyEngine(dao = db.protectionPolicyDao())
            return InteractionHub(
                db = db,
                identityResolver = identityResolver,
                contextEngine = contextEngine,
                fusionEngine = EvidenceFusionEngine(),
                policyEngine = policyEngine,
                incidentManager = incidentManager
            )
        }
    }
}

