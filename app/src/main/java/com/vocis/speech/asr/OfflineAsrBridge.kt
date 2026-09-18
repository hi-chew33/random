package com.vocis.speech.asr

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer

interface SpeechRecognizerBridge {
    val transcripts: SharedFlow<String>
    fun acceptWaveform(pcmData: ByteArray, length: Int = pcmData.size)
    fun reset()
}

/**
 * Production Offline Speech Recognition using Vosk (Kaldi ASR).
 * Runs completely on-device without network connectivity.
 */
class VoskSpeechRecognizerBridge(
    private val model: Model,
    private val sampleRate: Float = 16000.0f
) : SpeechRecognizerBridge, AutoCloseable {

    private var recognizer: Recognizer? = Recognizer(model, sampleRate)
    private val _transcripts = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val transcripts: SharedFlow<String> = _transcripts.asSharedFlow()

    override fun acceptWaveform(pcmData: ByteArray, length: Int) {
        val rec = recognizer ?: return
        if (rec.acceptWaveForm(pcmData, length)) {
            val jsonResult = rec.result
            val text = parseTextFromJson(jsonResult)
            if (text.isNotBlank()) {
                _transcripts.tryEmit(text)
            }
        }
    }

    private fun parseTextFromJson(jsonStr: String): String {
        return try {
            val obj = JSONObject(jsonStr)
            obj.optString("text", "")
        } catch (_: Exception) {
            ""
        }
    }

    override fun reset() {
        recognizer?.reset()
    }

    override fun close() {
        recognizer?.close()
        recognizer = null
    }
}

/**
 * Mock SpeechRecognizerBridge for unit testing.
 */
class MockSpeechRecognizerBridge : SpeechRecognizerBridge {
    private val _transcripts = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 64)
    override val transcripts: SharedFlow<String> = _transcripts.asSharedFlow()

    fun emitMockText(text: String) {
        _transcripts.tryEmit(text)
    }

    override fun acceptWaveform(pcmData: ByteArray, length: Int) {
        // Mock accepts audio without native Kaldi dependencies
    }

    override fun reset() {}
}
