package com.tinnitustracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listening_sessions")
data class ListeningSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val durationMs: Long,
    val presetLabel: String? = null,
    // Mock labels — random until real presets ship (plan #11). Three independent
    // dimensions so analytics can ask "how much rain while working?" later.
    val colorNoise: String? = null,   // white | pink | brown
    val ambient: String? = null,      // rain | wind | waves | traffic
    val activity: String? = null      // commute | resting | sleeping | working | studying
)
