package com.tinnitustracker.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val MATCHED_FREQUENCY_HZ = floatPreferencesKey("matched_frequency_hz")
    val HAS_TONAL_TINNITUS   = booleanPreferencesKey("has_tonal_tinnitus")
    val ONBOARDING_COMPLETE  = booleanPreferencesKey("onboarding_complete")
    val PROCESSING_MODE      = stringPreferencesKey("processing_mode") // "notch" | "amplify"
}
