package com.tinnitustracker.data.repository

import com.tinnitustracker.data.database.dao.SoundPresetDao
import com.tinnitustracker.data.database.entities.SoundPreset
import kotlinx.coroutines.flow.Flow

class SoundPresetRepository(private val dao: SoundPresetDao) {

    fun observeAll(): Flow<List<SoundPreset>> = dao.observeAll()

    fun observeById(id: Long): Flow<SoundPreset?> = dao.observeById(id)

    suspend fun getById(id: Long): SoundPreset? = dao.getById(id)

    suspend fun savePreset(preset: SoundPreset): Long {
        return if (preset.id == 0L) {
            dao.insert(preset)
        } else {
            dao.update(preset)
            preset.id
        }
    }

    suspend fun deletePreset(id: Long) = dao.deleteById(id)
}
