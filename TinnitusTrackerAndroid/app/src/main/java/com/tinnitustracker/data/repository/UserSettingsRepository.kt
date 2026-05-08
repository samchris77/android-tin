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

    suspend fun saveMatchedFrequency(hz: Float) {
        context.dataStore.edit { it[PreferenceKeys.MATCHED_FREQUENCY_HZ] = hz }
    }

    suspend fun setHasTonalTinnitus(value: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.HAS_TONAL_TINNITUS] = value }
    }
}
