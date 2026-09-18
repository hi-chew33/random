package com.vocis.vcd.fusion

import com.vocis.vcd.domain.VcdConstants

/**
 * Calibrates carrier line noise during the initial windows of a live call session.
 *
 * During windows 1-3, computes:
 * DynamicThreshold = max(0.50, BaselineSynthetic + 0.15)
 * If DynamicThreshold >= 1.0, marks line noise UNRELIABLE to suppress false alarms.
 */
class BaselineCalibrator(
    private val baselineWindowsCount: Int = VcdConstants.BASELINE_WINDOWS_COUNT,
    private val offset: Float = VcdConstants.BASELINE_OFFSET,
    private val minThreshold: Float = VcdConstants.MIN_DECISION_THRESHOLD,
    private val saturationLimit: Float = VcdConstants.SATURATION_THRESHOLD_LIMIT
) {

    private val initialScores = mutableListOf<Float>()
    var baselineSynthetic: Float = 0.0f
        private set
    var dynamicThreshold: Float = minThreshold
        private set
    var isCalibrated: Boolean = false
        private set
    var isReliable: Boolean = true
        private set

    /**
     * Ingests a synthetic probability score from an audio window.
     * Updates baseline and dynamic threshold until calibration count is reached.
     */
    fun ingestScore(syntheticProbability: Float): CalibrationStatus {
        if (!isCalibrated) {
            initialScores.add(syntheticProbability)

            val avg = initialScores.sum() / initialScores.size
            baselineSynthetic = avg
            val calculated = maxOf(minThreshold, baselineSynthetic + offset)
            dynamicThreshold = calculated

            if (calculated >= saturationLimit) {
                isReliable = false
            }

            if (initialScores.size >= baselineWindowsCount) {
                isCalibrated = true
            }
        }

        return CalibrationStatus(
            isCalibrated = isCalibrated,
            isReliable = isReliable,
            baselineSynthetic = baselineSynthetic,
            dynamicThreshold = dynamicThreshold
        )
    }

    /**
     * Resets calibration state for a new call session.
     */
    fun reset() {
        initialScores.clear()
        baselineSynthetic = 0.0f
        dynamicThreshold = minThreshold
        isCalibrated = false
        isReliable = true
    }
}

data class CalibrationStatus(
    val isCalibrated: Boolean,
    val isReliable: Boolean,
    val baselineSynthetic: Float,
    val dynamicThreshold: Float
)
