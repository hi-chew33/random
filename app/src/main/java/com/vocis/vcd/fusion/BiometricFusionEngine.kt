package com.vocis.vcd.fusion

import com.vocis.vcd.domain.VcdConstants
import com.vocis.vcd.domain.VcdVerdict
import com.vocis.vcd.domain.VcdVerificationResult

/**
 * Executes the dual-score 2D decision matrix.
 *
 * CRITICAL ARCHITECTURAL INVARIANT:
 * Speaker similarity and synthetic probability are NEVER arithmetically averaged.
 * High similarity + High synthetic probability = CRITICAL Clone Signature.
 *
 * AASIST RELIABILITY GATE:
 * AASIST was trained exclusively on English ASVspoof-2019 data. When the caller
 * speaks a non-English language (Hindi, Hinglish, Tamil, etc.) or the audio is
 * heavily codec-compressed (Opus/OGG < 20 kbps), the model produces extreme
 * out-of-distribution logits that are meaningless as spoof signals.
 * When [isAasistReliable] = false, the synthetic score is ignored and the verdict
 * is driven by speaker similarity alone (or marked UNCERTAIN if no voiceprint).
 */
class BiometricFusionEngine(
    val speakerMatchThreshold: Float = VcdConstants.SPEAKER_MATCH_THRESHOLD,
    val speakerMismatchThreshold: Float = VcdConstants.SPEAKER_MISMATCH_THRESHOLD
) {

    /**
     * Evaluates live scores against dynamic calibration and outputs a categorical verdict.
     *
     * @param similarity           Cosine similarity to enrolled contact voiceprint [0.0, 1.0].
     * @param syntheticProbability Synthetic speech probability from AASIST [0.0, 1.0].
     * @param dynamicThreshold     Line-noise calibrated threshold from BaselineCalibrator.
     * @param baselineSynthetic    Baseline AASIST score measured on enrolled clip.
     * @param isReliable           False when carrier line noise exceeds saturation limit.
     * @param isAasistReliable     False when AASIST score is untrustworthy (non-English audio,
     *                             extreme codec compression, or out-of-distribution input).
     *                             When false, the synthetic score is suppressed from the decision.
     */
    fun evaluate(
        similarity: Float,
        syntheticProbability: Float,
        dynamicThreshold: Float,
        baselineSynthetic: Float,
        isReliable: Boolean,
        isAasistReliable: Boolean = true
    ): VcdVerificationResult {

        // Line noise saturation check — hardware/carrier issue, not voice
        if (!isReliable) {
            return VcdVerificationResult(
                verdict = VcdVerdict.UNRELIABLE_LINE_SATURATED,
                similarity = similarity,
                syntheticProbability = syntheticProbability,
                dynamicThreshold = dynamicThreshold,
                baselineSynthetic = baselineSynthetic,
                isReliable = false
            )
        }

        val isHighSimilarity = similarity >= speakerMatchThreshold
        val isLowSimilarity  = similarity < speakerMismatchThreshold

        // ── AASIST unreliable path (non-English / heavily compressed audio) ──────────
        // Drop the synthetic score entirely; decide on similarity only.
        if (!isAasistReliable) {
            val verdict = when {
                // Matches an enrolled contact — treat as potentially authentic; flag for
                // human review rather than hard-blocking (LLM semantic layer handles scam).
                isHighSimilarity -> VcdVerdict.SAFE_VERIFIED_AUTHENTIC
                // Very different from enrolled contact — suspicious but not confirmed synthetic
                isLowSimilarity  -> VcdVerdict.SUSPICIOUS_IMPOSTOR
                // Middle range with no spoof signal — cannot decide
                else             -> VcdVerdict.UNCERTAIN
            }
            return VcdVerificationResult(
                verdict = verdict,
                similarity = similarity,
                syntheticProbability = syntheticProbability,
                dynamicThreshold = dynamicThreshold,
                baselineSynthetic = baselineSynthetic,
                isReliable = true
            )
        }

        // ── Normal path: AASIST score is trustworthy ─────────────────────────────────
        val isHighSynthetic = syntheticProbability >= dynamicThreshold

        val verdict = when {
            // High similarity + High synthetic probability -> Verified voice clone attack!
            isHighSimilarity && isHighSynthetic  -> VcdVerdict.CRITICAL_CLONE_DETECTED

            // High similarity + Low synthetic probability -> Genuine trusted contact
            isHighSimilarity && !isHighSynthetic -> VcdVerdict.SAFE_VERIFIED_AUTHENTIC

            // High synthetic probability with low or uncalibrated similarity -> Unknown AI voice
            isHighSynthetic                      -> VcdVerdict.CRITICAL_UNKNOWN_SYNTHETIC

            // Low similarity + Low synthetic probability -> Wrong person, but natural human
            isLowSimilarity && !isHighSynthetic  -> VcdVerdict.SUSPICIOUS_IMPOSTOR

            // Intermediate similarity (0.50 <= sim < 0.75) with natural human voice
            else                                 -> VcdVerdict.UNCERTAIN
        }

        return VcdVerificationResult(
            verdict = verdict,
            similarity = similarity,
            syntheticProbability = syntheticProbability,
            dynamicThreshold = dynamicThreshold,
            baselineSynthetic = baselineSynthetic,
            isReliable = true
        )
    }
}
