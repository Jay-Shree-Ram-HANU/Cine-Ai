package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class MediaType {
    PHOTO,
    SHORT
}

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: MediaType = MediaType.PHOTO,
    val sampleImageKey: String = "landscape", // "landscape", "fjord", "cyberpunk", "portrait", "custom"
    val localUri: String? = null,
    val processedUri: String? = null,
    val durationSeconds: Int = 0,
    val resolution: String = "4K DCI (4096x1716)",
    val filterPresetId: String = "teal_orange",
    val filterPresetName: String = "Teal & Orange Blockbuster",
    val unblurStrength: Float = 65f, // 0 to 100
    val denoiseStrength: Float = 30f, // 0 to 100
    val contrast: Float = 25f, // -50 to 50
    val exposure: Float = 5f, // -50 to 50
    val saturation: Float = 20f, // -50 to 50
    val warmth: Float = 15f, // -50 to 50 (amber vs blue)
    val tint: Float = -10f, // -50 to 50 (magenta vs green)
    val vignette: Float = 35f, // 0 to 100
    val grain: Float = 15f, // 0 to 100
    val letterboxRatio: String = "2.39:1", // "2.39:1", "16:9", "9:16", "4:3", "None"
    val fps: Int = 24, // 24, 30, 60
    val isCinematicShort: Boolean = false,
    val aiDirectorNotes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "preset_items")
data class PresetEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // "Hollywood", "Atmospheric", "Cyber & Noir", "Vintage Film", "Custom"
    val description: String,
    val isCustom: Boolean = false,
    val unblurStrength: Float = 50f,
    val contrast: Float = 20f,
    val exposure: Float = 0f,
    val saturation: Float = 15f,
    val warmth: Float = 10f,
    val tint: Float = 0f,
    val vignette: Float = 30f,
    val grain: Float = 15f,
    val letterboxRatio: String = "2.39:1",
    val previewColorHex: String = "#F59E0B"
)

data class UserProfile(
    val id: String = "user_director_01",
    val name: String = "Alex Nolan",
    val handle: String = "@cine_alex",
    val email: String = "alex.cinematics@studio.ai",
    val tier: String = "Director Pro",
    val avatarInitials: String = "AN",
    val bio: String = "Cinematic shorts creator & color grading enthusiast. Shooting anamorphic in 4K 24fps.",
    val isLoggedIn: Boolean = true,
    val customApiKey: String? = null,
    val totalRenders: Int = 42,
    val unblurCount: Int = 128,
    val storageUsedMb: Float = 840.5f
)
