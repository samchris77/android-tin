package com.tinnitustracker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tinnitustracker.data.database.entities.SoundPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundPresetDao {

    @Query("SELECT * FROM sound_presets ORDER BY name ASC")
    fun observeAll(): Flow<List<SoundPreset>>

    @Query("SELECT * FROM sound_presets WHERE id = :id")
    suspend fun getById(id: Long): SoundPreset?

    @Query("SELECT * FROM sound_presets WHERE id = :id")
    fun observeById(id: Long): Flow<SoundPreset?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preset: SoundPreset): Long

    @Update
    suspend fun update(preset: SoundPreset)

    @Query("DELETE FROM sound_presets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
