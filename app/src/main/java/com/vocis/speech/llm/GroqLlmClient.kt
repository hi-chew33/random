package com.vocis.speech.llm

import com.vocis.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Cloud LLM client interfacing with Groq API (llama-3.1-8b-instant).
 */
class GroqLlmClient(
    private val apiKey: String? = try {
        BuildConfig.GROQ_API_KEY.ifBlank { null }
    } catch (e: Throwable) {
        null
    },
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        private const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val SYSTEM_PROMPT = """
            You are a real-time telecommunication scam analysis system.
            Analyze the provided conversational speech transcript.
            Output ONLY valid JSON with this exact schema:
            {
              "isScam": boolean,
              "scamCategory": "NONE" | "DIGITAL_ARREST" | "OTP_THEFT" | "BANKING_IMPERSONATION" | "REMOTE_ACCESS",
              "urgencyLevel": "LOW" | "MEDIUM" | "HIGH" | "EXTREME",
              "coercionTactics": ["string"],
              "confidence": float between 0.0 and 1.0,
              "explanation": "concise rationale"
            }
        """
    }

    /**
     * Sends transcript to Groq for semantic analysis.
     * Returns null on missing API key, network timeout, rate limit (429), or parse failure.
     */
    fun analyzeTranscript(transcript: String): SemanticAnalysisResult? {
        if (apiKey.isNullOrBlank()) {
            return null
        }

        return try {
            val payload = buildJsonObject {
                put("model", "llama-3.1-8b-instant")
                putJsonObject("response_format") {
                    put("type", "json_object")
                }
                putJsonArray("messages") {
                    add(buildJsonObject {
                        put("role", "system")
                        put("content", SYSTEM_PROMPT.trimIndent())
                    })
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", transcript)
                    })
                }
                put("temperature", 0.1)
            }.toString()

            val request = Request.Builder()
                .url(GROQ_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }
                val bodyStr = response.body?.string() ?: return null
                val rootJson = JSONObject(bodyStr)
                val choices = rootJson.optJSONArray("choices") ?: return null
                if (choices.length() == 0) return null
                val messageObj = choices.getJSONObject(0).getJSONObject("message")
                val contentStr = messageObj.getString("content")

                parseAnalysisResponse(contentStr)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseAnalysisResponse(jsonContent: String): SemanticAnalysisResult? {
        return try {
            val obj = JSONObject(jsonContent)
            val isScam = obj.optBoolean("isScam", false)
            val catStr = obj.optString("scamCategory", "NONE")
            val cat = try { ScamCategory.valueOf(catStr) } catch (_: Exception) { ScamCategory.NONE }
            val urgStr = obj.optString("urgencyLevel", "LOW")
            val urg = try { UrgencyLevel.valueOf(urgStr) } catch (_: Exception) { UrgencyLevel.LOW }
            val conf = obj.optDouble("confidence", 0.5).toFloat()
            val expl = obj.optString("explanation", "")

            val tacticsList = mutableListOf<String>()
            val tacticsArr = obj.optJSONArray("coercionTactics")
            if (tacticsArr != null) {
                for (i in 0 until tacticsArr.length()) {
                    tacticsList.add(tacticsArr.getString(i))
                }
            }

            SemanticAnalysisResult(
                isScam = isScam,
                scamCategory = cat,
                urgencyLevel = urg,
                coercionTactics = tacticsList,
                confidence = conf,
                rawExplanation = expl,
                isFromFallback = false
            )
        } catch (_: Exception) {
            null
        }
    }
}
