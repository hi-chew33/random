package com.vocis

import com.vocis.core.data.entity.ProtectionPolicyEntity
import com.vocis.core.domain.model.ProtectionAction
import com.vocis.core.domain.model.ProtectionMode
import com.vocis.core.domain.model.RiskLevel
import com.vocis.intelligence.identity.CallerIdentity
import com.vocis.intelligence.identity.ReputationLevel
import com.vocis.intelligence.policy.ProtectionPolicyEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProtectionPolicyEngineTest {

    private lateinit var policyEngine: ProtectionPolicyEngine

    @Before
    fun setUp() {
        policyEngine = ProtectionPolicyEngine(dao = null)
    }

    @Test
    fun testKnownContactWhitelistPrecedence() = runBlocking {
        val knownIdentity = CallerIdentity(
            phoneNumber = "+919876543210",
            displayName = "Mom",
            reputationLevel = ReputationLevel.SAFE,
            isKnownContact = true,
            source = "contacts"
        )

        // Even under CRITICAL score, known contact is whitelisted
        val decision = policyEngine.decide(
            riskScore = 95,
            riskLevel = RiskLevel.CRITICAL,
            callerIdentity = knownIdentity
        )
        assertEquals(ProtectionAction.MONITOR_ONLY, decision)
    }

    @Test
    fun testAutoBlockCriticalOverride() = runBlocking {
        val strangerIdentity = CallerIdentity(
            phoneNumber = "+911234567890",
            displayName = null,
            reputationLevel = ReputationLevel.HIGH_RISK,
            isKnownContact = false,
            source = "mock"
        )

        val policy = ProtectionPolicyEntity(
            mode = ProtectionMode.PASSIVE,
            criticalRiskBehavior = ProtectionAction.SHOW_RISK_CARD, // Passive mode says show card
            autoBlockCritical = true // But autoBlockCritical flag is TRUE
        )

        val decision = policyEngine.decide(
            riskScore = 85,
            riskLevel = RiskLevel.CRITICAL,
            callerIdentity = strangerIdentity,
            policyOverride = policy
        )
        assertEquals(ProtectionAction.BLOCK_CALL, decision)
    }

    @Test
    fun testHighRiskBalancedMode() = runBlocking {
        val strangerIdentity = CallerIdentity(
            phoneNumber = "+911234567890",
            displayName = null,
            reputationLevel = ReputationLevel.SUSPICIOUS,
            isKnownContact = false,
            source = "mock"
        )

        val decision = policyEngine.decide(
            riskScore = 65,
            riskLevel = RiskLevel.HIGH,
            callerIdentity = strangerIdentity
        )
        assertEquals(ProtectionAction.SHOW_RISK_CARD, decision)
    }

    @Test
    fun testScreeningDecisionMapping() {
        val blockDecision = policyEngine.toScreeningDecision(ProtectionAction.BLOCK_CALL)
        assertTrue(blockDecision.disallowCall)
        assertTrue(blockDecision.rejectCall)
        assertTrue(blockDecision.skipCallLog)
        assertFalse(blockDecision.silenceCall)

        val riskCardDecision = policyEngine.toScreeningDecision(ProtectionAction.SHOW_RISK_CARD)
        assertFalse(riskCardDecision.disallowCall)
        assertFalse(riskCardDecision.rejectCall)
        assertTrue(riskCardDecision.silenceCall)

        val monitorDecision = policyEngine.toScreeningDecision(ProtectionAction.MONITOR_ONLY)
        assertFalse(monitorDecision.disallowCall)
        assertFalse(monitorDecision.silenceCall)
    }
}
