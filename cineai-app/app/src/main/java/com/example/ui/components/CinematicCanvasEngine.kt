package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItemEntity
import com.example.data.processing.BitmapProcessor
import com.example.data.processing.CinematicColorMatrix
import com.example.ui.theme.CineBorder
import com.example.ui.theme.CineGoldPrimary
import com.example.ui.theme.CineObsidian
import com.example.ui.theme.CinePrimaryContainer
import com.example.ui.theme.CineTealAccent
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import java.io.File
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Draws the Before (flat/raw) or After (cinematic color grade & AI unblur) rendering
 * for various sample scenes or applied parameters.
 */
fun DrawScope.drawCinematicScene(
    sampleKey: String,
    isAfter: Boolean,
    unblurStrength: Float,
    contrast: Float,
    exposure: Float,
    saturation: Float,
    warmth: Float,
    tint: Float,
    vignette: Float,
    grain: Float,
    letterboxRatio: String = "2.39:1"
) {
    val w = size.width
    val h = size.height

    when (sampleKey) {
        "landscape" -> drawValleyRoadLandscape(isAfter, unblurStrength, contrast, warmth, tint, saturation)
        "fjord" -> drawNordicFjordScene(isAfter, unblurStrength, contrast, warmth, tint, saturation)
        "cyberpunk" -> drawCyberpunkShortScene(isAfter, unblurStrength, contrast, warmth, tint, saturation)
        "portrait" -> drawVintagePortraitScene(isAfter, unblurStrength, contrast, warmth, tint, saturation)
        else -> drawValleyRoadLandscape(isAfter, unblurStrength, contrast, warmth, tint, saturation)
    }

    // Apply Letterbox Bars if needed
    drawCinematicLetterbox(letterboxRatio, w, h)

    // Apply Film Grain overlay if After
    if (isAfter && grain > 0) {
        drawFilmGrainOverlay(grain, w, h)
    }

    // Apply Vignette if After
    if (isAfter && vignette > 0) {
        drawCinematicVignette(vignette, w, h)
    }
}

