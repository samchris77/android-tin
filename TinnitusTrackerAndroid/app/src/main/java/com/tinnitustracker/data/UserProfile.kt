package com.tinnitustracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val tinnitusFrequency: Float = 1000f,
    val matchedVolume: Float = 0.3f,
    val createdDate: Date = Date()
)
