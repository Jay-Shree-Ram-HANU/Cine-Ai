package com.example.ui.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.CineGoldPrimary
import com.example.ui.theme.CineObsidian
import com.example.ui.theme.CineRedRecord
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTealAccent
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showAuthModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CineObsidian)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
            .testTag("profile_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Director Profile & Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { showEditProfileDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = CineGoldPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CineSurface)
        )

        // Creator Profile Hero Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CineSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Gold Ring Avatar
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(CineGoldPrimary, CineTealAccent)))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(CineSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.avatarInitials,
                            color = CineGoldPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${user.handle} • ${user.email}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = CineGoldPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = CineGoldPrimary, modifier = Modifier.size(14.dp))
                        Text(
                            text = user.tier.uppercase(),
                            color = CineGoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = user.bio,
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showAuthModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CineSurfaceVariant, contentColor = CineTealAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Switch Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showApiKeyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CineGoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gemini API", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Studio Stats Summary
        Card(
            colors = CardDefaults.cardColors(containerColor = CineSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "STUDIO RENDER METRICS",
                    color = CineGoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total 4K Cinema Renders", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                    Text("${user.totalRenders} Exports", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("AI Unblur & Deconvolution Ops", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                    Text("${user.unblurCount} Ops", color = CineTealAccent, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Local Vault Storage", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                    Text("${user.storageUsedMb} MB / 10 GB", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Gemini AI Director Engine Status
        Card(
            colors = CardDefaults.cardColors(containerColor = CineSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { showApiKeyDialog = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CineGoldPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CineGoldPrimary)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Gemini 3.5 Flash Director", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        if (user.customApiKey != null) "Custom API Key Active" else "Default Antigravity AI Engine Connected",
                        color = CineTealAccent,
                        fontSize = 11.sp
                    )
                }

                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(user.name) }
        var bio by remember { mutableStateOf(user.bio) }
        var tier by remember { mutableStateOf(user.tier) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Director Profile", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Director Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Creator Bio & Camera Rig") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Text("Director Tier", color = CineGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Director Pro", "Cinematic Visionary", "Indie Creator").forEach { t ->
                            Surface(
                                color = if (tier == t) CineGoldPrimary else CineSurfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { tier = t }
                            ) {
                                Text(
                                    text = t,
                                    color = if (tier == t) Color.Black else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(name, bio, tier)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CineGoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Save Profile", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = CineSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Gemini API Key Dialog
    if (showApiKeyDialog) {
        var keyInput by remember { mutableStateOf(user.customApiKey ?: "") }

        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Gemini API Key Configuration", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter a custom Gemini API key or leave blank to use the pre-configured project credentials via BuildConfig.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setCustomApiKey(keyInput.ifBlank { null })
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CineGoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Save Key", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = CineSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Switch Account Modal
    if (showAuthModal) {
        val sampleAccounts = listOf(
            "Alex Nolan" to "alex.cinematics@studio.ai",
            "Christopher Nolan" to "chris.nolan@imax.film",
            "Denis Villeneuve" to "denis.v@dune.cinema",
            "Greta Gerwig" to "greta.g@wb.studio"
        )

        AlertDialog(
            onDismissRequest = { showAuthModal = false },
            title = { Text("Switch Director Account", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sampleAccounts.forEach { (name, email) ->
                        Surface(
                            color = if (user.email == email) CineGoldPrimary.copy(alpha = 0.2f) else CineSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (user.email == email) CineGoldPrimary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.login(email, name)
                                    showAuthModal = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = CineGoldPrimary)
                                Column {
                                    Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(email, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAuthModal = false }) {
                    Text("Close", color = CineGoldPrimary)
                }
            },
            containerColor = CineSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
