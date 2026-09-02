package com.example.data.processing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

object BitmapProcessor {

    /**
     * Loads a Bitmap from a Content URI with downsampling and EXIF orientation
     * correction. Photos captured with the device camera carry rotation metadata;
     * without this they appear sideways/upside-down in the app.
     */
    suspend fun loadBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 1920): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                // 1. Read bounds to compute a power-of-two sample size.
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, bounds)
                }

                var sampleSize = 1
                while ((bounds.outWidth / sampleSize) > maxDimension ||
                    (bounds.outHeight / sampleSize) > maxDimension
                ) {
                    sampleSize *= 2
                }

                // 2. Decode the actual bitmap at the computed sample size.
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                } ?: return@withContext null

                // 3. Apply EXIF orientation so camera shots are upright.
                val rotation = readExifRotation(context, uri)
                if (rotation != 0) rotateBitmap(bitmap, rotation.toFloat()) else bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    /**
     * Decodes a local file bitmap with downsampling. Not suspend by design so it
     * can be used from any background dispatcher; always call from Dispatchers.IO.
     */
    fun decodeSampledBitmap(file: File, maxDimension: Int = 2048): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sampleSize = 1
            while ((bounds.outWidth / sampleSize) > maxDimension ||
                (bounds.outHeight / sampleSize) > maxDimension
            ) {
                sampleSize *= 2
            }
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, opts)
        } catch (e: Exception) {
            null
        }
    }

    private fun readExifRotation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true).also {
            if (it != source) source.recycle()
        }
    }

    /**
     * Converts a Bitmap to a Base64 string for Gemini Multimodal API calls.
     */
    suspend fun bitmapToBase64(bitmap: Bitmap, maxDimension: Int = 800): String =
        withContext(Dispatchers.IO) {
            val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }

    /**
     * Saves a Bitmap to app internal storage under the "images" directory.
     */
    suspend fun saveBitmapToInternalStorage(
        context: Context,
        bitmap: Bitmap,
        fileName: String = "cine_${UUID.randomUUID()}.jpg"
    ): File = withContext(Dispatchers.IO) {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val file = File(imagesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            out.flush()
        }
        file
    }

    /**
     * Generates a high-quality stylized sample scene Bitmap if local URI is absent.
     */
    suspend fun generateSampleSceneBitmap(
        sampleKey: String,
        width: Int = 1280,
        height: Int = 720
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (sampleKey) {
            "landscape" -> {
                val skyShader = android.graphics.LinearGradient(
                    0f, 0f, 0f, height * 0.5f,
                    intArrayOf(0xFF009688.toInt(), 0xFF00BCD4.toInt(), 0xFF80DEEA.toInt()),
                    null, Shader.TileMode.CLAMP
                )
                paint.shader = skyShader
                canvas.drawRect(0f, 0f, width.toFloat(), height * 0.5f, paint)

                paint.shader = null
                paint.color = 0xFF0F4C5C.toInt()
                val path1 = android.graphics.Path().apply {
                    moveTo(0f, height * 0.45f)
                    lineTo(width * 0.25f, height * 0.25f)
                    lineTo(width * 0.5f, height * 0.35f)
                    lineTo(width * 0.75f, height * 0.22f)
                    lineTo(width.toFloat(), height * 0.45f)
                    close()
                }
                canvas.drawPath(path1, paint)

                paint.color = 0xFFC59B27.toInt()
                canvas.drawRect(0f, height * 0.45f, width.toFloat(), height * 0.65f, paint)

                paint.color = 0xFF785B3A.toInt()
                val road = android.graphics.Path().apply {
                    moveTo(width * 0.48f, height * 0.48f)
                    lineTo(width * 0.52f, height * 0.48f)
                    lineTo(width * 0.85f, height.toFloat())
                    lineTo(width * 0.15f, height.toFloat())
                    close()
                }
                canvas.drawPath(road, paint)
            }
            "fjord" -> {
                paint.color = 0xFF80CBC4.toInt()
                canvas.drawRect(0f, 0f, width.toFloat(), height * 0.4f, paint)
                paint.color = 0xFF134E4A.toInt()
                canvas.drawRect(0f, height * 0.35f, width.toFloat(), height * 0.55f, paint)
                paint.color = 0xFF00695C.toInt()
                canvas.drawRect(0f, height * 0.55f, width.toFloat(), height.toFloat(), paint)
            }
            "cyberpunk" -> {
                paint.color = 0xFF090A0F.toInt()
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.color = 0xFFE11D48.toInt()
                canvas.drawCircle(width * 0.3f, height * 0.4f, 150f, paint)
                paint.color = 0xFF06B6D4.toInt()
                canvas.drawCircle(width * 0.7f, height * 0.6f, 180f, paint)
            }
            else -> {
                paint.color = 0xFF2B2930.toInt()
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.color = 0xFFD0BCFF.toInt()
                canvas.drawCircle(width * 0.5f, height * 0.5f, 200f, paint)
            }
        }
        bitmap
    }

    /**
     * Applies the full cinematic grade (denoise → unblur → color grade → vignette →
     * grain → letterbox) on a Bitmap.
     */
    suspend fun processCinematicBitmap(
        source: Bitmap,
        unblurStrength: Float,
        contrast: Float,
        exposure: Float,
        saturation: Float,
        warmth: Float,
        tint: Float,
        vignette: Float,
        grain: Float,
        letterboxRatio: String = "2.39:1",
        presetId: String = "teal_orange",
        denoiseStrength: Float = 0f
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height

        // 0. Optional AI Smooth Denoise (3x3 neighborhood blend)
        val denoised = if (denoiseStrength > 1f) {
            applyDenoise(source, denoiseStrength)
        } else {
            source
        }

        // 1. Apply Sharpen / Unblur convolution kernel
        val sharpened = if (unblurStrength > 5f) {
            applySharpenKernel(denoised, unblurStrength)
        } else {
            denoised.copy(Bitmap.Config.ARGB_8888, true)
        }

        // 2. Apply ColorMatrix grade (shared math with the Compose preview)
        val gradedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(gradedBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val colorMatrix = ColorMatrix(
            CinematicColorMatrix.compute(
                contrast = contrast,
                exposure = exposure,
                saturation = saturation,
                warmth = warmth,
                tint = tint,
                presetId = presetId
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(sharpened, 0f, 0f, paint)

        // 3. Vignette
        if (vignette > 5f) {
            applyVignette(canvas, width, height, vignette)
        }

        // 4. Film grain
        if (grain > 5f) {
            applyFilmGrain(canvas, width, height, grain)
        }

        // 5. Letterbox
        if (letterboxRatio != "None") {
            applyLetterbox(canvas, width, height, letterboxRatio)
        }

        if (denoised !== source) denoised.recycle()
        if (sharpened !== denoised) sharpened.recycle()

        gradedBitmap
    }

    /**
     * Light spatial smoothing (3x3 neighborhood blend) controlled by strength.
     * Preserves alpha and edge pixels.
     */
    private fun applyDenoise(source: Bitmap, strength: Float): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        pixels.copyInto(outPixels)

        val mix = (strength / 100f).coerceIn(0f, 1f) * 0.65f
        val inv = 1f - mix

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val i = y * width + x
                val c = pixels[i]
                val l = pixels[i - 1]
                val r = pixels[i + 1]
                val t = pixels[i - width]
                val b = pixels[i + width]

                val a = (c ushr 24) and 0xFF
                val blurR = (((l ushr 16) and 0xFF) + ((r ushr 16) and 0xFF) +
                        ((t ushr 16) and 0xFF) + ((b ushr 16) and 0xFF)) / 4
                val blurG = (((l ushr 8) and 0xFF) + ((r ushr 8) and 0xFF) +
                        ((t ushr 8) and 0xFF) + ((b ushr 8) and 0xFF)) / 4
                val blurB = ((l and 0xFF) + (r and 0xFF) + (t and 0xFF) + (b and 0xFF)) / 4

                val origR = (c ushr 16) and 0xFF
                val origG = (c ushr 8) and 0xFF
                val origB = c and 0xFF

                val nr = (blurR * mix + origR * inv).roundToInt().coerceIn(0, 255)
                val ng = (blurG * mix + origG * inv).roundToInt().coerceIn(0, 255)
                val nb = (blurB * mix + origB * inv).roundToInt().coerceIn(0, 255)

                outPixels[i] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
            }
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Fast Unsharp Mask / Sharpening filter using a 3x3 convolution kernel.
     */
    private fun applySharpenKernel(source: Bitmap, strength: Float): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Start from a copy so border pixels keep the original values instead of
        // becoming transparent-black (IntArray default = 0).
        pixels.copyInto(outPixels)

        val weight = (strength / 100f).coerceIn(0f, 1.5f)
        val center = 1f + 4f * weight
        val edge = -weight

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val top = (y - 1) * width + x
                val bottom = (y + 1) * width + x
                val left = y * width + (x - 1)
                val right = y * width + (x + 1)

                val pC = pixels[idx]
                val pT = pixels[top]
                val pB = pixels[bottom]
                val pL = pixels[left]
                val pR = pixels[right]

                val a = (pC ushr 24) and 0xFF
                val r = (((pC ushr 16) and 0xFF) * center +
                        (((pT ushr 16) and 0xFF) + ((pB ushr 16) and 0xFF) +
                                ((pL ushr 16) and 0xFF) + ((pR ushr 16) and 0xFF)) * edge)
                    .roundToInt().coerceIn(0, 255)
                val g = (((pC ushr 8) and 0xFF) * center +
                        (((pT ushr 8) and 0xFF) + ((pB ushr 8) and 0xFF) +
                                ((pL ushr 8) and 0xFF) + ((pR ushr 8) and 0xFF)) * edge)
                    .roundToInt().coerceIn(0, 255)
                val b = ((pC and 0xFF) * center +
                        ((pT and 0xFF) + (pB and 0xFF) + (pL and 0xFF) + (pR and 0xFF)) * edge)
                    .roundToInt().coerceIn(0, 255)

                outPixels[idx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        return output
    }

    private fun applyVignette(canvas: Canvas, width: Int, height: Int, vignetteStrength: Float) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = maxOf(cx, cy) * 1.3f
        val alpha = ((vignetteStrength / 100f) * 230).roundToInt().coerceIn(0, 255)

        val vignetteShader = RadialGradient(
            cx, cy, radius,
            intArrayOf(0x00000000, 0x00000000, (alpha shl 24) or 0x000000),
            floatArrayOf(0.0f, 0.45f, 1.0f),
            Shader.TileMode.CLAMP
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = vignetteShader
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun applyFilmGrain(canvas: Canvas, width: Int, height: Int, grainStrength: Float) {
        val count = (width * height * (grainStrength / 100f) * 0.008f).toInt().coerceIn(500, 15000)
        val random = Random(42)
        val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            alpha = ((grainStrength / 100f) * 90).roundToInt().coerceIn(10, 120)
            strokeWidth = 1.5f
        }
        val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF000000.toInt()
            alpha = ((grainStrength / 100f) * 70).roundToInt().coerceIn(8, 100)
            strokeWidth = 1.5f
        }

        // Alternate light and dark specks for a more realistic emulsion grain.
        for (i in 0 until count) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            canvas.drawPoint(x, y, if (i % 2 == 0) lightPaint else darkPaint)
        }
    }

    private fun applyLetterbox(canvas: Canvas, width: Int, height: Int, ratio: String) {
        val targetAspect = when (ratio) {
            "2.39:1" -> 2.39f
            "16:9" -> 16f / 9f
            "4:3" -> 4f / 3f
            "9:16" -> 9f / 16f
            else -> return
        }

        val currentAspect = width.toFloat() / height.toFloat()
        val paint = Paint().apply {
            color = 0xFF000000.toInt()
            style = Paint.Style.FILL
        }

        if (targetAspect > currentAspect) {
            val activeHeight = width / targetAspect
            val barHeight = (height - activeHeight) / 2f
            if (barHeight > 0f) {
                canvas.drawRect(0f, 0f, width.toFloat(), barHeight, paint)
                canvas.drawRect(0f, height - barHeight, width.toFloat(), height.toFloat(), paint)
            }
        } else if (targetAspect < currentAspect) {
            val activeWidth = height * targetAspect
            val barWidth = (width - activeWidth) / 2f
            if (barWidth > 0f) {
                canvas.drawRect(0f, 0f, barWidth, height.toFloat(), paint)
                canvas.drawRect(width - barWidth, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }
    }

    /**
     * Creates an Intent to share the processed media file.
     */
    fun createShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
