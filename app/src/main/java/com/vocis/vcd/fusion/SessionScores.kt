package com.vocis.vcd.fusion

import com.vocis.vcd.domain.VcdConstants
import java.util.ArrayDeque

/**
 * Maintains a sliding FIFO window of recent scores and computes median-filtered values
 * to stabilize instantaneous acoustic glitches and transient micro-bursts.
 */
class SessionScores(val windowSize: Int = VcdConstants.MEDIAN_FILTER_WINDOW_SIZE) {

    private val similarityDeque = ArrayDeque<Float>(windowSize)
    private val syntheticDeque = ArrayDeque<Float>(windowSize)

    /**
     * Appends a new window score pair and evicts the oldest once capacity is reached.
     */
    fun push(similarity: Float, syntheticProbability: Float) {
        if (similarityDeque.size >= windowSize) {
            similarityDeque.removeFirst()
        }
        similarityDeque.addLast(similarity)

        if (syntheticDeque.size >= windowSize) {
            syntheticDeque.removeFirst()
        }
        syntheticDeque.addLast(syntheticProbability)
    }

    /**
     * Computes the median of the current similarity scores in the window.
     */
    fun medianSimilarity(): Float {
        if (similarityDeque.isEmpty()) return 0.0f
        val sorted = similarityDeque.sorted()
        return sorted[sorted.size / 2]
    }

    /**
     * Computes the median of the current synthetic probabilities in the window.
     */
    fun medianSynthetic(): Float {
        if (syntheticDeque.isEmpty()) return 0.0f
        val sorted = syntheticDeque.sorted()
        return sorted[sorted.size / 2]
    }

    /**
     * Returns true if at least windowSize scores have been accumulated.
     */
    fun isFull(): Boolean {
        return similarityDeque.size >= windowSize
    }

    /**
     * Clears all accumulated session scores.
     */
    fun reset() {
        similarityDeque.clear()
        syntheticDeque.clear()
    }
}
