package com.tinnitustracker.data.repository

import com.tinnitustracker.data.database.dao.ListeningSessionDao
import com.tinnitustracker.data.database.entities.ListeningSession
import kotlinx.coroutines.flow.Flow

class ListeningSessionRepository(private val dao: ListeningSessionDao) {

    fun observeRange(fromEpoch: Long, toEpoch: Long): Flow<List<ListeningSession>> =
        dao.observeRange(fromEpoch, toEpoch)

    /**
     * Persists a session. Drops sessions shorter than [MIN_DURATION_MS] to filter
     * accidental taps / hairtrigger stop-after-start sequences out of the heatmap.
     * Returns the inserted row id, or null if the session was filtered.
     */
    suspend fun logSession(
        startedAtEpochMs: Long,
        endedAtEpochMs: Long,
        presetLabel: String? = null
    ): Long? {
        val duration = endedAtEpochMs - startedAtEpochMs
        if (duration < MIN_DURATION_MS) return null
        return dao.insert(
            ListeningSession(
                startedAtEpochMs = startedAtEpochMs,
                endedAtEpochMs = endedAtEpochMs,
                durationMs = duration,
                presetLabel = presetLabel
            )
        )
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    companion object {
        const val MIN_DURATION_MS = 5_000L
    }
}
