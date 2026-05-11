package com.tinnitustracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,         // epoch millis (start of day in local TZ — caller's responsibility)
    val severity: Int,      // 0..10
    val stressLevel: Int,   // 0..10
    val note: String = ""
)
