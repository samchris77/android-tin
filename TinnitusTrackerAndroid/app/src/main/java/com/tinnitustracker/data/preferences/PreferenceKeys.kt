package com.tinnitustracker.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val MATCHED_FREQUENCY_HZ = floatPreferencesKey("matched_frequency_hz")
    val HAS_TONAL_TINNITUS   = booleanPreferencesKey("has_tonal_tinnitus")
    val ONBOARDING_COMPLETE  = booleanPreferencesKey("onboarding_complete")
    val PROCESSING_MODE      = stringPreferencesKey("processing_mode") // "notch" | "amplify"
    val TFI_CADENCE_WEEKS    = intPreferencesKey("tfi_cadence_weeks")  // 1 | 2 (default 2)
    val LAST_TFI_DATE        = longPreferencesKey("last_tfi_date")     // epoch ms (0 = never)
    val ACTIVE_PRESET_ID     = longPreferencesKey("active_preset_id")
    val TREATMENT_START_DATE = longPreferencesKey("treatment_start_date")
    val DAILY_LISTENING_GOAL_MIN = intPreferencesKey("daily_listening_goal_min")
}
