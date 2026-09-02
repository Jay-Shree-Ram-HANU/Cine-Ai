package com.example.ui.screens.presets

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PresetEntity
import com.example.ui.theme.CineGoldPrimary
import com.example.ui.theme.CineObsidian
import com.example.ui.theme.CineRedRecord
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTealAccent
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsScreen(
    viewModel: StudioViewModel,
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets by viewModel.allPresets.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<PresetEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CineObsidian)
            .testTag("presets_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "LUT Presets Manager",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create LUT", tint = CineGoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CineSurface)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Hollywood & Atmospheric LUTs (${presets.size})",
                        color = CineGoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                items(presets, key = { it.id }) { preset ->
                    PresetDetailCard(
                        preset = preset,
                        onDelete = { presetToDelete = preset }
                    )
                }
            }
        }

        // Floating Action Button to Create New LUT
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = CineGoldPrimary,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New LUT Preset")
        }
    }

    // Create New Custom Preset Dialog
    if (showCreateDialog) {
        CreatePresetDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { name, cat, desc, unblur, contrast, exp, sat, warmth, tint, vig, grain, letterbox ->
                viewModel.saveCustomPreset(
                    name = name,
                    category = cat,
                    description = desc,
                    unblur = unblur,
                    contrast = contrast,
                    exposure = exp,
                    saturation = sat,
                    warmth = warmth,
                    tint = tint,
                    vignette = vig,
                    grain = grain,
                    letterbox = letterbox
                )
                showCreateDialog = false
            }
        )
    }

    // Delete Confirmation
    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text("Delete Preset?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${preset.name}\"?", color = Color(0xFFCBD5E1)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePreset(preset)
                        presetToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CineRedRecord)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = CineSurface,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun PresetDetailCard(
    preset: PresetEntity,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when (preset.id) {
                                    "teal_orange" -> CineGoldPrimary
                                    "nordic_emerald" -> CineTealAccent
                                    "cyberpunk_neon" -> Color(0xFFEC4899)
                                    "vintage_35mm" -> Color(0xFFD97706)
                                    "monochrome_noir" -> Color(0xFF94A3B8)
                                    else -> Color(0xFF38BDF8)
                                }
                            )
                    )
                    Text(
                        text = preset.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (preset.isCustom) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CineRedRecord, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Surface(
                        color = CineSurfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = preset.category.uppercase(),
                            color = CineTealAccent,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = preset.description,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Grading Specs Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SpecPill(label = "UNBLUR", value = "${preset.unblurStrength.toInt()}%", color = CineTealAccent)
                SpecPill(label = "CONTRAST", value = "${preset.contrast.toInt()}", color = CineGoldPrimary)
                SpecPill(label = "WARMTH", value = "${preset.warmth.toInt()}", color = CineGoldPrimary)
                SpecPill(label = "RATIO", value = preset.letterboxRatio, color = Color(0xFFCBD5E1))
            }
        }
    }
}

@Composable
private fun SpecPill(label: String, value: String, color: Color) {
    Surface(
        color = CineSurfaceVariant,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = "$label: $value",
            color = color,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun CreatePresetDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, cat: String, desc: String, unblur: Float, contrast: Float, exp: Float, sat: Float, warmth: Float, tint: Float, vig: Float, grain: Float, letterbox: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var unblur by remember { mutableFloatStateOf(65f) }
    var contrast by remember { mutableFloatStateOf(25f) }
    var exposure by remember { mutableFloatStateOf(5f) }
    var saturation by remember { mutableFloatStateOf(20f) }
    var warmth by remember { mutableFloatStateOf(15f) }
    var tint by remember { mutableFloatStateOf(-10f) }
    var vignette by remember { mutableFloatStateOf(35f) }
    var grain by remember { mutableFloatStateOf(15f) }
    var letterbox by remember { mutableStateOf("2.39:1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Director LUT", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("LUT Preset Name") },
                    placeholder = { Text("e.g., Midnight Sahara Gold") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CineGoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Mood") },
                    placeholder = { Text("e.g., Golden hour amber tones with deep cyan shadows") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CineGoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("AI Unblur: ${unblur.toInt()}%", color = CineTealAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = unblur,
                    onValueChange = { unblur = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = CineTealAccent, activeTrackColor = CineTealAccent)
                )

                Text("Contrast: ${contrast.toInt()}%", color = CineGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = contrast,
                    onValueChange = { contrast = it },
                    valueRange = -50f..50f,
                    colors = SliderDefaults.colors(thumbColor = CineGoldPrimary, activeTrackColor = CineGoldPrimary)
                )

                Text("Warmth (Amber vs Blue): ${warmth.toInt()}", color = CineGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = warmth,
                    onValueChange = { warmth = it },
                    valueRange = -50f..50f,
                    colors = SliderDefaults.colors(thumbColor = CineGoldPrimary, activeTrackColor = CineGoldPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name,
                            "Custom",
                            description.ifBlank { "Custom director grade LUT" },
                            unblur,
                            contrast,
                            exposure,
                            saturation,
                            warmth,
                            tint,
                            vignette,
                            grain,
                            letterbox
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CineGoldPrimary, contentColor = Color.Black)
            ) {
                Text("Save LUT", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = CineSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
