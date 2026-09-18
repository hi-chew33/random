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
 */
class BiometricFusionEngine(
    val speakerMatchThreshold: Float = VcdConstants.SPEAKER_MATCH_THRESHOLD,
    val speakerMismatchThreshold: Float = VcdConstants.SPEAKER_MISMATCH_THRESHOLD
) {

    /**
     * Evaluates live scores against dynamic calibration and outputs a categorical verdict.
     *
     * @param similarity Cosine similarity to enrolled contact voiceprint [0.0, 1.0].
     * @param syntheticProbability Synthetic speech probability from AASIST [0.0, 1.0].
     * @param dynamicThreshold Line-noise calibrated threshold from BaselineCalibrator.
     * @param isReliable Boolean indicating whether carrier line noise is below saturation limit.
     */
    fun evaluate(
        similarity: Float,
        syntheticProbability: Float,
        dynamicThreshold: Float,
        baselineSynthetic: Float,
        isReliable: Boolean
    ): VcdVerificationResult {

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
        val isLowSimilarity = similarity < speakerMismatchThreshold
        val isHighSynthetic = syntheticProbability >= dynamicThreshold

        val verdict = when {
            // High similarity + High synthetic probability -> Verified voice clone attack!
            isHighSimilarity && isHighSynthetic -> VcdVerdict.CRITICAL_CLONE_DETECTED

            // High similarity + Low synthetic probability -> Genuine trusted contact
            isHighSimilarity && !isHighSynthetic -> VcdVerdict.SAFE_VERIFIED_AUTHENTIC

            // Low similarity + Low synthetic probability -> Wrong person, but natural human
            isLowSimilarity && !isHighSynthetic -> VcdVerdict.SUSPICIOUS_IMPOSTOR

            // Low similarity + High synthetic probability -> Synthetic audio from unknown source
            isLowSimilarity && isHighSynthetic -> VcdVerdict.CRITICAL_UNKNOWN_SYNTHETIC

            // Intermediate similarity (0.50 <= sim < 0.75)
            else -> VcdVerdict.UNCERTAIN
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
