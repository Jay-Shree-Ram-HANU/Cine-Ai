package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.SampleDataProvider
import com.example.data.model.MediaItemEntity
import com.example.data.model.MediaType
import com.example.data.model.PresetEntity
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaRepository(private val database: AppDatabase) {

    val allMedia: Flow<List<MediaItemEntity>> = database.mediaDao().getAllMedia()
    val favoriteMedia: Flow<List<MediaItemEntity>> = database.mediaDao().getFavoriteMedia()
    val photos: Flow<List<MediaItemEntity>> = database.mediaDao().getPhotos()
    val cinematicShorts: Flow<List<MediaItemEntity>> = database.mediaDao().getCinematicShorts()

    fun getMediaByType(type: MediaType): Flow<List<MediaItemEntity>> =
        database.mediaDao().getMediaByType(type)

    fun searchMedia(query: String): Flow<List<MediaItemEntity>> =
        database.mediaDao().searchMedia(query)

    suspend fun getMediaById(id: String): MediaItemEntity? = withContext(Dispatchers.IO) {
        database.mediaDao().getMediaById(id)
    }

    suspend fun saveMedia(item: MediaItemEntity) = withContext(Dispatchers.IO) {
        database.mediaDao().insertMedia(item)
    }

    suspend fun updateMedia(item: MediaItemEntity) = withContext(Dispatchers.IO) {
        database.mediaDao().updateMedia(item)
    }

    suspend fun deleteMedia(item: MediaItemEntity) = withContext(Dispatchers.IO) {
        database.mediaDao().deleteMedia(item)
    }

    suspend fun deleteMediaById(id: String) = withContext(Dispatchers.IO) {
        database.mediaDao().deleteMediaById(id)
    }

    suspend fun deleteAllMedia() = withContext(Dispatchers.IO) {
        database.mediaDao().deleteAllMedia()
    }

    suspend fun resetToInitialSamples() = withContext(Dispatchers.IO) {
        database.mediaDao().deleteAllMedia()
        database.mediaDao().insertAll(SampleDataProvider.getInitialMediaItems())
    }

    suspend fun toggleFavorite(id: String) = withContext(Dispatchers.IO) {
        val item = database.mediaDao().getMediaById(id)
        if (item != null) {
            database.mediaDao().updateMedia(item.copy(isFavorite = !item.isFavorite, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        if (database.mediaDao().getCount() == 0) {
            database.mediaDao().insertAll(SampleDataProvider.getInitialMediaItems())
        }
    }
}

class PresetRepository(private val database: AppDatabase) {

    val allPresets: Flow<List<PresetEntity>> = database.presetDao().getAllPresets()

    suspend fun getPresetById(id: String): PresetEntity? = withContext(Dispatchers.IO) {
        database.presetDao().getPresetById(id)
    }

    suspend fun savePreset(preset: PresetEntity) = withContext(Dispatchers.IO) {
        database.presetDao().insertPreset(preset)
    }

    suspend fun updatePreset(preset: PresetEntity) = withContext(Dispatchers.IO) {
        database.presetDao().updatePreset(preset)
    }

    suspend fun deletePreset(preset: PresetEntity) = withContext(Dispatchers.IO) {
        database.presetDao().deletePreset(preset)
    }

    suspend fun deletePresetById(id: String) = withContext(Dispatchers.IO) {
        database.presetDao().deletePresetById(id)
    }

    suspend fun seedPresetsIfEmpty() = withContext(Dispatchers.IO) {
        if (database.presetDao().getCount() == 0) {
            database.presetDao().insertAllPresets(SampleDataProvider.getDefaultPresets())
        }
    }
}

class AuthRepository {
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "director_01",
            name = "Alex Nolan",
            handle = "@cine_alex",
            email = "alex.cinematics@studio.ai",
            tier = "Director Pro",
            avatarInitials = "AN",
            bio = "Cinematic creator shooting 4K 24fps anamorphic shorts & color graded landscapes.",
            isLoggedIn = true,
            totalRenders = 48,
            unblurCount = 132,
            storageUsedMb = 840.5f
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    fun updateProfile(name: String, bio: String, tier: String) {
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "C" }
        _currentUser.value = _currentUser.value.copy(
            name = name,
            bio = bio,
            tier = tier,
            avatarInitials = initials
        )
    }

    fun setCustomApiKey(key: String?) {
        _currentUser.value = _currentUser.value.copy(customApiKey = key)
    }

    fun login(email: String, name: String) {
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "AN" }
        _currentUser.value = _currentUser.value.copy(
            email = email,
            name = name,
            avatarInitials = initials,
            isLoggedIn = true
        )
    }

    fun logout() {
        _currentUser.value = _currentUser.value.copy(
            isLoggedIn = false
        )
    }

    fun incrementRenderCount() {
        _currentUser.value = _currentUser.value.copy(
            totalRenders = _currentUser.value.totalRenders + 1,
            storageUsedMb = _currentUser.value.storageUsedMb + 12.4f
        )
    }

    fun incrementUnblurCount() {
        _currentUser.value = _currentUser.value.copy(
            unblurCount = _currentUser.value.unblurCount + 1
        )
    }
}

object AppContainer {
    private var database: AppDatabase? = null
    lateinit var mediaRepository: MediaRepository
        private set
    lateinit var presetRepository: PresetRepository
        private set
    val authRepository: AuthRepository by lazy { AuthRepository() }

    fun initialize(context: Context) {
        if (database == null) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "cine_studio.db"
            ).fallbackToDestructiveMigration().build()
            database = db
            mediaRepository = MediaRepository(db)
            presetRepository = PresetRepository(db)

            CoroutineScope(Dispatchers.IO).launch {
                mediaRepository.seedInitialDataIfEmpty()
                presetRepository.seedPresetsIfEmpty()
            }
        }
    }
}