private fun DrawScope.drawValleyRoadLandscape(
    isAfter: Boolean,
    unblur: Float,
    contrast: Float,
    warmth: Float,
    tint: Float,
    saturation: Float
) {
    val w = size.width
    val h = size.height

    // Sky colors:
    // BEFORE: flat pale blue / grey sky
    // AFTER: blockbuster intense turquoise/cyan teal sky with glowing clouds
    val skyTop = if (isAfter) Color(0xFF009688) else Color(0xFF90A4AE)
    val skyMid = if (isAfter) Color(0xFF00BCD4) else Color(0xFFB0BEC5)
    val skyHorizon = if (isAfter) Color(0xFF80DEEA) else Color(0xFFCFD8DC)

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(skyTop, skyMid, skyHorizon),
            startY = 0f,
            endY = h * 0.45f
        ),
        size = Size(w, h * 0.45f)
    )

    // Fluffy cinematic clouds
    val cloudColor = if (isAfter) Color(0xFFFFFFFF).copy(alpha = 0.85f) else Color(0xFFECEFF1).copy(alpha = 0.6f)
    drawOval(
        color = cloudColor,
        topLeft = Offset(w * 0.1f, h * 0.08f),
        size = Size(w * 0.45f, h * 0.18f)
    )
    drawOval(
        color = cloudColor,
        topLeft = Offset(w * 0.35f, h * 0.12f),
        size = Size(w * 0.5f, h * 0.16f)
    )

    // Distant mountain range:
    // BEFORE: Hazy dull grey-blue mountain
    // AFTER: Deep cinematic cyan-teal moody mountain with sharp ridge contrast
    val mountainBack = if (isAfter) Color(0xFF0F4C5C) else Color(0xFF546E7A)
    val mountainFront = if (isAfter) Color(0xFF1B3B4B) else Color(0xFF37474F)

    val mountainPath1 = Path().apply {
        moveTo(0f, h * 0.42f)
        lineTo(w * 0.2f, h * 0.30f)
        lineTo(w * 0.4f, h * 0.33f)
        lineTo(w * 0.65f, h * 0.27f)
        lineTo(w * 0.85f, h * 0.34f)
        lineTo(w, h * 0.38f)
        lineTo(w, h * 0.45f)
        lineTo(0f, h * 0.45f)
        close()
    }
    drawPath(mountainPath1, mountainBack)

    val mountainPath2 = Path().apply {
        moveTo(0f, h * 0.43f)
        lineTo(w * 0.25f, h * 0.32f)
        lineTo(w * 0.55f, h * 0.35f)
        lineTo(w * 0.75f, h * 0.31f)
        lineTo(w, h * 0.39f)
        lineTo(w, h * 0.48f)
        lineTo(0f, h * 0.48f)
        close()
    }
    drawPath(mountainPath2, mountainFront)

    // Midground valley fields:
    // BEFORE: dull flat pale green/olive
    // AFTER: luminous golden-amber sunlit fields with deep shadow depth
    val valleyColor = if (isAfter) Color(0xFFC59B27) else Color(0xFF687A54)
    drawRect(
        brush = Brush.verticalGradient(
            colors = if (isAfter) listOf(Color(0xFF8D8030), Color(0xFFC59B27), Color(0xFFD4A017))
            else listOf(Color(0xFF556B2F), Color(0xFF6B8E23)),
            startY = h * 0.45f,
            endY = h * 0.60f
        ),
        topLeft = Offset(0f, h * 0.45f),
        size = Size(w, h * 0.15f)
    )

    // Foreground Rolling Grass Hill:
    // BEFORE: Muted olive green
    // AFTER: Radiant golden yellow grass with crisp AI unblurred blades
    val grassTop = if (isAfter) Color(0xFFD4AF37) else Color(0xFF556B2F)
    val grassBottom = if (isAfter) Color(0xFF996515) else Color(0xFF33421A)

    val hillPath = Path().apply {
        moveTo(0f, h * 0.52f)
        cubicTo(w * 0.3f, h * 0.50f, w * 0.7f, h * 0.54f, w, h * 0.51f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        hillPath,
        brush = Brush.verticalGradient(
            colors = listOf(grassTop, grassBottom),
            startY = h * 0.5f,
            endY = h
        )
    )

    // Dirt Path leading into the distance (Leading lines):
    // BEFORE: pale flat beige path
    // AFTER: rich textured dirt tracks with golden sun highlights and deep tread shadows
    val roadColor = if (isAfter) Color(0xFF785B3A) else Color(0xFF9E8D76)
    val roadHighlight = if (isAfter) Color(0xFFB58A57) else Color(0xFFBCAAA4)

    val roadPath = Path().apply {
        moveTo(w * 0.48f, h * 0.46f)
        lineTo(w * 0.52f, h * 0.46f)
        lineTo(w * 0.85f, h)
        lineTo(w * 0.15f, h)
        close()
    }
    drawPath(roadPath, roadColor)

    // Tire rut lines for depth
    val rutPathLeft = Path().apply {
        moveTo(w * 0.49f, h * 0.47f)
        lineTo(w * 0.28f, h)
        lineTo(w * 0.38f, h)
        lineTo(w * 0.50f, h * 0.47f)
        close()
    }
    drawPath(rutPathLeft, roadHighlight)

    val rutPathRight = Path().apply {
        moveTo(w * 0.51f, h * 0.47f)
        lineTo(w * 0.65f, h)
        lineTo(w * 0.76f, h)
        lineTo(w * 0.52f, h * 0.47f)
        close()
    }
    drawPath(rutPathRight, roadHighlight)

    // If After & Unblurred, draw crisp textural details
    if (isAfter) {
        val sharpColor = Color(0xFFFFD54F).copy(alpha = (unblur / 100f).coerceIn(0.3f, 0.9f))
        for (i in 0..18) {
            val rx = w * (0.05f + (i * 0.052f))
            val ry = h * (0.62f + (i % 5) * 0.07f)
            drawLine(
                color = sharpColor,
                start = Offset(rx, ry),
                end = Offset(rx - 3f, ry - 14f),
                strokeWidth = 2f
            )
        }
    }
}

