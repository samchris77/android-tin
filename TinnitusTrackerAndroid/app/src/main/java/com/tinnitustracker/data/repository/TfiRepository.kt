package com.tinnitustracker.data.repository

import com.tinnitustracker.data.database.dao.TfiAssessmentDao
import com.tinnitustracker.data.database.entities.TFIAssessment
import com.tinnitustracker.ui.assessment.TfiScoring
import kotlinx.coroutines.flow.Flow

class TfiRepository(
    private val dao: TfiAssessmentDao,
    private val userSettings: UserSettingsRepository
) {

    fun observeAll(): Flow<List<TFIAssessment>> = dao.observeAll()

    suspend fun latest(): TFIAssessment? = dao.latest()

    /**
     * Scores [items] using the verified TFI spec, persists the assessment,
     * and bumps `last_tfi_date`. Returns the persisted row.
     */
    suspend fun submit(items: Map<Int, Int>, takenAtEpochMs: Long): TFIAssessment {
        val scored = TfiScoring.score(items)
        val row = TFIAssessment(
            takenAtEpochMs = takenAtEpochMs,
            items = items,
            totalScore = scored.total,
            subscaleScores = scored.subscales
        )
        val id = dao.upsert(row)
        userSettings.setLastTfiDate(takenAtEpochMs)
        return row.copy(id = id)
    }
}
