package com.vocis

import com.vocis.core.domain.model.RiskLevel
import com.vocis.intelligence.context.AttackContext
import com.vocis.intelligence.identity.CallerIdentity
import com.vocis.intelligence.identity.ReputationLevel
import com.vocis.intelligence.risk.EvidenceFusionEngine
import com.vocis.intelligence.risk.RiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RiskEngineTest {

    private lateinit var fusionEngine: EvidenceFusionEngine

    @Before
    fun setUp() {
        fusionEngine = EvidenceFusionEngine()
    }

    @Test
    fun testRiskEngineClampingAndBanding() {
        assertEquals(100, RiskEngine.score(125))
        assertEquals(0, RiskEngine.score(-10))
        assertEquals(55, RiskEngine.score(55))

        assertEquals(RiskLevel.CRITICAL, RiskEngine.band(85))
        assertEquals(RiskLevel.CRITICAL, RiskEngine.band(75))
        assertEquals(RiskLevel.HIGH, RiskEngine.band(74))
        assertEquals(RiskLevel.HIGH, RiskEngine.band(50))
        assertEquals(RiskLevel.ELEVATED, RiskEngine.band(49))
        assertEquals(RiskLevel.ELEVATED, RiskEngine.band(25))
        assertEquals(RiskLevel.LOW, RiskEngine.band(24))
        assertEquals(RiskLevel.LOW, RiskEngine.band(0))
    }

    @Test
    fun testFusionEngineUnknownCallerWithOtpAndRemoteDesktop() {
        val identity = CallerIdentity(
            phoneNumber = "+919876543210",
            displayName = null,
            reputationLevel = ReputationLevel.UNKNOWN,
            isKnownContact = false,
            source = "mock"
        )
        val context = AttackContext(
            hasActiveOtp = true,
            otpWithinWindow = true,
            remoteDesktopActive = true,
            phishingLinkDetected = false,
            activeCallPhoneNumber = "+919876543210",
            windowStartMs = System.currentTimeMillis()
        )

        // Weights: UNKNOWN(15) + OTP(40) + RemoteDesktop(60) = 115 -> clamped to 100
        val assessment = fusionEngine.evaluate(identity, context)
        assertEquals(100, assessment.score)
        assertEquals(RiskLevel.CRITICAL, assessment.level)
        assertEquals(3, assessment.factors.size)
    }

    @Test
    fun testKnownContactSafeBaseline() {
        val identity = CallerIdentity(
            phoneNumber = "+919876543210",
            displayName = "Dad",
            reputationLevel = ReputationLevel.SAFE,
            isKnownContact = true,
            source = "contacts"
        )
        val context = AttackContext(
            hasActiveOtp = false,
            otpWithinWindow = false,
            remoteDesktopActive = false,
            phishingLinkDetected = false,
            activeCallPhoneNumber = "+919876543210",
            windowStartMs = System.currentTimeMillis()
        )

        val assessment = fusionEngine.evaluate(identity, context)
        assertEquals(0, assessment.score)
        assertEquals(RiskLevel.LOW, assessment.level)
    }

    @Test
    fun testInputHashChangesWithFactors() {
        val identity = CallerIdentity(
            phoneNumber = "+919876543210",
            displayName = null,
            reputationLevel = ReputationLevel.UNKNOWN,
            isKnownContact = false,
            source = "mock"
        )
        val contextWithoutOtp = AttackContext(
            hasActiveOtp = false,
            otpWithinWindow = false,
            remoteDesktopActive = false,
            phishingLinkDetected = false,
            activeCallPhoneNumber = "+919876543210",
            windowStartMs = System.currentTimeMillis()
        )
        val contextWithOtp = contextWithoutOtp.copy(hasActiveOtp = true, otpWithinWindow = true)

        val factors1 = fusionEngine.buildFactors(identity, contextWithoutOtp)
        val factors2 = fusionEngine.buildFactors(identity, contextWithOtp)

        val hash1 = fusionEngine.calculateInputHash(factors1)
        val hash2 = fusionEngine.calculateInputHash(factors2)

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun testAssessmentMemoization() {
        val identity = CallerIdentity(
            phoneNumber = "+919876543210",
            displayName = null,
            reputationLevel = ReputationLevel.SUSPICIOUS,
            isKnownContact = false,
            source = "mock"
        )
        val context = AttackContext(
            hasActiveOtp = false,
            otpWithinWindow = false,
            remoteDesktopActive = false,
            phishingLinkDetected = false,
            activeCallPhoneNumber = "+919876543210",
            windowStartMs = System.currentTimeMillis()
        )

        val eval1 = fusionEngine.evaluate(identity, context)
        val eval2 = fusionEngine.evaluate(identity, context)

        assertSame(eval1, eval2)
    }
}
