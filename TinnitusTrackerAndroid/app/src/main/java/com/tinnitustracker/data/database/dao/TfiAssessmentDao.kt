package com.tinnitustracker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tinnitustracker.data.database.entities.TFIAssessment
import kotlinx.coroutines.flow.Flow

@Dao
interface TfiAssessmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(assessment: TFIAssessment): Long

    @Query("SELECT * FROM tfi_assessments ORDER BY takenAtEpochMs DESC LIMIT 1")
    suspend fun latest(): TFIAssessment?

    @Query("SELECT * FROM tfi_assessments ORDER BY takenAtEpochMs DESC")
    fun observeAll(): Flow<List<TFIAssessment>>

    @Query("DELETE FROM tfi_assessments WHERE id = :id")
    suspend fun deleteById(id: Long)
}
