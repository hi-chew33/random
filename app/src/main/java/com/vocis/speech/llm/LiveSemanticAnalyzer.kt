package com.vocis.speech.llm

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

/**
 * Batches speech transcript chunks over a 10-second window and coordinates
 * semantic scam analysis with automatic local heuristic fallback.
 */
class LiveSemanticAnalyzer(
    private val groqClient: GroqLlmClient = GroqLlmClient(apiKey = null),
    private val localClassifier: LocalHeuristicScamClassifier = LocalHeuristicScamClassifier(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val transcriptBuffer = StringBuilder()
    private val _analysisResults = MutableSharedFlow<SemanticAnalysisResult>(replay = 1, extraBufferCapacity = 16)
    val analysisResults: SharedFlow<SemanticAnalysisResult> = _analysisResults.asSharedFlow()

    /**
     * Appends newly transcribed speech tokens.
     */
    fun appendTranscript(token: String) {
        synchronized(transcriptBuffer) {
            transcriptBuffer.append(" ").append(token)
        }
    }

    /**
     * Executes analysis on the accumulated transcript window.
     * Tries Groq Cloud LLM first; gracefully falls back to local regex on failure or missing API key.
     */
    suspend fun analyzeCurrentWindow(): SemanticAnalysisResult = withContext(ioDispatcher) {
        val currentText = synchronized(transcriptBuffer) {
            val text = transcriptBuffer.toString().trim()
            text
        }

        if (currentText.isBlank()) {
            val empty = SemanticAnalysisResult(
                isScam = false,
                scamCategory = ScamCategory.NONE,
                urgencyLevel = UrgencyLevel.LOW,
                confidence = 1.0f,
                rawExplanation = "No speech detected"
            )
            return@withContext empty
        }

        // 1. Try Groq Cloud LLM
        val groqResult = groqClient.analyzeTranscript(currentText)

        // 2. Fall back to local pattern heuristic if Groq fails or API key is absent
        val finalResult = groqResult ?: localClassifier.classify(currentText)

        _analysisResults.tryEmit(finalResult)
        finalResult
    }

    /**
     * Clears transcript buffer for new call session.
     */
    fun reset() {
        synchronized(transcriptBuffer) {
            transcriptBuffer.clear()
        }
    }
}
