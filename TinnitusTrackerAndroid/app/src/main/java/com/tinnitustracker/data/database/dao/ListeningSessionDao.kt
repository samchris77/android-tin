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

    /** Sessions whose startedAt falls in [fromEpoch, toEpoch). Reactive. */
    @Query(
        "SELECT * FROM listening_sessions " +
            "WHERE startedAtEpochMs >= :fromEpoch AND startedAtEpochMs < :toEpoch " +
            "ORDER BY startedAtEpochMs ASC"
    )
    fun observeRange(fromEpoch: Long, toEpoch: Long): Flow<List<ListeningSession>>

    @Query("DELETE FROM listening_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