private fun DrawScope.drawNordicFjordScene(
    isAfter: Boolean,
    unblur: Float,
    contrast: Float,
    warmth: Float,
    tint: Float,
    saturation: Float
) {
    val w = size.width
    val h = size.height

    // Sky:
    // BEFORE: Overcast flat white/grey
    // AFTER: Atmospheric misty teal gradient
    val sky = if (isAfter) {
        listOf(Color(0xFF80CBC4), Color(0xFFB2DFDB), Color(0xFFE0F2F1))
    } else {
        listOf(Color(0xFFCFD8DC), Color(0xFFECEFF1), Color(0xFFF5F5F5))
    }
    drawRect(
        brush = Brush.verticalGradient(sky, startY = 0f, endY = h * 0.4f),
        size = Size(w, h * 0.4f)
    )

    // Steep dramatic mountain peaks:
    // BEFORE: dull grey rock
    // AFTER: moody emerald-tinted dark granite with crisp ridges
    val rockColor = if (isAfter) Color(0xFF134E4A) else Color(0xFF455A64)
    val mountainPeak = Path().apply {
        moveTo(0f, h * 0.42f)
        lineTo(w * 0.35f, h * 0.12f)
        lineTo(w * 0.45f, h * 0.18f)
        lineTo(w * 0.75f, h * 0.09f)
        lineTo(w, h * 0.38f)
        lineTo(w, h * 0.45f)
        lineTo(0f, h * 0.45f)
        close()
    }
    drawPath(mountainPeak, rockColor)

    // Fjord Water:
    // BEFORE: Dull steel grey-blue water
    // AFTER: Deep glowing turquoise/emerald sea with silky reflections
    val waterColors = if (isAfter) {
        listOf(Color(0xFF004D40), Color(0xFF00695C), Color(0xFF00897B), Color(0xFF26A69A))
    } else {
        listOf(Color(0xFF37474F), Color(0xFF455A64), Color(0xFF546E7A))
    }
    drawRect(
        brush = Brush.verticalGradient(waterColors, startY = h * 0.42f, endY = h * 0.85f),
        topLeft = Offset(0f, h * 0.42f),
        size = Size(w, h * 0.43f)
    )

    // Shoreline with Golden Amber Autumn Foliage:
    // BEFORE: pale yellow-brown dead grass
    // AFTER: intensely vibrant warm amber, orange & gold leaves
    val shoreTop = if (isAfter) Color(0xFFEA580C) else Color(0xFF8D6E63)
    val shoreMid = if (isAfter) Color(0xFFD97706) else Color(0xFF6D4C41)
    val shoreBottom = if (isAfter) Color(0xFFB45309) else Color(0xFF4E342E)

    val shorePath = Path().apply {
        moveTo(0f, h * 0.70f)
        cubicTo(w * 0.4f, h * 0.65f, w * 0.7f, h * 0.72f, w, h * 0.62f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        shorePath,
        brush = Brush.verticalGradient(
            listOf(shoreTop, shoreMid, shoreBottom),
            startY = h * 0.62f,
            endY = h
        )
    )

    // Traditional Red Rorbu Cottages on Wooden Stilts:
    // BEFORE: Faded dull dark red
    // AFTER: Punchy vibrant crimson red with glowing windows & crisp stilt woodwork
    val houseRed = if (isAfter) Color(0xFFDC2626) else Color(0xFF8B2500)
    val houseShadow = if (isAfter) Color(0xFF991B1B) else Color(0xFF5C1000)
    val roofColor = if (isAfter) Color(0xFF1E293B) else Color(0xFF475569)
    val stiltWood = if (isAfter) Color(0xFF78350F) else Color(0xFF5D4037)

    // House 1 (Foreground Left)
    val h1x = w * 0.15f
    val h1y = h * 0.74f
    val h1w = w * 0.28f
    val h1h = h * 0.14f

    // Stilts
    for (s in 0..4) {
        drawLine(
            color = stiltWood,
            start = Offset(h1x + s * (h1w / 4), h1y + h1h),
            end = Offset(h1x + s * (h1w / 4), h1y + h1h + 26f),
            strokeWidth = 4f
        )
    }

    // House Body
    drawRect(color = houseRed, topLeft = Offset(h1x, h1y), size = Size(h1w, h1h))
    drawRect(color = houseShadow, topLeft = Offset(h1x + h1w * 0.6f, h1y), size = Size(h1w * 0.4f, h1h))

    // Roof
    val roofPath1 = Path().apply {
        moveTo(h1x - 10f, h1y)
        lineTo(h1x + h1w * 0.5f, h1y - 28f)
        lineTo(h1x + h1w + 10f, h1y)
        close()
    }
    drawPath(roofPath1, roofColor)

    // Glowing warm window
    val windowGlow = if (isAfter) Color(0xFFFEF08A) else Color(0xFFE2E8F0)
    drawRect(color = windowGlow, topLeft = Offset(h1x + 16f, h1y + 16f), size = Size(24f, 24f))

    // House 2 (Right Midground)
    val h2x = w * 0.60f
    val h2y = h * 0.64f
    val h2w = w * 0.22f
    val h2h = h * 0.10f

    for (s in 0..3) {
        drawLine(
            color = stiltWood,
            start = Offset(h2x + s * (h2w / 3), h2y + h2h),
            end = Offset(h2x + s * (h2w / 3), h2y + h2h + 18f),
            strokeWidth = 3f
        )
    }
    drawRect(color = houseRed, topLeft = Offset(h2x, h2y), size = Size(h2w, h2h))
    val roofPath2 = Path().apply {
        moveTo(h2x - 8f, h2y)
        lineTo(h2x + h2w * 0.5f, h2y - 20f)
        lineTo(h2x + h2w + 8f, h2y)
        close()
    }
    drawPath(roofPath2, roofColor)
}

private fun DrawScope.drawCyberpunkShortScene(
    isAfter: Boolean,
    unblur: Float,
    contrast: Float,
    warmth: Float,
    tint: Float,
    saturation: Float
) {
    val w = size.width
    val h = size.height

    // Dark Rainy Urban Night
    val bgColors = if (isAfter) listOf(Color(0xFF030712), Color(0xFF0F172A), Color(0xFF1E1B4B))
    else listOf(Color(0xFF1F2937), Color(0xFF111827))

    drawRect(brush = Brush.verticalGradient(bgColors))

    // Neon Signs & Tall Skyscrapers
    val cyanNeon = if (isAfter) Color(0xFF06B6D4) else Color(0xFF38BDF8)
    val magentaNeon = if (isAfter) Color(0xFFEC4899) else Color(0xFFF472B6)

    // Skyscrapers silhouettes
    drawRect(color = Color(0xFF090D16), topLeft = Offset(w * 0.05f, h * 0.15f), size = Size(w * 0.35f, h * 0.85f))
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(w * 0.55f, h * 0.10f), size = Size(w * 0.40f, h * 0.90f))

    // Glowing Neon Signs
    drawRect(color = cyanNeon, topLeft = Offset(w * 0.12f, h * 0.25f), size = Size(w * 0.18f, h * 0.04f))
    drawRect(color = magentaNeon, topLeft = Offset(w * 0.65f, h * 0.20f), size = Size(w * 0.22f, h * 0.06f))

    // Rain soaked street with luminous reflections
    val streetReflection = if (isAfter) {
        listOf(Color(0xFF0284C7).copy(alpha = 0.6f), Color(0xFFDB2777).copy(alpha = 0.5f), Color(0xFF030712))
    } else {
        listOf(Color(0xFF334155), Color(0xFF0F172A))
    }
    drawRect(
        brush = Brush.verticalGradient(streetReflection, startY = h * 0.65f, endY = h),
        topLeft = Offset(0f, h * 0.65f),
        size = Size(w, h * 0.35f)
    )

    // Motion streaks for 24fps short simulation
    if (isAfter) {
        drawLine(
            color = cyanNeon.copy(alpha = 0.7f),
            start = Offset(w * 0.2f, h * 0.78f),
            end = Offset(w * 0.75f, h * 0.78f),
            strokeWidth = 6f
        )
        drawLine(
            color = magentaNeon.copy(alpha = 0.6f),
            start = Offset(w * 0.35f, h * 0.84f),
            end = Offset(w * 0.85f, h * 0.84f),
            strokeWidth = 4f
        )
    }
}

