package com.tinnitustracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE date >= :startDate ORDER BY date DESC")
    fun getEntriesAfter(startDate: Date): Flow<List<DiaryEntry>>

    @Insert
    suspend fun insert(entry: DiaryEntry)

    @Update
    suspend fun update(entry: DiaryEntry)

    @Delete
    suspend fun delete(entry: DiaryEntry)
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfile(): UserProfile?
    
    @Insert
    suspend fun insertProfile(profile: UserProfile)
    
    @Update
    suspend fun updateProfile(profile: UserProfile)
}
