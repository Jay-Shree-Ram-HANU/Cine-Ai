package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.data.model.MediaItemEntity
import com.example.data.model.MediaType
import com.example.data.model.PresetEntity
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = try {
        MediaType.valueOf(value)
    } catch (e: Exception) {
        MediaType.PHOTO
    }
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY createdAt DESC")
    fun getAllMedia(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY createdAt DESC")
    fun getMediaByType(type: MediaType): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteMedia(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE type = 'PHOTO' ORDER BY createdAt DESC")
    fun getPhotos(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE type = 'SHORT' OR isCinematicShort = 1 ORDER BY createdAt DESC")
    fun getCinematicShorts(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaById(id: String): MediaItemEntity?

    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%' OR filterPresetName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchMedia(query: String): Flow<List<MediaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItemEntity>)

    @Update
    suspend fun updateMedia(item: MediaItemEntity)

    @Delete
    suspend fun deleteMedia(item: MediaItemEntity)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: String)

    @Query("DELETE FROM media_items")
    suspend fun deleteAllMedia()

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE type = 'SHORT' OR isCinematicShort = 1")
    suspend fun getShortsCount(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE type = 'PHOTO'")
    suspend fun getPhotosCount(): Int
}

@Dao
interface PresetDao {
    @Query("SELECT * FROM preset_items ORDER BY isCustom DESC, name ASC")
    fun getAllPresets(): Flow<List<PresetEntity>>

    @Query("SELECT * FROM preset_items WHERE id = :id")
    suspend fun getPresetById(id: String): PresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPresets(presets: List<PresetEntity>)

    @Update
    suspend fun updatePreset(preset: PresetEntity)

    @Delete
    suspend fun deletePreset(preset: PresetEntity)

    @Query("DELETE FROM preset_items WHERE id = :id")
    suspend fun deletePresetById(id: String)

    @Query("SELECT COUNT(*) FROM preset_items")
    suspend fun getCount(): Int
}

@Database(
    entities = [MediaItemEntity::class, PresetEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun presetDao(): PresetDao
}
