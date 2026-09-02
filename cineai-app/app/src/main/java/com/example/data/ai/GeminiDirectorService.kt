package com.example.data.ai

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Central definition of the Gemini model used by the Director service.
 * Keep this in sync with the endpoint in [GeminiApiService].
 */
const val GEMINI_DIRECTOR_MODEL = "gemini-3.5-flash"

data class GeminiInlineData(
    val mimeType: String = "image/jpeg",
    val data: String
)

data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiCandidate(
    val content: GeminiContent?
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiDirectorClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .apply {
            // The API key travels in the request URL, so request logging MUST be
            // disabled in release builds to avoid leaking it into logcat.
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    }
                )
            }
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}

data class DirectorGradeRecommendation(
    val recommendedPresetId: String,
    val recommendedPresetName: String,
    val unblurStrength: Float,
    val denoiseStrength: Float,
    val contrast: Float,
    val exposure: Float,
    val saturation: Float,
    val warmth: Float,
    val tint: Float,
    val vignette: Float,
    val grain: Float,
    val notes: String
)

class GeminiDirectorService {

    suspend fun testApiConnection(customApiKey: String): Result<String> =
        withContext(Dispatchers.IO) {
            if (customApiKey.isBlank()) {
                return@withContext Result.failure(Exception("API key cannot be empty"))
            }
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = "Respond in 3 words: 'CineAI Gemini Connected'")
                            )
                        )
                    )
                )
                val response = GeminiDirectorClient.service.generateContent(customApiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    Result.success("Connected to $GEMINI_DIRECTOR_MODEL: ${text.trim()}")
                } else {
                    Result.failure(Exception("Received empty response from Gemini"))
                }
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: "Failed to reach Gemini API"))
            }
        }

    suspend fun analyzeAndAutoGrade(
        sceneTitle: String,
        sceneType: String,
        currentPreset: String,
        imageBase64: String? = null,
        customApiKey: String? = null
    ): DirectorGradeRecommendation = withContext(Dispatchers.IO) {
        val apiKey = customApiKey?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY

        val prompt = buildPrompt(sceneTitle, sceneType, currentPreset)

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val parts = mutableListOf<GeminiPart>()
                parts.add(GeminiPart(text = prompt))
                if (!imageBase64.isNullOrBlank()) {
                    parts.add(
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = "image/jpeg",
                                data = imageBase64
                            )
                        )
                    )
                }

                val request = GeminiRequest(contents = listOf(GeminiContent(parts = parts)))
                val response = GeminiDirectorClient.service.generateContent(apiKey, request)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!rawText.isNullOrBlank()) {
                    return@withContext parseDirectorGuidance(rawText, sceneTitle, sceneType)
                }
            } catch (e: Exception) {
                // Fall through to the offline rule-based director engine.
            }
        }

        // High-quality rule-based director analysis engine (no API key / no network).
        return@withContext generateSmartRuleBasedDirectorGrade(sceneTitle, sceneType, currentPreset)
    }

    private fun buildPrompt(sceneTitle: String, sceneType: String, currentPreset: String): String =
        """
            You are an elite Hollywood Film Director and Master Colorist.
            Analyze this cinematic shot:
            - Title/Subject: "$sceneTitle"
            - Type: "$sceneType"
            - Current Look: "$currentPreset"

            Respond with ONLY the following labeled values, one per line, no extra prose:
            LUT: <one of: teal_orange, nordic_emerald, cyberpunk_neon, vintage_35mm, monochrome_noir, bleach_bypass, wes_anderson>
            Unblur: <0 to 100>
            Denoise: <0 to 100>
            Contrast: <-50 to 50>
            Exposure: <-50 to 50>
            Saturation: <-50 to 50>
            Warmth: <-50 to 50>
            Tint: <-50 to 50>
            Vignette: <0 to 100>
            Grain: <0 to 100>
            Notes: <two concise sentences explaining the grade, lighting mood, leading lines, and emotional depth>
        """.trimIndent()

    private fun parseDirectorGuidance(
        rawText: String,
        title: String,
        type: String
    ): DirectorGradeRecommendation {
        val (presetId, presetName) = detectPreset(rawText)

        return DirectorGradeRecommendation(
            recommendedPresetId = presetId,
            recommendedPresetName = presetName,
            unblurStrength = extractNumber(rawText, "unblur", 75f).coerceIn(0f, 100f),
            denoiseStrength = extractNumber(rawText, "denoise", 30f).coerceIn(0f, 100f),
            contrast = extractNumber(rawText, "contrast", 30f).coerceIn(-50f, 50f),
            exposure = extractNumber(rawText, "exposure", 6f).coerceIn(-50f, 50f),
            saturation = extractNumber(rawText, "saturation", 24f).coerceIn(-50f, 50f),
            warmth = extractNumber(rawText, "warmth", 22f).coerceIn(-50f, 50f),
            tint = extractNumber(rawText, "tint", -12f).coerceIn(-50f, 50f),
            vignette = extractNumber(rawText, "vignette", 40f).coerceIn(0f, 100f),
            grain = extractNumber(rawText, "grain", 18f).coerceIn(0f, 100f),
            notes = "AI Director (Gemini): ${extractNotes(rawText)}"
        )
    }

    private val presetNames = mapOf(
        "teal_orange" to "Teal & Orange Blockbuster",
        "nordic_emerald" to "Nordic Fjord Emerald",
        "cyberpunk_neon" to "Cyberpunk Neon Noir",
        "vintage_35mm" to "Golden Hour 35mm Film",
        "monochrome_noir" to "Classic Noir Silver",
        "bleach_bypass" to "Sci-Fi Bleach Bypass",
        "wes_anderson" to "Pastel Symmetrical Cinema"
    )

    private fun detectPreset(rawText: String): Pair<String, String> {
        // Prefer an explicit "LUT: <id>" line if the model returned one.
        val explicit = Regex("""\blut\b\s*[:=-]?\s*([a-z0-9_]+)""", RegexOption.IGNORE_CASE)
            .find(rawText)
            ?.groupValues
            ?.get(1)
            ?.lowercase()
        if (explicit != null && presetNames.containsKey(explicit)) {
            return explicit to presetNames.getValue(explicit)
        }

        val lower = rawText.lowercase()
        return when {
            "teal" in lower || "orange" in lower || "blockbuster" in lower ->
                "teal_orange" to presetNames.getValue("teal_orange")
            "nordic" in lower || "fjord" in lower || "emerald" in lower ->
                "nordic_emerald" to presetNames.getValue("nordic_emerald")
            "cyber" in lower || "neon" in lower || "tokyo" in lower ->
                "cyberpunk_neon" to presetNames.getValue("cyberpunk_neon")
            "vintage" in lower || "35mm" in lower || "analog" in lower || "golden" in lower ->
                "vintage_35mm" to presetNames.getValue("vintage_35mm")
            "monochrome" in lower || "noir" in lower || "black & white" in lower || "silver" in lower ->
                "monochrome_noir" to presetNames.getValue("monochrome_noir")
            "bleach" in lower || "sci-fi" in lower || "sci fi" in lower ->
                "bleach_bypass" to presetNames.getValue("bleach_bypass")
            "wes" in lower || "pastel" in lower ->
                "wes_anderson" to presetNames.getValue("wes_anderson")
            else -> "teal_orange" to presetNames.getValue("teal_orange")
        }
    }

    /**
     * Extracts a labeled numeric value from free-form model output, tolerant of
     * decimals, negative signs, colons/equals and markdown bullets.
     */
    private fun extractNumber(raw: String, label: String, default: Float): Float {
        val pattern = Regex(
            """${Regex.escape(label)}\s*[:=-]?\s*(-?\d+(?:\.\d+)?)""",
            RegexOption.IGNORE_CASE
        )
        return pattern.find(raw)?.groupValues?.get(1)?.toFloatOrNull() ?: default
    }

    private fun extractNotes(raw: String): String {
        val notesLine = Regex("""\bnotes\s*[:=-]\s*(.+)""", RegexOption.IGNORE_CASE)
            .find(raw)
            ?.groupValues
            ?.get(1)
            ?.trim()

        val text = notesLine ?: raw.lines()
            .filter { line ->
                line.isNotBlank() &&
                    !line.startsWith("1)") && !line.startsWith("2)") && !line.startsWith("3)") &&
                    !Regex(
                        """^\s*(lut|unblur|denoise|contrast|exposure|saturation|warmth|tint|vignette|grain)\s*[:=-]""",
                        RegexOption.IGNORE_CASE
                    ).containsMatchIn(line)
            }
            .joinToString(" ")

        return text.replace(Regex("""\s+"""), " ").trim().take(320).ifBlank {
            "Enhanced dynamic contrast, color separation, and focal sharpness for pristine cinematic cadence."
        }
    }

    private fun generateSmartRuleBasedDirectorGrade(
        title: String,
        type: String,
        currentPreset: String
    ): DirectorGradeRecommendation {
        val titleLower = title.lowercase()
        return when {
            "fjord" in titleLower || "water" in titleLower || "nordic" in titleLower ||
                "coastal" in titleLower || "lake" in titleLower -> {
                DirectorGradeRecommendation(
                    recommendedPresetId = "nordic_emerald",
                    recommendedPresetName = "Nordic Fjord Emerald",
                    unblurStrength = 84f,
                    denoiseStrength = 30f,
                    contrast = 35f,
                    exposure = 4f,
                    saturation = 22f,
                    warmth = 12f,
                    tint = -24f,
                    vignette = 45f,
                    grain = 18f,
                    notes = "AI Director (Offline): Deconvolving water ripples and micro-textures. Enhanced misty teal ocean tones and warm amber foliage contrast against deep mountain shadows."
                )
            }
            "rain" in titleLower || "cyber" in titleLower || "neon" in titleLower ||
                "night" in titleLower || "city" in titleLower || "short" in titleLower -> {
                DirectorGradeRecommendation(
                    recommendedPresetId = "cyberpunk_neon",
                    recommendedPresetName = "Cyberpunk Neon Noir",
                    unblurStrength = 88f,
                    denoiseStrength = 35f,
                    contrast = 44f,
                    exposure = -4f,
                    saturation = 42f,
                    warmth = -18f,
                    tint = 32f,
                    vignette = 54f,
                    grain = 24f,
                    notes = "AI Director (Offline): High-frequency edge sharpness restored. Pushed electric cyan reflections on asphalt and blooming magenta neon signage for high-octane 24fps cinematic atmosphere."
                )
            }
            "portrait" in titleLower || "face" in titleLower || "artist" in titleLower ||
                "model" in titleLower -> {
                DirectorGradeRecommendation(
                    recommendedPresetId = "vintage_35mm",
                    recommendedPresetName = "Golden Hour 35mm Film",
                    unblurStrength = 65f,
                    denoiseStrength = 40f,
                    contrast = 18f,
                    exposure = 14f,
                    saturation = 14f,
                    warmth = 32f,
                    tint = 6f,
                    vignette = 32f,
                    grain = 42f,
                    notes = "AI Director (Offline): Soft lens edge unblur applied. Golden hour amber halation applied to hair and skin tones with organic 35mm silver-halide grain."
                )
            }
            "noir" in titleLower || "action" in titleLower || "midnight" in titleLower -> {
                DirectorGradeRecommendation(
                    recommendedPresetId = "monochrome_noir",
                    recommendedPresetName = "Classic Noir Silver",
                    unblurStrength = 90f,
                    denoiseStrength = 25f,
                    contrast = 50f,
                    exposure = 2f,
                    saturation = -100f,
                    warmth = 0f,
                    tint = 0f,
                    vignette = 58f,
                    grain = 36f,
                    notes = "AI Director (Offline): Motion blur stabilized. Dramatic high-contrast silver nitrate monochrome grading isolates silhouette edges with rich shadow roll-off."
                )
            }
            else -> {
                DirectorGradeRecommendation(
                    recommendedPresetId = "teal_orange",
                    recommendedPresetName = "Teal & Orange Blockbuster",
                    unblurStrength = 78f,
                    denoiseStrength = 30f,
                    contrast = 30f,
                    exposure = 8f,
                    saturation = 28f,
                    warmth = 26f,
                    tint = -14f,
                    vignette = 42f,
                    grain = 16f,
                    notes = "AI Director (Offline): Leading dirt road geometry enhanced. Hollywood blockbuster teal sky and golden sunlit earth palette applied with 2.39:1 Cinemascope framing."
                )
            }
        }
    }
}
