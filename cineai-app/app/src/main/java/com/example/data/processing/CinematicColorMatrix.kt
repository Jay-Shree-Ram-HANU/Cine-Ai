package com.example.data.processing

import android.graphics.ColorMatrix

/**
 * Single source of truth for the cinematic color-grading math.
 *
 * Both the real bitmap render pipeline ([BitmapProcessor]) and the live Compose
 * preview ([com.example.ui.components.buildComposeCinematicColorMatrix]) consume
 * the exact same [FloatArray] produced here, so what the user sees in the
 * Before/After slider is pixel-identical to the exported image.
 */
object CinematicColorMatrix {

    /**
     * Builds the combined 4x5 color matrix for a given grade.
     *
     * Order of operations (matches the original pipeline):
     *   1. Saturation
     *   2. Contrast (scale + translate around mid-gray)
     *   3. Exposure / Warmth / Tint color shift
     *   4. Preset LUT color balance
     *
     * @return 20-element color matrix in Android/Compose ARGB layout.
     */
    fun compute(
        contrast: Float,
        exposure: Float,
        saturation: Float,
        warmth: Float,
        tint: Float,
        presetId: String
    ): FloatArray {
        val cm = ColorMatrix()

        // 1. Saturation
        val satFactor = (1f + saturation / 50f).coerceIn(0f, 3f)
        cm.setSaturation(satFactor)

        // 2. Contrast
        val cScale = (1f + contrast / 50f).coerceIn(0.2f, 3f)
        val cTranslate = (-0.5f * cScale + 0.5f) * 255f
        cm.postConcat(
            ColorMatrix(
                floatArrayOf(
                    cScale, 0f, 0f, 0f, cTranslate,
                    0f, cScale, 0f, 0f, cTranslate,
                    0f, 0f, cScale, 0f, cTranslate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )

        // 3. Exposure + Temperature/Tint
        val expVal = (exposure / 50f) * 60f
        val warmR = (warmth / 50f) * 35f
        val warmB = -(warmth / 50f) * 35f
        val tintG = -(tint / 50f) * 25f
        val tintM = (tint / 50f) * 15f
        cm.postConcat(
            ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, expVal + warmR + tintM,
                    0f, 1f, 0f, 0f, expVal + tintG,
                    0f, 0f, 1f, 0f, expVal + warmB + tintM,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )

        // 4. LUT preset color balance
        cm.postConcat(ColorMatrix(lutArray(presetId)))
        return cm.array
    }

    private fun lutArray(presetId: String): FloatArray = when (presetId) {
        "teal_orange" -> floatArrayOf(
            1.15f, 0f, 0f, 0f, 18f,
            0f, 1.05f, 0f, 0f, 5f,
            0f, 0f, 1.25f, 0f, -10f,
            0f, 0f, 0f, 1f, 0f
        )
        "nordic_emerald" -> floatArrayOf(
            0.90f, 0f, 0f, 0f, -10f,
            0f, 1.20f, 0f, 0f, 15f,
            0f, 0f, 1.15f, 0f, 12f,
            0f, 0f, 0f, 1f, 0f
        )
        "cyberpunk_neon" -> floatArrayOf(
            1.30f, 0f, 0f, 0f, 25f,
            0f, 0.85f, 0f, 0f, -15f,
            0f, 0f, 1.35f, 0f, 30f,
            0f, 0f, 0f, 1f, 0f
        )
        "vintage_35mm" -> floatArrayOf(
            1.15f, 0f, 0f, 0f, 22f,
            0f, 1.05f, 0f, 0f, 12f,
            0f, 0f, 0.85f, 0f, -15f,
            0f, 0f, 0f, 1f, 0f
        )
        "monochrome_noir" -> {
            val bw = ColorMatrix()
            bw.setSaturation(0f)
            bw.array
        }
        "bleach_bypass" -> floatArrayOf(
            1.25f, 0f, 0f, 0f, 10f,
            0f, 1.25f, 0f, 0f, 10f,
            0f, 0f, 1.25f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        )
        "wes_anderson" -> floatArrayOf(
            1.20f, 0f, 0f, 0f, 28f,
            0f, 1.15f, 0f, 0f, 18f,
            0f, 0f, 0.95f, 0f, -5f,
            0f, 0f, 0f, 1f, 0f
        )
        else -> ColorMatrix().array
    }
}
