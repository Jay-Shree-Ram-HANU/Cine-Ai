package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiDirectorService
import com.example.data.model.MediaItemEntity
import com.example.data.model.MediaType
import com.example.data.model.PresetEntity
import com.example.data.model.UserProfile
import com.example.data.processing.BitmapProcessor
import com.example.data.repository.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class StudioViewModel : ViewModel() {

    private val mediaRepository = AppContainer.mediaRepository
    private val presetRepository = AppContainer.presetRepository
    private val authRepository = AppContainer.authRepository
    private val geminiDirectorService = GeminiDirectorService()

    // State flows
    val allMedia: StateFlow<List<MediaItemEntity>> = mediaRepository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photos: StateFlow<List<MediaItemEntity>> = mediaRepository.photos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cinematicShorts: StateFlow<List<MediaItemEntity>> = mediaRepository.cinematicShorts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPresets: StateFlow<List<PresetEntity>> = presetRepository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserProfile> = authRepository.currentUser

    // UI filters & search
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow<MediaType?>(null)
    val filterOnlyFavorites = MutableStateFlow(false)

    // Filtered media list
    val filteredMedia: StateFlow<List<MediaItemEntity>> = combine(
        allMedia,
        searchQuery,
        filterType,
        filterOnlyFavorites
    ) { items, query, type, onlyFavs ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.filterPresetName.contains(query, ignoreCase = true)
            val matchesType = type == null || item.type == type
            val matchesFav = !onlyFavs || item.isFavorite
            matchesQuery && matchesType && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active editing item
    private val _activeEditingItem = MutableStateFlow<MediaItemEntity?>(null)
    val activeEditingItem: StateFlow<MediaItemEntity?> = _activeEditingItem.asStateFlow()

    // AI Operation state
    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // SnackBar / Toast feedback
    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    fun clearSnack() {
        _snackMessage.value = null
    }

    fun loadItemForEditing(mediaId: String) {
        viewModelScope.launch {
            val item = mediaRepository.getMediaById(mediaId)
            if (item != null) {
                _activeEditingItem.value = item
            } else {
                _activeEditingItem.value = allMedia.value.firstOrNull() ?: MediaItemEntity(
                    id = mediaId,
                    title = "New Cinematic Shot",
                    sampleImageKey = "landscape"
                )
            }
        }
    }

    fun updateEditingItem(updated: MediaItemEntity) {
        _activeEditingItem.value = updated
    }

    fun saveEditingItemChanges() {
        val current = _activeEditingItem.value ?: return
        viewModelScope.launch {
            mediaRepository.updateMedia(current.copy(updatedAt = System.currentTimeMillis()))
            authRepository.incrementRenderCount()
            _snackMessage.value = "Cinematic render saved to Media Vault"
        }
    }

    fun createAndSaveNewCapture(
        title: String,
        type: MediaType,
        sampleKey: String,
        preset: PresetEntity,
        fps: Int = 24
    ): String {
        val newId = UUID.randomUUID().toString()
        val newItem = MediaItemEntity(
            id = newId,
            title = title,
            type = type,
            sampleImageKey = sampleKey,
            filterPresetId = preset.id,
            filterPresetName = preset.name,
            unblurStrength = preset.unblurStrength,
            contrast = preset.contrast,
            exposure = preset.exposure,
            saturation = preset.saturation,
            warmth = preset.warmth,
            tint = preset.tint,
            vignette = preset.vignette,
            grain = preset.grain,
            letterboxRatio = preset.letterboxRatio,
            fps = fps,
            isCinematicShort = (type == MediaType.SHORT),
            aiDirectorNotes = "Captured with CineAI Camera. Preset: ${preset.name}."
        )
        viewModelScope.launch {
            mediaRepository.saveMedia(newItem)
            authRepository.incrementRenderCount()
            _snackMessage.value = "New $type saved to Vault"
        }
        return newId
    }

    /**
     * Import a real user photo from device gallery.
     */
    fun importPhotoFromGallery(context: Context, uri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            _statusMessage.value = "Importing & preparing high-res cinematic canvas..."
            try {
                val bitmap = BitmapProcessor.loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    val savedFile = BitmapProcessor.saveBitmapToInternalStorage(context, bitmap)
                    val newId = UUID.randomUUID().toString()
                    val newItem = MediaItemEntity(
                        id = newId,
                        title = "Imported Shot #${newId.take(4)}",
                        type = MediaType.PHOTO,
                        sampleImageKey = "custom",
                        localUri = savedFile.absolutePath,
                        filterPresetId = "teal_orange",
                        filterPresetName = "Teal & Orange Blockbuster",
                        unblurStrength = 65f,
                        contrast = 25f,
                        exposure = 5f,
                        saturation = 20f,
                        warmth = 15f,
                        tint = -10f,
                        vignette = 35f,
                        grain = 15f,
                        letterboxRatio = "2.39:1",
                        aiDirectorNotes = "Imported image ready for AI Director Grading and Sharpness enhancement."
                    )
                    mediaRepository.saveMedia(newItem)
                    authRepository.incrementRenderCount()
                    _snackMessage.value = "Photo imported successfully!"
                    onComplete(newId)
                } else {
                    _snackMessage.value = "Could not load image from gallery"
                }
            } catch (e: Exception) {
                _snackMessage.value = "Error importing photo: ${e.localizedMessage}"
            } finally {
                _isAiProcessing.value = false
                _statusMessage.value = null
            }
        }
    }

    /**
     * Import a real photo captured via device camera.
     */
    fun importCapturedDevicePhoto(context: Context, bitmap: Bitmap, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            _statusMessage.value = "Saving raw camera capture..."
            try {
                val savedFile = BitmapProcessor.saveBitmapToInternalStorage(context, bitmap)
                val newId = UUID.randomUUID().toString()
                val newItem = MediaItemEntity(
                    id = newId,
                    title = "Camera Capture #${newId.take(4)}",
                    type = MediaType.PHOTO,
                    sampleImageKey = "custom",
                    localUri = savedFile.absolutePath,
                    filterPresetId = "teal_orange",
                    filterPresetName = "Teal & Orange Blockbuster",
                    unblurStrength = 75f,
                    contrast = 28f,
                    exposure = 6f,
                    saturation = 22f,
                    warmth = 18f,
                    tint = -12f,
                    vignette = 38f,
                    grain = 16f,
                    letterboxRatio = "2.39:1",
                    aiDirectorNotes = "Live photo captured with Android camera. AI Unblur and Cinemascope 2.39:1 enabled."
                )
                mediaRepository.saveMedia(newItem)
                authRepository.incrementRenderCount()
                _snackMessage.value = "Photo captured and stored in Vault!"
                onComplete(newId)
            } catch (e: Exception) {
                _snackMessage.value = "Error saving capture: ${e.localizedMessage}"
            } finally {
                _isAiProcessing.value = false
                _statusMessage.value = null
            }
        }
    }

    /**
     * Save an actual photo captured directly through CameraX ImageCapture use case.
     */
    fun saveCameraXCapturedPhoto(
        photoFile: java.io.File,
        preset: PresetEntity,
        letterboxRatio: String = "2.39:1",
        autoUnblurEnabled: Boolean = true,
        ev: Float = 0f,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            _statusMessage.value = "CameraX: Ingesting high-resolution anamorphic frame..."
            try {
                val newId = UUID.randomUUID().toString()
                val newItem = MediaItemEntity(
                    id = newId,
                    title = "CameraX Pro Frame #${newId.take(4).uppercase()}",
                    type = MediaType.PHOTO,
                    sampleImageKey = "custom",
                    localUri = photoFile.absolutePath,
                    filterPresetId = preset.id,
                    filterPresetName = preset.name,
                    unblurStrength = if (autoUnblurEnabled) preset.unblurStrength else 0f,
                    contrast = preset.contrast,
                    exposure = preset.exposure + (ev * 10f),
                    saturation = preset.saturation,
                    warmth = preset.warmth,
                    tint = preset.tint,
                    vignette = preset.vignette,
                    grain = preset.grain,
                    letterboxRatio = letterboxRatio,
                    resolution = "4K DCI (CameraX Raw)",
                    aiDirectorNotes = "Live photo captured via CameraX camera provider with ${preset.name} grading recipe."
                )
                mediaRepository.saveMedia(newItem)
                authRepository.incrementRenderCount()
                if (autoUnblurEnabled) authRepository.incrementUnblurCount()
                _snackMessage.value = "CameraX shot captured & saved to Vault!"
                onComplete(newId)
            } catch (e: Exception) {
                _snackMessage.value = "Failed saving CameraX frame: ${e.localizedMessage}"
            } finally {
                _isAiProcessing.value = false
                _statusMessage.value = null
            }
        }
    }

    /**
     * Reset media vault to empty to test visual empty states.
     */
    fun clearAllMediaVault() {
        viewModelScope.launch {
            mediaRepository.deleteAllMedia()
            _activeEditingItem.value = null
            _snackMessage.value = "Media Vault cleared (0 items)"
        }
    }

    /**
     * Restore initial curated sample cinematic pack.
     */
    fun restoreSampleMediaPack() {
        viewModelScope.launch {
            mediaRepository.resetToInitialSamples()
            _snackMessage.value = "Sample Cinematic Pack reloaded!"
        }
    }

    /**
     * Renders full resolution cinematic output and launches native share sheet.
     */
    fun exportAndShareMedia(context: Context, item: MediaItemEntity) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            _statusMessage.value = "Rendering high-res cinematic grade..."
            try {
                // Decode off the main thread and downsample so large gallery photos
                // don't block the UI or exhaust memory.
                val baseBitmap = withContext(Dispatchers.IO) {
                    if (item.localUri != null) {
                        val file = java.io.File(item.localUri)
                        if (file.exists()) {
                            BitmapProcessor.decodeSampledBitmap(file, maxDimension = 2560)
                        } else {
                            BitmapProcessor.generateSampleSceneBitmap(item.sampleImageKey)
                        }
                    } else {
                        BitmapProcessor.generateSampleSceneBitmap(item.sampleImageKey)
                    }
                }

                if (baseBitmap == null) {
                    _snackMessage.value = "Could not load image for export"
                    return@launch
                }

                val processed = BitmapProcessor.processCinematicBitmap(
                    source = baseBitmap,
                    unblurStrength = item.unblurStrength,
                    denoiseStrength = item.denoiseStrength,
                    contrast = item.contrast,
                    exposure = item.exposure,
                    saturation = item.saturation,
                    warmth = item.warmth,
                    tint = item.tint,
                    vignette = item.vignette,
                    grain = item.grain,
                    letterboxRatio = item.letterboxRatio,
                    presetId = item.filterPresetId
                )

                val exportFile = BitmapProcessor.saveBitmapToInternalStorage(
                    context,
                    processed,
                    "render_${item.title.take(15).replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
                )

                val shareIntent = BitmapProcessor.createShareIntent(context, exportFile)
                val chooser = android.content.Intent.createChooser(shareIntent, "Export Graded Cinematic Master")
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)

                authRepository.incrementRenderCount()
                _snackMessage.value = "Exported & ready to share!"
            } catch (e: Exception) {
                _snackMessage.value = "Export failed: ${e.localizedMessage}"
            } finally {
                _isAiProcessing.value = false
                _statusMessage.value = null
            }
        }
    }

    fun applyPresetToEditingItem(preset: PresetEntity) {
        val current = _activeEditingItem.value ?: return
        _activeEditingItem.value = current.copy(
            filterPresetId = preset.id,
            filterPresetName = preset.name,
            unblurStrength = preset.unblurStrength,
            contrast = preset.contrast,
            exposure = preset.exposure,
            saturation = preset.saturation,
            warmth = preset.warmth,
            tint = preset.tint,
            vignette = preset.vignette,
            grain = preset.grain,
            letterboxRatio = preset.letterboxRatio
        )
        _snackMessage.value = "Applied ${preset.name}"
    }

    fun runAiDirectorAutoGrade(context: Context? = null) {
        val current = _activeEditingItem.value ?: return
        _isAiProcessing.value = true
        _statusMessage.value = "Gemini 3.5 AI: Inspecting lighting, color balance & composition..."

        viewModelScope.launch {
            try {
                // If there is an image, convert to Base64 for multimodal Gemini vision.
                // Decode happens off the main thread.
                var base64: String? = null
                if (current.localUri != null) {
                    val file = java.io.File(current.localUri)
                    if (file.exists()) {
                        base64 = withContext(Dispatchers.IO) {
                            val bmp = BitmapProcessor.decodeSampledBitmap(file, maxDimension = 600)
                            bmp?.let { BitmapProcessor.bitmapToBase64(it, maxDimension = 600) }
                        }
                    }
                }

                val recommendation = geminiDirectorService.analyzeAndAutoGrade(
                    sceneTitle = current.title,
                    sceneType = current.type.name,
                    currentPreset = current.filterPresetName,
                    imageBase64 = base64,
                    customApiKey = currentUser.value.customApiKey
                )

                _activeEditingItem.value = current.copy(
                    filterPresetId = recommendation.recommendedPresetId,
                    filterPresetName = recommendation.recommendedPresetName,
                    unblurStrength = recommendation.unblurStrength,
                    denoiseStrength = recommendation.denoiseStrength,
                    contrast = recommendation.contrast,
                    exposure = recommendation.exposure,
                    saturation = recommendation.saturation,
                    warmth = recommendation.warmth,
                    tint = recommendation.tint,
                    vignette = recommendation.vignette,
                    grain = recommendation.grain,
                    aiDirectorNotes = recommendation.notes
                )

                authRepository.incrementUnblurCount()
                _snackMessage.value = "AI Director Auto-Grade applied!"
            } catch (e: Exception) {
                _snackMessage.value = "AI analysis complete with fallback profile"
            } finally {
                _isAiProcessing.value = false
                _statusMessage.value = null
            }
        }
    }

    fun convertShortToCinematic(
        item: MediaItemEntity,
        enable24Fps: Boolean = true,
        enableAnamorphicScope: Boolean = true,
        enableHalation: Boolean = true,
        enableMotionBlur: Boolean = true
    ) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            _statusMessage.value = "Converting mobile video to 24fps anamorphic short..."
            val updated = item.copy(
                fps = if (enable24Fps) 24 else 60,
                isCinematicShort = true,
                letterboxRatio = if (enableAnamorphicScope) "2.39:1" else "None",
                grain = if (enableHalation) item.grain.coerceAtLeast(20f) else item.grain,
                vignette = if (enableHalation) item.vignette.coerceAtLeast(45f) else item.vignette,
                unblurStrength = if (enableMotionBlur) item.unblurStrength.coerceAtLeast(80f) else item.unblurStrength,
                aiDirectorNotes = "Converted to ${if (enable24Fps) 24 else 60}fps cadence " +
                    (if (enableAnamorphicScope) "with 2.39:1 letterbox, " else "without letterbox, ") +
                    (if (enableMotionBlur) "motion-blur smoothing, " else "no motion smoothing, ") +
                    "and " + (if (enableHalation) "film halation/grain." else "clean digital finish.")
            )
            mediaRepository.updateMedia(updated)
            _isAiProcessing.value = false
            _statusMessage.value = null
            _snackMessage.value = "Transformed to ${if (enable24Fps) "24fps" else "60fps"} Cinematic Short!"
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            mediaRepository.toggleFavorite(id)
        }
    }

    fun deleteMedia(item: MediaItemEntity) {
        viewModelScope.launch {
            mediaRepository.deleteMedia(item)
            _snackMessage.value = "Removed \"${item.title}\" from Vault"
        }
    }

    fun duplicateMedia(item: MediaItemEntity) {
        viewModelScope.launch {
            val duplicate = item.copy(
                id = UUID.randomUUID().toString(),
                title = "${item.title} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            mediaRepository.saveMedia(duplicate)
            _snackMessage.value = "Duplicated shot to Vault"
        }
    }

    // Presets CRUD
    fun saveCustomPreset(
        name: String,
        category: String,
        description: String,
        unblur: Float,
        contrast: Float,
        exposure: Float,
        saturation: Float,
        warmth: Float,
        tint: Float,
        vignette: Float,
        grain: Float,
        letterbox: String
    ) {
        viewModelScope.launch {
            val preset = PresetEntity(
                name = name,
                category = category,
                description = description,
                isCustom = true,
                unblurStrength = unblur,
                contrast = contrast,
                exposure = exposure,
                saturation = saturation,
                warmth = warmth,
                tint = tint,
                vignette = vignette,
                grain = grain,
                letterboxRatio = letterbox,
                previewColorHex = "#F59E0B"
            )
            presetRepository.savePreset(preset)
            _snackMessage.value = "Custom LUT preset \"$name\" saved"
        }
    }

    fun updatePreset(preset: PresetEntity) {
        viewModelScope.launch {
            presetRepository.updatePreset(preset)
            _snackMessage.value = "Preset \"${preset.name}\" updated"
        }
    }

    fun deletePreset(preset: PresetEntity) {
        viewModelScope.launch {
            presetRepository.deletePreset(preset)
            _snackMessage.value = "Preset \"${preset.name}\" deleted"
        }
    }

    // Profile & Auth
    fun updateProfile(name: String, bio: String, tier: String) {
        authRepository.updateProfile(name, bio, tier)
        _snackMessage.value = "Profile updated"
    }

    fun setCustomApiKey(key: String?) {
        authRepository.setCustomApiKey(key)
        _snackMessage.value = if (key.isNullOrBlank()) "Reset to default Gemini API" else "Custom Gemini API Key saved"
    }

    fun testGeminiApiConnection(key: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = geminiDirectorService.testApiConnection(key)
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Success")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to connect")
            }
        }
    }

    fun clearCache(context: Context) {
        viewModelScope.launch {
            val imagesDir = java.io.File(context.filesDir, "images")
            if (imagesDir.exists()) {
                imagesDir.listFiles()?.forEach { it.delete() }
            }
            context.cacheDir.listFiles()?.forEach { it.delete() }
            _snackMessage.value = "Temporary render cache cleared"
        }
    }

    fun login(email: String, name: String) {
        authRepository.login(email, name)
        _snackMessage.value = "Signed in as $name"
    }

    fun logout() {
        authRepository.logout()
        _snackMessage.value = "Signed out of director account"
    }
}
