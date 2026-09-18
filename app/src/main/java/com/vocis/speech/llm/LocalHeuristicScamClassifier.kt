package com.vocis.speech.llm

/**
 * Deterministic local heuristic scam detection engine.
 * Serves as an immediate zero-latency fallback when Groq API is unavailable, rate-limited (429),
 * or when the user does not possess an active cloud API key.
 */
class LocalHeuristicScamClassifier {

    private val digitalArrestPatterns = listOf(
        Regex("(?i)\\b(digital arrest|cbi|ed officer|narcotics|police station|mumbai police|delhi police|arrest warrant|supreme court|illegal parcel)\\b"),
        Regex("(?i)\\b(passport confiscated|sim card blocked|trai order|money laundering|terror funding)\\b")
    )

    private val otpTheftPatterns = listOf(
        Regex("(?i)\\b(one time password|otp|verification code|share the code|6 digit code|sms code)\\b"),
        Regex("(?i)\\b(do not share|read back the numbers|confirm your pin)\\b")
    )

    private val bankingPatterns = listOf(
        Regex("(?i)\\b(rbi guidelines|account blocked|kyc update|pan expired|immediate transfer|safe account|rbi verification account)\\b"),
        Regex("(?i)\\b(credit card limit|debit card blocked|electricity disconnect)\\b")
    )

    private val remoteAccessPatterns = listOf(
        Regex("(?i)\\b(anydesk|teamviewer|rustdesk|quicksupport|screen share|install this app|support apk)\\b")
    )

    /**
     * Analyzes transcribed conversational speech for scam lures.
     */
    fun classify(transcript: String): SemanticAnalysisResult {
        val tactics = mutableListOf<String>()
        var highestCategory = ScamCategory.NONE
        var confidence = 0.0f
        var urgency = UrgencyLevel.LOW

        // 1. Digital Arrest check
        var digitalArrestMatches = 0
        for (pattern in digitalArrestPatterns) {
            if (pattern.containsMatchIn(transcript)) {
                digitalArrestMatches++
            }
        }
        if (digitalArrestMatches > 0) {
            highestCategory = ScamCategory.DIGITAL_ARREST
            confidence = if (digitalArrestMatches >= 2) 0.95f else 0.85f
            urgency = UrgencyLevel.EXTREME
            tactics.add("LEGAL_THREAT")
            tactics.add("ISOLATION")
        }

        // 2. Remote Access check
        for (pattern in remoteAccessPatterns) {
            if (pattern.containsMatchIn(transcript)) {
                if (highestCategory == ScamCategory.NONE) {
                    highestCategory = ScamCategory.REMOTE_ACCESS
                    confidence = 0.90f
                    urgency = UrgencyLevel.HIGH
                }
                tactics.add("DEVICE_TAKEOVER")
            }
        }

        // 3. OTP Theft check
        for (pattern in otpTheftPatterns) {
            if (pattern.containsMatchIn(transcript)) {
                if (highestCategory == ScamCategory.NONE) {
                    highestCategory = ScamCategory.OTP_THEFT
                    confidence = 0.90f
                    urgency = UrgencyLevel.EXTREME
                }
                tactics.add("CREDENTIAL_HARVESTING")
            }
        }

        // 4. Banking Impersonation check
        for (pattern in bankingPatterns) {
            if (pattern.containsMatchIn(transcript)) {
                if (highestCategory == ScamCategory.NONE) {
                    highestCategory = ScamCategory.BANKING_IMPERSONATION
                    confidence = 0.80f
                    urgency = UrgencyLevel.HIGH
                }
                tactics.add("FINANCIAL_COERCION")
            }
        }

        val isScam = highestCategory != ScamCategory.NONE

        return SemanticAnalysisResult(
            isScam = isScam,
            scamCategory = highestCategory,
            urgencyLevel = urgency,
            coercionTactics = tactics,
            confidence = confidence,
            rawExplanation = if (isScam) "Detected matching keywords for $highestCategory" else "Normal conversation",
            isFromFallback = true
        )
    }
}
