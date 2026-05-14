package com.tinnitustracker.data.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ListeningSessionWithSegments(
    @Embedded val session: ListeningSession,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val segments: List<ListeningSessionSegment>
)
