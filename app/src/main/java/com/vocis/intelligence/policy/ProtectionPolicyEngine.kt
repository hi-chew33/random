package com.vocis.intelligence.policy

import com.vocis.core.data.dao.ProtectionPolicyDao
import com.vocis.core.data.entity.ProtectionPolicyEntity
import com.vocis.core.domain.model.ProtectionAction
import com.vocis.core.domain.model.ProtectionMode
import com.vocis.core.domain.model.RiskLevel
import com.vocis.intelligence.identity.CallerIdentity

data class CallScreeningDecision(
    val action: ProtectionAction,
    val disallowCall: Boolean,
    val rejectCall: Boolean,
    val silenceCall: Boolean,
    val skipCallLog: Boolean
)

class ProtectionPolicyEngine(
    private val dao: ProtectionPolicyDao? = null
) {
    fun defaultPolicy(): ProtectionPolicyEntity {
        return ProtectionPolicyEntity(
            policyId = "default_policy",
            mode = ProtectionMode.BALANCED,
            lowRiskBehavior = ProtectionAction.MONITOR_ONLY,
            elevatedRiskBehavior = ProtectionAction.SHOW_COMPACT_WARNING,
            highRiskBehavior = ProtectionAction.SHOW_RISK_CARD,
            criticalRiskBehavior = ProtectionAction.BLOCK_CALL,
            unknownCallerBehavior = ProtectionAction.MONITOR_ONLY,
            autoBlockCritical = true
        )
    }

    suspend fun decide(
        riskScore: Int,
        riskLevel: RiskLevel,
        callerIdentity: CallerIdentity,
        policyOverride: ProtectionPolicyEntity? = null
    ): ProtectionAction {
        // 1. Whitelist precedence: known contacts are never blocked or warned aggressively
        if (callerIdentity.isKnownContact) {
            return ProtectionAction.MONITOR_ONLY
        }

        val policy = policyOverride ?: dao?.getPolicy() ?: defaultPolicy()

        // 2. Critical override rule: autoBlockCritical forces BLOCK_CALL regardless of mode
        if (policy.autoBlockCritical && riskLevel == RiskLevel.CRITICAL) {
            return ProtectionAction.BLOCK_CALL
        }

        // 3. Mode and Risk-level specific behavior
        return when (riskLevel) {
            RiskLevel.LOW -> policy.lowRiskBehavior
            RiskLevel.ELEVATED -> policy.elevatedRiskBehavior
            RiskLevel.HIGH -> policy.highRiskBehavior
            RiskLevel.CRITICAL -> policy.criticalRiskBehavior
        }
    }

    fun toScreeningDecision(action: ProtectionAction): CallScreeningDecision {
        return when (action) {
            ProtectionAction.BLOCK_CALL -> CallScreeningDecision(
                action = action,
                disallowCall = true,
                rejectCall = true,
                silenceCall = false,
                skipCallLog = true
            )
            ProtectionAction.SHOW_SECURITY_INTERVENTION,
            ProtectionAction.SHOW_RISK_CARD -> CallScreeningDecision(
                action = action,
                disallowCall = false,
                rejectCall = false,
                silenceCall = true,
                skipCallLog = false
            )
            ProtectionAction.SHOW_COMPACT_WARNING,
            ProtectionAction.MONITOR_ONLY -> CallScreeningDecision(
                action = action,
                disallowCall = false,
                rejectCall = false,
                silenceCall = false,
                skipCallLog = false
            )
        }
    }
}
