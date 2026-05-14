package com.tinnitustracker.data.repository

import com.tinnitustracker.data.database.dao.ListeningSessionDao
import com.tinnitustracker.data.database.entities.ListeningSession
import com.tinnitustracker.data.database.entities.ListeningSessionSegment
import com.tinnitustracker.data.database.entities.ListeningSessionWithSegments
import kotlinx.coroutines.flow.Flow

data class SessionSegmentDraft(
    val startedAtEpochMs: Long,
    val durationMs: Long,
    val presetName: String?
)

class ListeningSessionRepository(private val dao: ListeningSessionDao) {

    fun observeRange(fromEpoch: Long, toEpoch: Long): Flow<List<ListeningSession>> =
        dao.observeRange(fromEpoch, toEpoch)

    fun observeRangeWithSegments(fromEpoch: Long, toEpoch: Long): Flow<List<ListeningSessionWithSegments>> =
        dao.observeRangeWithSegments(fromEpoch, toEpoch)

    /**
     * Persists a session and its segments. Drops sessions shorter than [MIN_DURATION_MS] to filter
     * accidental taps / hairtrigger stop-after-start sequences out of the heatmap.
     * Returns the inserted row id, or null if the session was filtered.
     */
    suspend fun logSession(
        startedAtEpochMs: Long,
        endedAtEpochMs: Long,
        segments: List<SessionSegmentDraft>,
        presetLabel: String? = null // For backwards compat / dominant preset
    ): Long? {
        val duration = endedAtEpochMs - startedAtEpochMs
        if (duration < MIN_DURATION_MS) return null
        
        val sessionId = dao.insert(
            ListeningSession(
                startedAtEpochMs = startedAtEpochMs,
                endedAtEpochMs = endedAtEpochMs,
                durationMs = duration,
                presetLabel = presetLabel
            )
        )

        val segmentEntities = segments.map { draft ->
            ListeningSessionSegment(
                sessionId = sessionId,
                startedAtEpochMs = draft.startedAtEpochMs,
                durationMs = draft.durationMs,
                presetName = draft.presetName
            )
        }
        
        if (segmentEntities.isNotEmpty()) {
            dao.insertSegments(segmentEntities)
        }

        return sessionId
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    companion object {
        // 3 min: filters out tap-noise and low-signal short plays without
        // silently dropping legitimate short TRT exposures.
        const val MIN_DURATION_MS = 180_000L
    }
}
