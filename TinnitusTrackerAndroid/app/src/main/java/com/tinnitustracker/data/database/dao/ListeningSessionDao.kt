package com.tinnitustracker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tinnitustracker.data.database.entities.ListeningSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ListeningSessionDao {

    @Insert
    suspend fun insert(s: ListeningSession): Long

    @Query(
        "SELECT * FROM listening_sessions " +
            "WHERE startedAtEpochMs >= :fromEpoch AND startedAtEpochMs < :toEpoch " +
            "ORDER BY startedAtEpochMs ASC"
    )
    fun observeRange(fromEpoch: Long, toEpoch: Long): Flow<List<ListeningSession>>

    @androidx.room.Transaction
    @Query(
        "SELECT * FROM listening_sessions " +
            "WHERE startedAtEpochMs >= :fromEpoch AND startedAtEpochMs < :toEpoch " +
            "ORDER BY startedAtEpochMs ASC"
    )
    fun observeRangeWithSegments(fromEpoch: Long, toEpoch: Long): Flow<List<com.tinnitustracker.data.database.entities.ListeningSessionWithSegments>>

    @Insert
    suspend fun insertSegments(segments: List<com.tinnitustracker.data.database.entities.ListeningSessionSegment>)

    @Query("DELETE FROM listening_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
