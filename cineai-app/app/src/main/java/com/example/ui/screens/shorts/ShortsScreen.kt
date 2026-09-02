package com.example.ui.screens.shorts

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItemEntity
import com.example.data.model.MediaType
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.theme.CineGoldPrimary
import com.example.ui.theme.CineObsidian
import com.example.ui.theme.CineRedRecord
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTealAccent
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun ShortsScreen(
    viewModel: StudioViewModel,
    onNavigateToEditor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allMedia by viewModel.allMedia.collectAsState()
    val isAiProcessing by viewModel.isAiProcessing.collectAsState()

    // Find first short item or default to cyberpunk short
    val currentShort = allMedia.firstOrNull { it.type == MediaType.SHORT } ?: MediaItemEntity(
        id = "short_tokyo_cyber",
        title = "Shibuya Rain Alleyway",
        type = MediaType.SHORT,
        sampleImageKey = "cyberpunk",
        filterPresetName = "Cyberpunk Neon Noir",
        fps = 24,
        isCinematicShort = true
    )

    var isPlaying by remember { mutableStateOf(true) }
    var enable24FpsCadence by remember { mutableStateOf(true) }
    var enableAnamorphicScope by remember { mutableStateOf(true) }
    var enableFilmHalation by remember { mutableStateOf(true) }
    var enableMotionBlur by remember { mutableStateOf(true) }
    var playbackProgress by remember { mutableFloatStateOf(0.42f) }

    val infiniteTransition = rememberInfiniteTransition(label = "playback_anim")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val activeProgress = if (isPlaying) animatedProgress else playbackProgress

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CineObsidian)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
            .testTag("shorts_screen")
    ) {
        // Header
        Surface(
            color = CineSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = CineRedRecord, modifier = Modifier.size(18.dp))
                        Text(
                            text = "CINEMATIC SHORTS CONVERTER",
                            color = CineRedRecord,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "24 FPS Anamorphic Engine",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = CineGoldPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "4K PRORES",
                        color = CineGoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Live Shorts Player Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp)
        ) {
            BeforeAfterSlider(
                mediaItem = currentShort.copy(
                    fps = if (enable24FpsCadence) 24 else 60,
                    letterboxRatio = if (enableAnamorphicScope) "2.39:1" else "None",
                    grain = if (enableFilmHalation) 24f else 0f
                ),
                modifier = Modifier.fillMaxSize()
            )

            // Live Play/Pause overlay indicator
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .clickable { isPlaying = !isPlaying }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }

            // Cadence Tag
            Surface(
                color = if (enable24FpsCadence) CineRedRecord else Color(0xFF475569),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = if (enable24FpsCadence) "24.00 FPS • CINEMA CADENCE" else "60.00 FPS • MOBILE RAW",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Playback Scrubber Bar
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("00:0${(activeProgress * 15).toInt()}:12", color = CineTealAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("00:15:00", color = Color(0xFF64748B), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = activeProgress,
                onValueChange = {
                    isPlaying = false
                    playbackProgress = it
                },
                colors = SliderDefaults.colors(
                    thumbColor = CineRedRecord,
                    activeTrackColor = CineRedRecord,
                    inactiveTrackColor = Color(0xFF334155)
                )
            )
        }

        // Convert Action Button
        Button(
            onClick = {
                viewModel.convertShortToCinematic(
                    currentShort,
                    enable24Fps = enable24FpsCadence,
                    enableAnamorphicScope = enableAnamorphicScope,
                    enableHalation = enableFilmHalation,
                    enableMotionBlur = enableMotionBlur
                )
            },
            enabled = !isAiProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = CineGoldPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(48.dp)
        ) {
            if (isAiProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transforming Short...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transform Short to 24fps Cinema", fontWeight = FontWeight.Bold)
            }
        }

        // Cinematic Settings Toggles
        Card(
            colors = CardDefaults.cardColors(containerColor = CineSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "CINEMATIC SHORT SETTINGS",
                    color = CineGoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )

                // 24fps Motion Cadence Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("24 FPS Motion Cadence", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Hollywood 180° shutter blur interpolation", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = enable24FpsCadence,
                        onCheckedChange = { enable24FpsCadence = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CineGoldPrimary,
                            checkedTrackColor = CineSurfaceVariant
                        )
                    )
                }

                // 2.39:1 Anamorphic Scope
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("2.39:1 Cinemascope Crop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Classic theatrical widescreen matte framing", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = enableAnamorphicScope,
                        onCheckedChange = { enableAnamorphicScope = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CineGoldPrimary,
                            checkedTrackColor = CineSurfaceVariant
                        )
                    )
                }

                // Halation & Analog Grain
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Film Halation & 35mm Grain", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Warm highlight blooming and organic emulsion", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = enableFilmHalation,
                        onCheckedChange = { enableFilmHalation = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CineGoldPrimary,
                            checkedTrackColor = CineSurfaceVariant
                        )
                    )
                }
            }
        }

        // Shorts Reel in Vault
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Shorts in Vault",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val shortsList = allMedia.filter { it.type == MediaType.SHORT }
            if (shortsList.isEmpty()) {
                Text(
                    text = "No shorts recorded yet. Use the AI Camera to record a 24fps short!",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )
            } else {
                shortsList.forEach { sItem ->
                    Surface(
                        color = CineSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onNavigateToEditor(sItem.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(sItem.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${sItem.filterPresetName} • ${sItem.fps} FPS", color = CineTealAccent, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.Tune, contentDescription = "Edit", tint = CineGoldPrimary)
                        }
                    }
                }
            }
        }
    }
}
