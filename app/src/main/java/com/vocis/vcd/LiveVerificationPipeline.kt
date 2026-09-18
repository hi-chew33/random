package com.vocis.vcd

import com.vocis.vcd.audio.AudioRingBuffer
import com.vocis.vcd.audio.WindowSlicingEngine
import com.vocis.vcd.crypto.BiometricCryptoVault
import com.vocis.vcd.domain.ContactVoiceprint
import com.vocis.vcd.domain.MathPrimitives
import com.vocis.vcd.domain.VcdVerificationResult
import com.vocis.vcd.fusion.BaselineCalibrator
import com.vocis.vcd.fusion.BiometricFusionEngine
import com.vocis.vcd.fusion.SessionScores
import com.vocis.vcd.inference.AntiSpoofDetectorModel
import com.vocis.vcd.inference.SpeakerEncoderModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * End-to-end Live Voice Clone Defence pipeline executing on active call audio.
 * Continuously polls audio frames from AudioRingBuffer, runs Resemblyzer and AASIST inference,
 * performs per-contact baseline calibration, applies 5-window median smoothing, and evaluates
 * the 2D decision matrix.
 */
class LiveVerificationPipeline(
    private val ringBuffer: AudioRingBuffer,
    private val windowSlicingEngine: WindowSlicingEngine,
    private val speakerEncoder: SpeakerEncoderModel,
    private val antiSpoofDetector: AntiSpoofDetectorModel,
    private val baselineCalibrator: BaselineCalibrator,
    private val sessionScores: SessionScores,
    private val fusionEngine: BiometricFusionEngine,
    private val cryptoVault: BiometricCryptoVault,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    /**
     * Starts continuous live verification against an enrolled contact voiceprint entity from Person A's Room database.
     */
    fun startVerification(
        entity: com.vocis.core.data.entity.ContactVoiceprintEntity,
        isActive: () -> Boolean
    ): Flow<VcdVerificationResult> {
        val voiceprint = ContactVoiceprint(
            contactId = entity.contactId.toString(),
            contactName = entity.name,
            phoneNumber = entity.phoneNumber,
            encryptedEmbedding = entity.voiceprintCipher,
            iv = entity.iv
        )
        return startVerification(voiceprint, isActive)
    }

    /**
     * Starts continuous live verification against an enrolled contact voiceprint.
     * Emits a VcdVerificationResult for every completed, non-silent 64,600-sample window.
     */
    fun startVerification(
        enrolledVoiceprint: ContactVoiceprint,
        isActive: () -> Boolean
    ): Flow<VcdVerificationResult> = flow {
        // 1. Decrypt contact centroid embedding once at session startup
        val enrolledCentroid = cryptoVault.decrypt(
            enrolledVoiceprint.encryptedEmbedding,
            enrolledVoiceprint.iv
        )

        // Reset session state
        baselineCalibrator.reset()
        sessionScores.reset()

        while (isActive()) {
            // 2. Extract next 64,600-sample window (slides by 48,000 samples)
            val audioWindow = windowSlicingEngine.extractNextWindow(ringBuffer)
            if (audioWindow == null) {
                // Buffer not full enough yet or silence frame skipped; yield
                kotlinx.coroutines.delay(50)
                continue
            }

            // 3. Neural inference
            // AASIST anti-spoof inference -> raw synthetic probability
            val rawSyntheticProb = antiSpoofDetector.detectSpoof(audioWindow)

            // Resemblyzer speaker embedding inference -> 256-dim unit vector
            val liveEmbedding = speakerEncoder.embed(audioWindow)
            val rawSimilarity = MathPrimitives.cosineSimilarity(enrolledCentroid, liveEmbedding)

            // 4. Baseline carrier calibration (initial 3 windows)
            val calibStatus = baselineCalibrator.ingestScore(rawSyntheticProb)

            // 5. Sliding 5-window median filter stabilization
            sessionScores.push(rawSimilarity, rawSyntheticProb)
            val smoothedSimilarity = sessionScores.medianSimilarity()
            val smoothedSynthetic = sessionScores.medianSynthetic()

            // 6. Dual-score 2D decision fusion
            val result = fusionEngine.evaluate(
                similarity = smoothedSimilarity,
                syntheticProbability = smoothedSynthetic,
                dynamicThreshold = calibStatus.dynamicThreshold,
                baselineSynthetic = calibStatus.baselineSynthetic,
                isReliable = calibStatus.isReliable
            )

            emit(result)
        }
    }.flowOn(ioDispatcher)
}
