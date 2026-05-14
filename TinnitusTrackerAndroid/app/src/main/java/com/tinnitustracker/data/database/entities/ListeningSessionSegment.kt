package com.tinnitustracker.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "listening_session_segments",
    foreignKeys = [
        ForeignKey(
            entity = ListeningSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ListeningSessionSegment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val startedAtEpochMs: Long,
    val durationMs: Long,
    val presetName: String?
)
