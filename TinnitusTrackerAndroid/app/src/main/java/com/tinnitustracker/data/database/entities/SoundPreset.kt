package com.tinnitustracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sound_presets")
data class SoundPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val processingMode: String, // "notch" | "amplify"
    val colorNoise: String, // "pink" | "white" | "brown" | "off"
    val colorNoiseVolume: Float,
    val ambientMix: Map<String, Float> // e.g., {"rain": 0.6, "waves": 0.0}
)
