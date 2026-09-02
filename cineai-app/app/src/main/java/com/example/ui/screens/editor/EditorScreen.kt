package com.example.ui.screens.editor

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItemEntity
import com.example.data.model.PresetEntity
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.theme.CineBorder
import com.example.ui.theme.CineGoldPrimary
import com.example.ui.theme.CineObsidian
import com.example.ui.theme.CineRedRecord
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTealAccent
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    mediaId: String,
    viewModel: StudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(mediaId) {
        viewModel.loadItemForEditing(mediaId)
    }

    val itemState by viewModel.activeEditingItem.collectAsState()
    val presets by viewModel.allPresets.collectAsState()
    val isAiProcessing by viewModel.isAiProcessing.collectAsState()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("LUT Looks", "AI Unblur", "Color Tuning", "Export")

    val item = itemState ?: MediaItemEntity(
        id = mediaId,
        title = "Cinematic Shot",
        sampleImageKey = "landscape"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CineObsidian)
            .testTag("editor_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CineTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${item.filterPresetName} • Unblur ${item.unblurStrength.toInt()}%",
                        color = CineTealAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CineTextPrimary)
                }
            },
            actions = {
                IconButton(onClick = { viewModel.toggleFavorite(item.id) }) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) CineRedRecord else CineTextPrimary
                    )
                }

                Button(
                    onClick = { viewModel.saveEditingItemChanges() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CineGoldPrimary,
                        contentColor = Color(0xFF21005D)
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CineSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // Interactive Before & After Viewer with Draggable Handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                BeforeAfterSlider(
                    mediaItem = item,
                    initialSliderPosition = 0.5f,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = "👈 Drag slider to compare raw original vs AI cinematic output",
                color = CineTextSecondary,
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 6.dp)
            )

            // 1-Tap AI Magic Auto-Grade Button
            Surface(
                color = CineSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CineGoldPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "AI Auto-Enhance & Grade",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Auto-optimizes exposure, deblur strength, and cinematic contrast.",
                            color = CineTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.runAiDirectorAutoGrade(context) },
                        enabled = !isAiProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CineGoldPrimary,
                            contentColor = Color(0xFF21005D)
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isAiProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFF21005D), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Grading...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Auto-Grade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Simplified Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CineSurface,
                contentColor = CineGoldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CineGoldPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) CineGoldPrimary else CineTextSecondary,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> PresetsTabContent(
                    currentPresetId = item.filterPresetId,
                    presets = presets,
                    onSelectPreset = { viewModel.applyPresetToEditingItem(it) }
                )
                1 -> UnblurTabContent(
                    item = item,
                    onUpdate = { viewModel.updateEditingItem(it) }
                )
                2 -> ColorGradeTabContent(
                    item = item,
                    onUpdate = { viewModel.updateEditingItem(it) }
                )
                3 -> ExportTabContent(
                    item = item,
                    onExport = { viewModel.exportAndShareMedia(context, item) },
                    onDelete = {
                        viewModel.deleteMedia(item)
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetsTabContent(
    currentPresetId: String,
    presets: List<PresetEntity>,
    onSelectPreset: (PresetEntity) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Tap a style to instantly apply cinematic color LUT:",
            color = CineTextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(presets, key = { it.id }) { preset ->
                val isSelected = currentPresetId == preset.id
                Surface(
                    color = if (isSelected) CineSurfaceVariant else CineSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) CineGoldPrimary else Color(0xFF1F2937)
                    ),
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { onSelectPreset(preset) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.category.uppercase(),
                                color = CineTealAccent,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CineGoldPrimary, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = preset.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = preset.description,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            maxLines = 2,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnblurTabContent(
    item: MediaItemEntity,
    onUpdate: (MediaItemEntity) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // AI Edge Unblur
        SimpleEditorSlider(
            label = "AI Sharpness & Unblur",
            value = item.unblurStrength,
            range = 0f..100f,
            unit = "%",
            color = CineGoldPrimary,
            onValueChange = { onUpdate(item.copy(unblurStrength = it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // AI Denoise
        SimpleEditorSlider(
            label = "AI Smooth Denoise",
            value = item.denoiseStrength,
            range = 0f..100f,
            unit = "%",
            color = CineTealAccent,
            onValueChange = { onUpdate(item.copy(denoiseStrength = it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Film Grain
        SimpleEditorSlider(
            label = "35mm Film Grain Texture",
            value = item.grain,
            range = 0f..100f,
            unit = "%",
            color = Color(0xFFCBD5E1),
            onValueChange = { onUpdate(item.copy(grain = it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Vignette
        SimpleEditorSlider(
            label = "Cinematic Vignette (Edge Falloff)",
            value = item.vignette,
            range = 0f..100f,
            unit = "%",
            color = Color(0xFFCBD5E1),
            onValueChange = { onUpdate(item.copy(vignette = it)) }
        )
    }
}

@Composable
private fun ColorGradeTabContent(
    item: MediaItemEntity,
    onUpdate: (MediaItemEntity) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Warmth
        SimpleEditorSlider(
            label = "Golden Warmth vs Cool Blue",
            value = item.warmth,
            range = -50f..50f,
            unit = "",
            color = CineGoldPrimary,
            onValueChange = { onUpdate(item.copy(warmth = it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Contrast
        SimpleEditorSlider(
            label = "Cinematic Contrast",
            value = item.contrast,
            range = -50f..50f,
            unit = "%",
            color = CineGoldPrimary,
            onValueChange = { onUpdate(item.copy(contrast = it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Saturation
        SimpleEditorSlider(
            label = "Color Saturation & Richness",
            value = item.saturation,
            range = -50f..50f,
            unit = "%",
            color = CineTealAccent,
            onValueChange = { onUpdate(item.copy(saturation = it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Exposure
        SimpleEditorSlider(
            label = "Brightness & Exposure",
            value = item.exposure,
            range = -50f..50f,
            unit = "",
            color = Color(0xFFCBD5E1),
            onValueChange = { onUpdate(item.copy(exposure = it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cinemascope Aspect Ratio
        Text("Cinema Letterbox Scope", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("2.39:1", "16:9", "4:3", "None").forEach { ratio ->
                val isSel = item.letterboxRatio == ratio
                Surface(
                    color = if (isSel) CineGoldPrimary else CineSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onUpdate(item.copy(letterboxRatio = ratio)) }
                ) {
                    Text(
                        text = ratio,
                        color = if (isSel) Color(0xFF21005D) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportTabContent(
    item: MediaItemEntity,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Export & Share Master", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Render high-resolution cinematic JPEG/Short with all color grades and unblur applied.",
            color = CineTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onExport,
            colors = ButtonDefaults.buttonColors(
                containerColor = CineGoldPrimary,
                contentColor = Color(0xFF21005D)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export & Share Image", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(
                containerColor = CineRedRecord.copy(alpha = 0.15f),
                contentColor = CineRedRecord
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Delete from Vault", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SimpleEditorSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                "${if (value > 0 && range.start < 0) "+" else ""}${value.toInt()}$unit",
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0xFF334155)
            )
        )
    }
}
