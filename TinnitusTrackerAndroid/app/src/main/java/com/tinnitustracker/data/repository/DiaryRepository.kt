package com.tinnitustracker.data.repository

import com.tinnitustracker.data.database.dao.DiaryDao
import com.tinnitustracker.data.database.entities.DiaryEntry
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val dao: DiaryDao) {

    fun observeRange(fromEpoch: Long, toEpoch: Long): Flow<List<DiaryEntry>> =
        dao.observeRange(fromEpoch, toEpoch)

    suspend fun latest(): DiaryEntry? = dao.latest()

    suspend fun upsert(entry: DiaryEntry): Long = dao.upsert(entry)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
