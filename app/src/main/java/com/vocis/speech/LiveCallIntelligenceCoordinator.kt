package com.vocis.speech

import com.vocis.speech.asr.SpeechRecognizerBridge
import com.vocis.speech.llm.LiveSemanticAnalyzer
import com.vocis.speech.llm.ScamCategory
import com.vocis.speech.llm.SemanticAnalysisResult
import com.vocis.vcd.domain.ContactVoiceprint
import com.vocis.vcd.domain.VcdVerdict
import com.vocis.vcd.domain.VcdVerificationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Unified live call intelligence state exposed to Role D's in-call UI screens and overlays.
 */
data class LiveCallIntelligenceState(
    val latestVcdResult: VcdVerificationResult? = null,
    val fullTranscript: String = "",
    val latestSemanticAnalysis: SemanticAnalysisResult? = null,
    val isCriticalVoiceClone: Boolean = false,
    val isScamLureDetected: Boolean = false,
    val sharedSecretQuestion: String? = null
)

/**
 * Master coordinator for Role C.
 * Fuses biometric voice verification, offline speech transcription, and semantic threat intelligence.
 */
class LiveCallIntelligenceCoordinator(
    private val asrBridge: SpeechRecognizerBridge,
    private val semanticAnalyzer: LiveSemanticAnalyzer,
    private val scope: CoroutineScope
) {

    private val _state = MutableStateFlow(LiveCallIntelligenceState())
    val state: StateFlow<LiveCallIntelligenceState> = _state.asStateFlow()

    private var transcriptJob: Job? = null
    private var analysisJob: Job? = null
    private val transcriptAccumulator = StringBuilder()

    /**
     * Starts listening to live call intelligence streams.
     */
    fun startSession(enrolledContact: ContactVoiceprint? = null) {
        reset()

        _state.value = _state.value.copy(
            sharedSecretQuestion = enrolledContact?.sharedSecretQuestion
        )

        // 1. Collect speech transcripts
        transcriptJob = scope.launch {
            asrBridge.transcripts.collect { textToken ->
                transcriptAccumulator.append(" ").append(textToken)
                semanticAnalyzer.appendTranscript(textToken)

                _state.value = _state.value.copy(
                    fullTranscript = transcriptAccumulator.toString().trim()
                )
            }
        }

        // 2. Collect semantic analyzer results
        analysisJob = scope.launch {
            semanticAnalyzer.analysisResults.collect { analysis ->
                _state.value = _state.value.copy(
                    latestSemanticAnalysis = analysis,
                    isScamLureDetected = analysis.isScam
                )
            }
        }
    }

    /**
     * Ingests a new live biometric verdict from the VCD pipeline.
     */
    fun onVcdResult(result: VcdVerificationResult) {
        val isClone = (result.verdict == VcdVerdict.CRITICAL_CLONE_DETECTED ||
                result.verdict == VcdVerdict.CRITICAL_UNKNOWN_SYNTHETIC)

        _state.value = _state.value.copy(
            latestVcdResult = result,
            isCriticalVoiceClone = isClone
        )
    }

    fun reset() {
        transcriptJob?.cancel()
        analysisJob?.cancel()
        transcriptAccumulator.clear()
        semanticAnalyzer.reset()
        asrBridge.reset()
        _state.value = LiveCallIntelligenceState()
    }
}
