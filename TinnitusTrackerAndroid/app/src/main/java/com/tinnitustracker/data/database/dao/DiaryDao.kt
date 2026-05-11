package com.tinnitustracker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tinnitustracker.data.database.entities.DiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DiaryEntry): Long

    @Query("SELECT * FROM diary_entries WHERE date BETWEEN :fromEpoch AND :toEpoch ORDER BY date DESC")
    fun observeRange(fromEpoch: Long, toEpoch: Long): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries ORDER BY date DESC LIMIT 1")
    suspend fun latest(): DiaryEntry?

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
