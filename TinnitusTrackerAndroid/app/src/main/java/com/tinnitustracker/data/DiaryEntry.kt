package com.tinnitustracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Date,
    val createdAt: Date = Date(),
    val loudnessLevel: Int, // 0-10
    val comfortLevel: Int, // 0-10
    val stressLevel: Int, // 0-10
    val notes: String? = null,
    val currentFrequency: Float = 0f,
    val currentVolume: Float = 0f,
    val sessionDuration: Double = 0.0,
    val entryNumber: Int = 0
)