private fun DrawScope.drawVintagePortraitScene(
    isAfter: Boolean,
    unblur: Float,
    contrast: Float,
    warmth: Float,
    tint: Float,
    saturation: Float
) {
    val w = size.width
    val h = size.height

    // Golden Hour Sunset Background
    val bg = if (isAfter) {
        listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFF78350F))
    } else {
        listOf(Color(0xFFFED7AA), Color(0xFFD1D5DB), Color(0xFF9CA3AF))
    }
    drawRect(brush = Brush.radialGradient(bg, center = Offset(w * 0.5f, h * 0.3f), radius = w * 0.8f))

    // Artist / Subject Silhouette with Rim Lighting
    val rimLight = if (isAfter) Color(0xFFFDE68A) else Color(0xFFF1F5F9)
    val skinTone = if (isAfter) Color(0xFFB45309) else Color(0xFF78716C)

    // Head
    drawCircle(color = rimLight, center = Offset(w * 0.5f, h * 0.40f), radius = w * 0.23f)
    drawCircle(color = skinTone, center = Offset(w * 0.49f, h * 0.40f), radius = w * 0.21f)

    // Shoulders
    val shoulderPath = Path().apply {
        moveTo(w * 0.15f, h)
        cubicTo(w * 0.25f, h * 0.60f, w * 0.75f, h * 0.60f, w * 0.85f, h)
        close()
    }
    drawPath(shoulderPath, if (isAfter) Color(0xFF451A03) else Color(0xFF334155))
}

