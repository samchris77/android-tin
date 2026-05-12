package com.tinnitustracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.tinnitustracker.data.preferences.PreferenceKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "user_settings")

class UserSettingsRepository(private val context: Context) {

    val matchedFrequencyHz: Flow<Float> = context.dataStore.data
        .map { it[PreferenceKeys.MATCHED_FREQUENCY_HZ] ?: 1000f }

    val hasTonalTinnitus: Flow<Boolean?> = context.dataStore.data
        .map { it[PreferenceKeys.HAS_TONAL_TINNITUS] }

    val processingMode: Flow<String> = context.dataStore.data
        .map { it[PreferenceKeys.PROCESSING_MODE] ?: "notch" }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data
        .map { it[PreferenceKeys.ONBOARDING_COMPLETE] ?: false }

    val tfiCadenceWeeks: Flow<Int> = context.dataStore.data
        .map { it[PreferenceKeys.TFI_CADENCE_WEEKS] ?: 2 }

    val lastTfiDate: Flow<Long> = context.dataStore.data
        .map { it[PreferenceKeys.LAST_TFI_DATE] ?: 0L }

    suspend fun setOnboardingComplete(value: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.ONBOARDING_COMPLETE] = value }
    }

    suspend fun saveMatchedFrequency(hz: Float) {
        context.dataStore.edit { it[PreferenceKeys.MATCHED_FREQUENCY_HZ] = hz }
    }

    suspend fun setHasTonalTinnitus(value: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.HAS_TONAL_TINNITUS] = value }
    }

    suspend fun saveProcessingMode(mode: String) {
        context.dataStore.edit { it[PreferenceKeys.PROCESSING_MODE] = mode }
    }

    suspend fun setTfiCadenceWeeks(weeks: Int) {
        context.dataStore.edit { it[PreferenceKeys.TFI_CADENCE_WEEKS] = weeks }
    }

    suspend fun setLastTfiDate(epochMs: Long) {
        context.dataStore.edit { it[PreferenceKeys.LAST_TFI_DATE] = epochMs }
    }
}
