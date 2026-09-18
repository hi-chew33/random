package com.vocis.speech.llm

import kotlinx.serialization.Serializable

@Serializable
enum class ScamCategory {
    NONE,
    DIGITAL_ARREST,
    OTP_THEFT,
    BANKING_IMPERSONATION,
    REMOTE_ACCESS,
    COERCIVE_AUTHORITY
}

@Serializable
enum class UrgencyLevel {
    LOW,
    MEDIUM,
    HIGH,
    EXTREME
}

@Serializable
data class SemanticAnalysisResult(
    val isScam: Boolean,
    val scamCategory: ScamCategory,
    val urgencyLevel: UrgencyLevel,
    val coercionTactics: List<String> = emptyList(),
    val confidence: Float,
    val rawExplanation: String? = null,
    val isFromFallback: Boolean = false
)