private fun DrawScope.drawCinematicLetterbox(ratio: String, w: Float, h: Float) {
    if (ratio == "None") return
    val barHeight = when (ratio) {
        "2.39:1" -> h * 0.12f // Classic Cinemascope
        "16:9" -> h * 0.06f
        "4:3" -> h * 0.04f
        else -> 0f
    }
    if (barHeight > 0f) {
        drawRect(color = Color.Black, topLeft = Offset(0f, 0f), size = Size(w, barHeight))
        drawRect(color = Color.Black, topLeft = Offset(0f, h - barHeight), size = Size(w, barHeight))
    }
}

private fun DrawScope.drawFilmGrainOverlay(grain: Float, w: Float, h: Float) {
    val alpha = (grain / 100f * 0.18f).coerceIn(0.04f, 0.25f)
    val random = Random(42)
    for (i in 0..120) {
        val gx = random.nextFloat() * w
        val gy = random.nextFloat() * h
        val rad = random.nextFloat() * 1.8f + 0.8f
        drawCircle(
            color = if (i % 2 == 0) Color.White.copy(alpha = alpha) else Color.Black.copy(alpha = alpha),
            center = Offset(gx, gy),
            radius = rad
        )
    }
}

private fun DrawScope.drawCinematicVignette(vignette: Float, w: Float, h: Float) {
    val alpha = (vignette / 100f * 0.8f).coerceIn(0.1f, 0.9f)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = alpha)),
            center = Offset(w / 2f, h / 2f),
            radius = (w.coerceAtLeast(h)) * 0.7f
        )
    )
}

// Compose ColorMatrix for Cinematic Grading & LUTs.
// Delegates to [CinematicColorMatrix] so the live preview matches the exported
// bitmap render exactly (previously the two paths used different math).
fun buildComposeCinematicColorMatrix(mediaItem: MediaItemEntity): androidx.compose.ui.graphics.ColorMatrix =
    androidx.compose.ui.graphics.ColorMatrix(
        CinematicColorMatrix.compute(
            contrast = mediaItem.contrast,
            exposure = mediaItem.exposure,
            saturation = mediaItem.saturation,
            warmth = mediaItem.warmth,
            tint = mediaItem.tint,
            presetId = mediaItem.filterPresetId
        )
    )

fun buildComposeFlatRawColorMatrix(): androidx.compose.ui.graphics.ColorMatrix {
    val cm = androidx.compose.ui.graphics.ColorMatrix()
    // Flat camera sensor dynamic range representation
    cm.setToSaturation(0.75f)
    val flatMat = androidx.compose.ui.graphics.ColorMatrix(floatArrayOf(
        0.90f, 0f, 0f, 0f, 15f,
        0f, 0.90f, 0f, 0f, 15f,
        0f, 0f, 0.90f, 0f, 15f,
        0f, 0f, 0f, 1f, 0f
    ))
    cm.timesAssign(flatMat)
    return cm
}

/**
 * Decodes a local image file into an [ImageBitmap] on a background dispatcher,
 * downsampled to a sane maximum dimension to avoid jank and OutOfMemoryErrors
 * on large gallery/camera photos.
 */
@Composable
private fun rememberDecodedImage(uri: String?): ImageBitmap? {
    val image by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = if (uri == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                BitmapProcessor.decodeSampledBitmap(File(uri), maxDimension = 1800)?.asImageBitmap()
            }
        }
    }
    return image
}

/**
 * Interactive Before & After Slider Component.
 * Supports smooth dragging, tap-to-compare, and side-by-side mode.
 */
