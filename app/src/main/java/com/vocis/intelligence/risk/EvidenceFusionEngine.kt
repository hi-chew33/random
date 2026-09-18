package com.vocis.intelligence.risk

import com.vocis.core.domain.model.RiskLevel
import com.vocis.intelligence.context.AttackContext
import com.vocis.intelligence.context.ContextType
import com.vocis.intelligence.identity.CallerIdentity
import com.vocis.intelligence.identity.ReputationLevel
import com.vocis.intelligence.identity.baseRiskWeight
import com.vocis.intelligence.linguistic.ScamClassification
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

data class EvidenceFactor(
    val source: String,
    val weight: Int,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class RiskAssessment(
    val score: Int,
    val level: RiskLevel,
    val factors: List<EvidenceFactor>,
    val inputHash: String,
    val evaluatedAt: Long = System.currentTimeMillis()
)

class EvidenceFusionEngine {
    private val assessmentCache = ConcurrentHashMap<String, RiskAssessment>()

    fun buildFactors(
        identity: CallerIdentity,
        context: AttackContext,
        scamClassification: ScamClassification? = null,
        isVoiceCloneCritical: Boolean = false
    ): List<EvidenceFactor> {
        val factors = mutableListOf<EvidenceFactor>()

        // 1. Caller Identity reputation weight
        val baseRepWeight = identity.baseRiskWeight()
        factors.add(
            EvidenceFactor(
                source = "CALLER_REPUTATION",
                weight = baseRepWeight,
                description = "Caller reputation '${identity.reputationLevel}' for ${identity.phoneNumber}"
            )
        )

        // 2. Anonymous / private caller
        if (identity.phoneNumber == "UNKNOWN" || identity.phoneNumber.isBlank()) {
            factors.add(
                EvidenceFactor(
                    source = "ANONYMOUS_CALLER",
                    weight = 25,
                    description = "Caller identity hidden or presentation private"
                )
            )
        }

        // 3. Active OTP within 5-minute window
        if (context.otpWithinWindow) {
            factors.add(
                EvidenceFactor(
                    source = "OTP_IN_WINDOW",
                    weight = 40,
                    description = "Valid OTP message received within active 5-minute correlation window"
                )
            )
        }

        // 4. Remote Desktop tool active
        if (context.remoteDesktopActive) {
            factors.add(
                EvidenceFactor(
                    source = "REMOTE_DESKTOP_ACTIVE",
                    weight = 60,
                    description = "Remote desktop sharing application (AnyDesk/TeamViewer/RustDesk) is active"
                )
            )
        }

        // 5. Phishing link detected in recent SMS
        if (context.phishingLinkDetected) {
            factors.add(
                EvidenceFactor(
                    source = "PHISHING_LINK_DETECTED",
                    weight = 30,
                    description = "Suspicious URL or raw IP link identified in incoming SMS"
                )
            )
        }

        // 6. Callback number mismatch (TriNetra Attack Context)
        if (context.hasCallbackMismatch) {
            factors.add(
                EvidenceFactor(
                    source = "CALLBACK_MISMATCH",
                    weight = 30,
                    description = "Suspicious callback mismatch: SMS advertised callback ${context.callbackNumber} but incoming caller is ${context.activeCallPhoneNumber}"
                )
            )
        }

        // 7. Composite Correlated Attack Context (TriNetra AttackContextEngine)
        if (context.contextType != ContextType.UNKNOWN && context.contextType != ContextType.OTP_THEFT && context.contextType != ContextType.REMOTE_ACCESS_SCAM) {
            val weight = when (context.contextType) {
                ContextType.GOVERNMENT_IMPERSONATION -> 50
                ContextType.PARCEL_SCAM -> 35
                ContextType.TELECOM_IMPERSONATION -> 35
                ContextType.UTILITY_SCAM -> 30
                ContextType.FINANCIAL_FRAUD -> 25
                ContextType.SOCIAL_ENGINEERING -> 25
                else -> 20
            }
            factors.add(
                EvidenceFactor(
                    source = "CORRELATED_CONTEXT_${context.contextType.name}",
                    weight = weight,
                    description = "Correlated 5-min multi-event threat: ${context.explanation}"
                )
            )
        }

        // 8. Linguistic scam / urgency classification
        scamClassification?.let { scam ->
            if (scam.isScam) {
                val weight = if (scam.scamScore >= 80) 40 else 20
                factors.add(
                    EvidenceFactor(
                        source = "SCAM_CLASSIFICATION_${scam.scamCategory}",
                        weight = weight,
                        description = scam.rationale
                    )
                )
            }
            if (scam.urgencyTactics.isNotEmpty()) {
                factors.add(
                    EvidenceFactor(
                        source = "URGENCY_COERCION",
                        weight = 20,
                        description = "High urgency coercion tactics detected: ${scam.urgencyTactics.joinToString()}"
                    )
                )
            }
        }

        // 9. Voice Clone verdict from TriNetra VCD
        if (isVoiceCloneCritical) {
            factors.add(
                EvidenceFactor(
                    source = "VOICE_CLONE_CRITICAL",
                    weight = 60,
                    description = "Acoustic deepfake synthesis confidence threshold exceeded"
                )
            )
        }

        return factors
    }

    fun calculateInputHash(factors: List<EvidenceFactor>): String {
        val sortedKeys = factors.map { "${it.source}:${it.weight}" }.sorted().joinToString(";")
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(sortedKeys.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun fuse(factors: List<EvidenceFactor>): Int {
        return factors.sumOf { it.weight }
    }

    fun evaluate(
        identity: CallerIdentity,
        context: AttackContext,
        scamClassification: ScamClassification? = null,
        isVoiceCloneCritical: Boolean = false
    ): RiskAssessment {
        val factors = buildFactors(identity, context, scamClassification, isVoiceCloneCritical)
        val hash = calculateInputHash(factors)

        return assessmentCache.computeIfAbsent(hash) {
            val rawSum = fuse(factors)
            val score = RiskEngine.score(rawSum)
            val level = RiskEngine.band(score)
            RiskAssessment(
                score = score,
                level = level,
                factors = factors,
                inputHash = hash
            )
        }
    }

    fun invalidateCache() {
        assessmentCache.clear()
    }
}

object RiskEngine {
    fun score(fusedWeight: Int): Int = fusedWeight.coerceIn(0, 100)

    fun band(score: Int): RiskLevel = RiskLevel.fromScore(score)
}
