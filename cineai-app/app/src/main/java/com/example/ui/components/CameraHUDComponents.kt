package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CineGoldPrimary
import com.example.ui.theme.CineGreenFocus
import com.example.ui.theme.CineObsidian
import com.example.ui.theme.CineRedRecord
import com.example.ui.theme.CineTealAccent

@Composable
fun HistogramIndicator(
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
            .width(84.dp)
            .height(44.dp)
            .testTag("histogram_indicator")
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val w = size.width
            val h = size.height

            // Simulated RGB/Luma waveform path
            val path = Path().apply {
                moveTo(0f, h)
                lineTo(w * 0.15f, h * 0.85f)
                lineTo(w * 0.35f, h * 0.25f)
                lineTo(w * 0.50f, h * 0.50f)
                lineTo(w * 0.70f, h * 0.15f)
                lineTo(w * 0.85f, h * 0.60f)
                lineTo(w, h * 0.90f)
                lineTo(w, h)
                close()
            }
            drawPath(
                path,
                brush = Brush.verticalGradient(
                    listOf(CineTealAccent.copy(alpha = 0.8f), CineGoldPrimary.copy(alpha = 0.3f)),
                    startY = 0f,
                    endY = h
                )
            )
            drawPath(path, color = CineTealAccent, style = Stroke(width = 1.5f))
        }
    }
}

@Composable
fun AudioLevelMeter(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_anim")
    val levelL by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "level_l"
    )
    val levelR by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(340, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "level_r"
    )

    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("MIC", color = Color(0xFF94A3B8), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // L Channel
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.dp)
                    .background(Color(0xFF334155), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(levelL)
                        .background(
                            if (levelL > 0.85f) CineRedRecord else CineGreenFocus,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
            // R Channel
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.dp)
                    .background(Color(0xFF334155), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(levelR)
                        .background(
                            if (levelR > 0.85f) CineRedRecord else CineGreenFocus,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun CameraShutterButton(
    isRecording: Boolean = false,
    isShortMode: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .size(76.dp)
            .testTag("camera_shutter_button")
            .clip(CircleShape)
            .border(
                width = 4.dp,
                color = if (isRecording) CineRedRecord.copy(alpha = pulseAlpha) else CineGoldPrimary,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(if (isRecording) RoundedCornerShape(8.dp) else CircleShape)
                .background(if (isShortMode || isRecording) CineRedRecord else Color.White)
        )
    }
}

@Composable
fun CameraGridOverlay(
    gridType: String = "rule_of_thirds", // "rule_of_thirds", "golden_ratio", "crosshair", "none"
    modifier: Modifier = Modifier
) {
    if (gridType == "none") return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val gridColor = Color.White.copy(alpha = 0.28f)

        when (gridType) {
            "rule_of_thirds" -> {
                // Horizontal lines
                drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth = 1f)
                // Vertical lines
                drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1f)
                drawLine(gridColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth = 1f)
            }
            "golden_ratio" -> {
                val phi1 = 0.382f
                val phi2 = 0.618f
                drawLine(gridColor, Offset(0f, h * phi1), Offset(w, h * phi1), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, h * phi2), Offset(w, h * phi2), strokeWidth = 1f)
                drawLine(gridColor, Offset(w * phi1, 0f), Offset(w * phi1, h), strokeWidth = 1f)
                drawLine(gridColor, Offset(w * phi2, 0f), Offset(w * phi2, h), strokeWidth = 1f)
            }
            "crosshair" -> {
                val cx = w / 2f
                val cy = h / 2f
                drawLine(CineGreenFocus.copy(alpha = 0.7f), Offset(cx - 20f, cy), Offset(cx + 20f, cy), strokeWidth = 1.5f)
                drawLine(CineGreenFocus.copy(alpha = 0.7f), Offset(cx, cy - 20f), Offset(cx, cy + 20f), strokeWidth = 1.5f)
                drawCircle(CineGreenFocus.copy(alpha = 0.5f), radius = 32f, center = Offset(cx, cy), style = Stroke(1f))
            }
        }
    }
}