@Composable
fun BeforeAfterSlider(
    mediaItem: MediaItemEntity,
    modifier: Modifier = Modifier,
    initialSliderPosition: Float = 0.5f,
    showControls: Boolean = true
) {
    var sliderXFraction by remember { mutableFloatStateOf(initialSliderPosition) }
    var comparisonMode by remember { mutableStateOf("split") } // "split", "hold_before", "hold_after"

    val animatedSliderX by animateFloatAsState(
        targetValue = when (comparisonMode) {
            "hold_before" -> 1.0f
            "hold_after" -> 0.0f
            else -> sliderXFraction
        },
        animationSpec = tween(180),
        label = "slider_pos"
    )

    // Load local bitmap if available (off the main thread, downsampled)
    val imageBitmap: ImageBitmap? = rememberDecodedImage(mediaItem.localUri)

    // Color matrices for real-time graded AFTER vs flat RAW BEFORE
    val afterColorFilter = remember(mediaItem) {
        androidx.compose.ui.graphics.ColorFilter.colorMatrix(buildComposeCinematicColorMatrix(mediaItem))
    }
    val beforeColorFilter = remember {
        androidx.compose.ui.graphics.ColorFilter.colorMatrix(buildComposeFlatRawColorMatrix())
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CineObsidian)
    ) {
        val totalWidth = constraints.maxWidth.toFloat()
        val totalHeight = constraints.maxHeight.toFloat()
        val splitPx = (animatedSliderX * totalWidth).coerceIn(0f, totalWidth)

        // Render Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("before_after_canvas")
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        comparisonMode = "split"
                        sliderXFraction = (change.position.x / totalWidth).coerceIn(0.05f, 0.95f)
                    }
                }
        ) {
            if (imageBitmap != null) {
                val dstSize = IntSize(totalWidth.roundToInt(), totalHeight.roundToInt())
                // Draw AFTER (Graded + AI Enhanced) on full canvas
                drawImage(
                    image = imageBitmap,
                    dstSize = dstSize,
                    colorFilter = afterColorFilter
                )
                // Draw After grading overlays
                if (mediaItem.grain > 0f) {
                    drawFilmGrainOverlay(mediaItem.grain, totalWidth, totalHeight)
                }
                if (mediaItem.vignette > 0f) {
                    drawCinematicVignette(mediaItem.vignette, totalWidth, totalHeight)
                }
                drawCinematicLetterbox(mediaItem.letterboxRatio, totalWidth, totalHeight)

                // Clip & Draw BEFORE (Flat RAW original image) on left portion up to splitPx
                clipRect(left = 0f, top = 0f, right = splitPx, bottom = totalHeight) {
                    drawImage(
                        image = imageBitmap,
                        dstSize = dstSize,
                        colorFilter = beforeColorFilter
                    )
                }
            } else {
                // Draw AFTER (Processed) on full canvas
                drawCinematicScene(
                    sampleKey = mediaItem.sampleImageKey,
                    isAfter = true,
                    unblurStrength = mediaItem.unblurStrength,
                    contrast = mediaItem.contrast,
                    exposure = mediaItem.exposure,
                    saturation = mediaItem.saturation,
                    warmth = mediaItem.warmth,
                    tint = mediaItem.tint,
                    vignette = mediaItem.vignette,
                    grain = mediaItem.grain,
                    letterboxRatio = mediaItem.letterboxRatio
                )

                // Clip & Draw BEFORE (Raw/Unedited) on left portion up to splitPx
                clipRect(left = 0f, top = 0f, right = splitPx, bottom = totalHeight) {
                    drawCinematicScene(
                        sampleKey = mediaItem.sampleImageKey,
                        isAfter = false,
                        unblurStrength = 0f,
                        contrast = 0f,
                        exposure = 0f,
                        saturation = 0f,
                        warmth = 0f,
                        tint = 0f,
                        vignette = 0f,
                        grain = 0f,
                        letterboxRatio = mediaItem.letterboxRatio
                    )
                }
            }

            // Draw Divider Line
            if (comparisonMode == "split" && splitPx > 10f && splitPx < totalWidth - 10f) {
                drawLine(
                    color = CineGoldPrimary,
                    start = Offset(splitPx, 0f),
                    end = Offset(splitPx, totalHeight),
                    strokeWidth = 3.dp.toPx()
                )
                // Subtle shadow on line
                drawLine(
                    color = Color.Black.copy(alpha = 0.5f),
                    start = Offset(splitPx + 2f, 0f),
                    end = Offset(splitPx + 2f, totalHeight),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Draggable Handle Pill
        if (comparisonMode == "split") {
            Box(
                modifier = Modifier
                    .offset { IntOffset(splitPx.roundToInt() - 20.dp.roundToPx(), (totalHeight / 2f).roundToInt() - 20.dp.roundToPx()) }
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CineGoldPrimary)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Drag to compare Before and After",
                    tint = Color(0xFF21005D),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Pill Badges: "BEFORE" on left, "AFTER" on right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Surface(
                color = Color(0xFF1C1B1F).copy(alpha = 0.85f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, CineBorder),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "BEFORE (RAW)",
                    color = CineTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Surface(
                color = CineGoldPrimary,
                shape = CircleShape,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = "AFTER (${mediaItem.filterPresetName.take(16).uppercase()})",
                    color = Color(0xFF21005D),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
