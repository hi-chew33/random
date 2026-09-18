package com.vocis.vcd.inference

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.vocis.vcd.domain.MathPrimitives
import com.vocis.vcd.domain.VcdConstants
import java.nio.FloatBuffer

/**
 * Production ONNX Runtime implementation of AASIST audio anti-spoofing detector.
 *
 * Input node: [1, 64600] float32 tensor representing 16kHz linear audio PCM.
 * Output node: [1, 2] logits [bonafide, spoof].
 */
class OnnxAasistDetector(
    private val session: OrtSession,
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
) : AntiSpoofDetectorModel, AutoCloseable {

    override fun detectSpoof(audioWindow: FloatArray): Float {
        require(audioWindow.size >= VcdConstants.WINDOW_SAMPLES) {
            "Audio window size must be at least ${VcdConstants.WINDOW_SAMPLES}, got ${audioWindow.size}"
        }

        val inputWindow = if (audioWindow.size == VcdConstants.WINDOW_SAMPLES) {
            audioWindow
        } else {
            audioWindow.copyOf(VcdConstants.WINDOW_SAMPLES)
        }

        val shape = longArrayOf(1, VcdConstants.WINDOW_SAMPLES.toLong())
        val floatBuffer = FloatBuffer.wrap(inputWindow)
        val tensor = OnnxTensor.createTensor(env, floatBuffer, shape)

        tensor.use { inputTensor ->
            val inputName = session.inputNames.iterator().next()
            val results = session.run(mapOf(inputName to inputTensor))
            results.use { output ->
                val outputTensor = output[0] as OnnxTensor
                val rawValue = outputTensor.value

                // Handle 2D [1, 2] output logits array
                val logits = when (rawValue) {
                    is Array<*> -> {
                        val row0 = rawValue[0]
                        if (row0 is FloatArray) row0 else floatArrayOf(0.0f, 0.0f)
                    }
                    is FloatArray -> rawValue
                    else -> floatArrayOf(0.0f, 0.0f)
                }

                val probs = MathPrimitives.softmax2(logits)
                // probs[0] = bonafide, probs[1] = spoof / synthetic
                return probs[1]
            }
        }
    }

    override fun close() {
        session.close()
    }
}

/**
 * Production ONNX Runtime implementation of Resemblyzer GE2E speaker encoder.
 * Produces 256-dimensional L2-normalized embeddings.
 */
class OnnxSpeakerEncoder(
    private val session: OrtSession,
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
) : SpeakerEncoderModel, AutoCloseable {

    override fun embed(audioWindow: FloatArray): FloatArray {
        val shape = longArrayOf(1, audioWindow.size.toLong())
        val floatBuffer = FloatBuffer.wrap(audioWindow)
        val tensor = OnnxTensor.createTensor(env, floatBuffer, shape)

        tensor.use { inputTensor ->
            val inputName = session.inputNames.iterator().next()
            val results = session.run(mapOf(inputName to inputTensor))
            results.use { output ->
                val outputTensor = output[0] as OnnxTensor
                val rawValue = outputTensor.value

                val rawEmbedding = when (rawValue) {
                    is Array<*> -> {
                        val row0 = rawValue[0]
                        if (row0 is FloatArray) row0 else FloatArray(VcdConstants.EMBEDDING_DIM)
                    }
                    is FloatArray -> rawValue
                    else -> FloatArray(VcdConstants.EMBEDDING_DIM)
                }

                return MathPrimitives.l2Normalize(rawEmbedding)
            }
        }
    }

    override fun close() {
        session.close()
    }
}
