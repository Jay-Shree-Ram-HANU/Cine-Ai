package com.example.ui.screens.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.MediaType
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.theme.CineGoldPrimary
import com.example.ui.theme.CineObsidian
import com.example.ui.theme.CineRedRecord
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTealAccent
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: StudioViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToEditor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mediaList by viewModel.filteredMedia.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val onlyFavorites by viewModel.filterOnlyFavorites.collectAsState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importPhotoFromGallery(context, it) { newId ->
                onNavigateToEditor(newId)
            }
        }
    }

    var isGridView by remember { mutableStateOf(true) }
    var itemToDelete by remember { mutableStateOf<MediaItemEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CineObsidian)
            .testTag("library_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Media Vault",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Import Photo",
                        tint = CineTealAccent
                    )
                }
                IconButton(onClick = { isGridView = !isGridView }) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle View",
                        tint = CineGoldPrimary
                    )
                }
                IconButton(onClick = onNavigateToCamera) {
                    Icon(Icons.Default.Add, contentDescription = "New Shot", tint = CineGoldPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CineSurface)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search title or preset LUT...", color = Color(0xFF64748B), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CineGoldPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CineSurface,
                unfocusedContainerColor = CineSurface,
                focusedBorderColor = CineGoldPrimary,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All
            FilterChip(
                selected = filterType == null && !onlyFavorites,
                onClick = {
                    viewModel.filterType.value = null
                    viewModel.filterOnlyFavorites.value = false
                },
                label = { Text("All", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CineGoldPrimary,
                    selectedLabelColor = Color.Black,
                    containerColor = CineSurface,
                    labelColor = Color.White
                )
            )

            // Photos
            FilterChip(
                selected = filterType == MediaType.PHOTO && !onlyFavorites,
                onClick = {
                    viewModel.filterType.value = MediaType.PHOTO
                    viewModel.filterOnlyFavorites.value = false
                },
                label = { Text("Photos", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CineGoldPrimary,
                    selectedLabelColor = Color.Black,
                    containerColor = CineSurface,
                    labelColor = Color.White
                )
            )

            // Shorts
            FilterChip(
                selected = filterType == MediaType.SHORT && !onlyFavorites,
                onClick = {
                    viewModel.filterType.value = MediaType.SHORT
                    viewModel.filterOnlyFavorites.value = false
                },
                label = { Text("Shorts (24fps)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CineGoldPrimary,
                    selectedLabelColor = Color.Black,
                    containerColor = CineSurface,
                    labelColor = Color.White
                )
            )

            // Favorites
            FilterChip(
                selected = onlyFavorites,
                onClick = {
                    viewModel.filterOnlyFavorites.value = !onlyFavorites
                },
                label = { Text("★ Favorites", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CineTealAccent,
                    selectedLabelColor = Color.Black,
                    containerColor = CineSurface,
                    labelColor = Color.White
                )
            )
        }

        // Empty State or Media Grid
        if (mediaList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No media matches \"$searchQuery\"" else "Your Media Vault is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Capture a new cinematic photo or 24fps short with the AI Camera to get started.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToCamera,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CineGoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open AI Camera", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isGridView) 2 else 1),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(mediaList, key = { it.id }) { item ->
                    MediaVaultCard(
                        item = item,
                        onClick = { onNavigateToEditor(item.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(item.id) },
                        onDuplicate = { viewModel.duplicateMedia(item) },
                        onDelete = { itemToDelete = item }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Masterpiece?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to remove \"${item.title}\" from your Media Vault? This action cannot be undone.",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMedia(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CineRedRecord)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = CineSurface,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun MediaVaultCard(
    item: MediaItemEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // Visual Canvas Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                BeforeAfterSlider(
                    mediaItem = item,
                    initialSliderPosition = 0.0f,
                    modifier = Modifier.fillMaxSize()
                )

                // Top badges (Type & Heart)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (item.type == MediaType.SHORT) "SHORT 24FPS" else "PHOTO",
                            color = if (item.type == MediaType.SHORT) CineRedRecord else CineGoldPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(15.dp))
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) CineRedRecord else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Info & Details
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(CineSurfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Open in Colorist", color = Color.White, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = CineGoldPrimary) },
                                onClick = {
                                    showMenu = false
                                    onClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate", color = Color.White, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CineTealAccent) },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = CineRedRecord, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CineRedRecord) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

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
