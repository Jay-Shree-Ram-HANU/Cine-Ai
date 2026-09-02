package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItemEntity
import com.example.data.model.MediaType
import com.example.data.model.PresetEntity
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.theme.CineBorder
import com.example.ui.theme.CineGoldLight
import com.example.ui.theme.CineGoldPrimary
import com.example.ui.theme.CineObsidian
import com.example.ui.theme.CinePrimaryContainer
import com.example.ui.theme.CineRedRecord
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTealAccent
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun DashboardScreen(
    viewModel: StudioViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToShorts: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToPresets: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mediaItems by viewModel.allMedia.collectAsState()
    val presets by viewModel.allPresets.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            viewModel.importPhotoFromGallery(context, it) { newId ->
                onNavigateToEditor(newId)
            }
        }
    }

    val featuredItem = mediaItems.firstOrNull() ?: MediaItemEntity(
        id = "demo_feat",
        title = "Valley Road Sunset",
        sampleImageKey = "landscape",
        filterPresetName = "Teal & Orange Blockbuster"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CineObsidian)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Director Header
        item {
            DirectorHeader(
                user = user,
                onProfileClick = onNavigateToProfile,
                onCameraClick = onNavigateToCamera
            )
        }

        // Metrics Row
        item {
            StudioMetricsGrid(
                totalRenders = user.totalRenders,
                unblurCount = user.unblurCount,
                presetCount = presets.size,
                storageMb = user.storageUsedMb
            )
        }

        // Hero AI Capture & Process Action
        item {
            HeroActionCard(
                onLaunchCamera = onNavigateToCamera,
                onImportPhoto = { galleryLauncher.launch("image/*") }
            )
        }

        // Featured Interactive Before & After
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI CINEMATIC TRANSFORMATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = CineGoldPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Before & After Color Grading",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (mediaItems.isNotEmpty()) {
                        Button(
                            onClick = { onNavigateToEditor(featuredItem.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CineSurfaceVariant,
                                contentColor = CineTealAccent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Open Colorist", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (mediaItems.isNotEmpty()) {
                    BeforeAfterSlider(
                        mediaItem = featuredItem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "👈 Drag handle to compare flat camera raw vs AI cinematic grade & unblur",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                } else {
                    // Empty state for Before/After transformation hero
                    DashboardEmptyFeaturedCard(
                        onLaunchCamera = onNavigateToCamera,
                        onLoadSamplePack = { viewModel.restoreSampleMediaPack() }
                    )
                }
            }
        }

        // Recent Cinematic Renders Horizontal Reel
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MASTERPIECES VAULT",
                            style = MaterialTheme.typography.labelMedium,
                            color = CineTealAccent,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Recent Cinematic Captures",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (mediaItems.isNotEmpty()) {
                        Text(
                            text = "View All (${mediaItems.size})",
                            color = CineGoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable(onClick = onNavigateToLibrary)
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (mediaItems.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mediaItems, key = { it.id }) { item ->
                            RecentItemCard(
                                item = item,
                                onClick = {
                                    if (item.type == MediaType.SHORT) {
                                        onNavigateToShorts()
                                    } else {
                                        onNavigateToEditor(item.id)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // Visual Empty State for Recent Captures Gallery
                    DashboardEmptyGalleryCard(
                        onLaunchCamera = onNavigateToCamera,
                        onLoadSamplePack = { viewModel.restoreSampleMediaPack() }
                    )
                }
            }
        }

        // Pre-programmed Cinematic LUT Presets Showcase
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COLOR SCIENCE PRESETS",
                            style = MaterialTheme.typography.labelMedium,
                            color = CineGoldPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Pre-Programmed LUTs",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Manage LUTs",
                        color = CineTealAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToPresets)
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(presets, key = { it.id }) { preset ->
                        PresetCardMini(
                            preset = preset,
                            onClick = {
                                if (featuredItem.id.isNotEmpty()) {
                                    viewModel.loadItemForEditing(featuredItem.id)
                                    viewModel.applyPresetToEditingItem(preset)
                                    onNavigateToEditor(featuredItem.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        // 24fps Shorts Converter Feature Banner
        item {
            ShortsBannerCard(onLaunchShorts = onNavigateToShorts)
        }
    }
}

@Composable
private fun DirectorHeader(
    user: com.example.data.model.UserProfile,
    onProfileClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Surface(
        color = CineObsidian,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.clickable(onClick = onProfileClick)
            ) {
                // Director Profile Avatar with Sophisticated Lilac Ring
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(2.dp, CineGoldPrimary, CircleShape)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(CineTealAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.avatarInitials,
                            color = Color(0xFF332D41),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "AI CineCam",
                            style = MaterialTheme.typography.titleMedium,
                            color = CineTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            color = CinePrimaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = user.tier.uppercase(),
                                color = CineGoldPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = "${user.name} • 24fps Cinema",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CineTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Capture CTA button in Sophisticated Lilac container
            Button(
                onClick = onCameraClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CineGoldPrimary,
                    contentColor = Color(0xFF21005D)
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("dashboard_launch_camera_btn")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Capture", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StudioMetricsGrid(
    totalRenders: Int,
    unblurCount: Int,
    presetCount: Int,
    storageMb: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricChip(
            label = "RENDERS",
            value = "$totalRenders",
            icon = Icons.Default.Movie,
            color = CineGoldPrimary,
            modifier = Modifier.weight(1f)
        )
        MetricChip(
            label = "AI UNBLUR",
            value = "$unblurCount",
            icon = Icons.Default.AutoAwesome,
            color = CineTealAccent,
            modifier = Modifier.weight(1f)
        )
        MetricChip(
            label = "AI LUTS",
            value = "$presetCount",
            icon = Icons.Default.Palette,
            color = Color(0xFFEC4899),
            modifier = Modifier.weight(1f)
        )
        MetricChip(
            label = "STORAGE",
            value = "${storageMb.toInt()}M",
            icon = Icons.Default.Storage,
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CineSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CineBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = CineTextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                color = CineTextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun HeroActionCard(
    onLaunchCamera: () -> Unit,
    onImportPhoto: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CineBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF381E72).copy(alpha = 0.5f),
                        CineSurface
                    )
                ),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = CinePrimaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "AI CINEMATIC COLOR ENGINE",
                        color = CineGoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "• 2.39:1 Scope",
                    color = CineTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Turn Any Photo into a Cinema Still",
                style = MaterialTheme.typography.titleLarge,
                color = CineTextPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Instant AI Deblur, Hollywood 35mm color grading, custom LUT styles, and anamorphic letterboxing.",
                style = MaterialTheme.typography.bodyMedium,
                color = CineTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onLaunchCamera,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CineGoldPrimary,
                        contentColor = Color(0xFF21005D)
                    ),
                    shape = CircleShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Camera", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onImportPhoto,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CineSurfaceVariant,
                        contentColor = CineTealAccent
                    ),
                    shape = CircleShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Photo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun RecentItemCard(
    item: MediaItemEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937)),
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            // Visual Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                BeforeAfterSlider(
                    mediaItem = item,
                    initialSliderPosition = 0.0f, // Show fully processed
                    modifier = Modifier.fillMaxSize()
                )
                // Type & FPS Tag
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (item.type == MediaType.SHORT) "SHORT • 24FPS" else "PHOTO • RAW",
                        color = if (item.type == MediaType.SHORT) CineRedRecord else CineGoldPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.filterPresetName,
                        color = CineTealAccent,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Unblur ${item.unblurStrength.toInt()}%",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCardMini(
    preset: PresetEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937)),
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
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
                    text = preset.category.uppercase(),
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = preset.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = preset.description,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ShortsBannerCard(onLaunchShorts: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onLaunchShorts)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CineRedRecord.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CineRedRecord, modifier = Modifier.size(28.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Normal Shorts ➔ Cinematic 24fps",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Apply 24fps motion blur, 2.39:1 scope, and halation to mobile videos.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
        }
    }
}

@Composable
private fun DashboardEmptyFeaturedCard(
    onLaunchCamera: () -> Unit,
    onLoadSamplePack: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(CineGoldPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = CineGoldPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = "No Active Renders in Vault",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Capture a high-res photo using CameraX or convert a video to see real-time AI color grading and unblur transformations.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onLaunchCamera,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CineGoldPrimary,
                        contentColor = Color(0xFF21005D)
                    ),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shoot Frame", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onLoadSamplePack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CineSurfaceVariant,
                        contentColor = CineTealAccent
                    ),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Load Sample Pack", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DashboardEmptyGalleryCard(
    onLaunchCamera: () -> Unit,
    onLoadSamplePack: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CineTealAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = CineTealAccent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vault Gallery is Empty",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Shoot 4K 24fps frames with CameraX or import photos to build your portfolio.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onLaunchCamera,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CineGoldPrimary,
                            contentColor = Color(0xFF21005D)
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Launch Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onLoadSamplePack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CineSurfaceVariant,
                            contentColor = CineTealAccent
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Demo Pack", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
