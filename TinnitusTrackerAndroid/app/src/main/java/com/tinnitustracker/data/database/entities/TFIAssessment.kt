package com.tinnitustracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import org.json.JSONObject

@Entity(tableName = "tfi_assessments")
data class TFIAssessment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val takenAtEpochMs: Long,
    val items: Map<Int, Int>,            // raw item responses, key = item # (1..25), value = 0..10 (or 0..100 for #1, #3)
    val totalScore: Int,                 // 0..100 (rounded)
    val subscaleScores: Map<String, Int> // "I", "SC", "C", "SL", "A", "R", "Q", "E" → 0..100
)

class MapTypeConverters {

    @TypeConverter
    fun fromIntIntMap(value: Map<Int, Int>): String {
        val obj = JSONObject()
        value.forEach { (k, v) -> obj.put(k.toString(), v) }
        return obj.toString()
    }

    @TypeConverter
    fun toIntIntMap(value: String): Map<Int, Int> {
        if (value.isBlank()) return emptyMap()
        val obj = JSONObject(value)
        val out = mutableMapOf<Int, Int>()
        obj.keys().forEach { k -> out[k.toInt()] = obj.getInt(k) }
        return out
    }

    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>): String {
        val obj = JSONObject()
        value.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    @TypeConverter
    fun toStringIntMap(value: String): Map<String, Int> {
        if (value.isBlank()) return emptyMap()
        val obj = JSONObject(value)
        val out = mutableMapOf<String, Int>()
        obj.keys().forEach { k -> out[k] = obj.getInt(k) }
        return out
    }

    @TypeConverter
    fun fromStringFloatMap(value: Map<String, Float>): String {
        val obj = JSONObject()
        value.forEach { (k, v) -> obj.put(k, v.toDouble()) }
        return obj.toString()
    }

    @TypeConverter
    fun toStringFloatMap(value: String): Map<String, Float> {
        if (value.isBlank()) return emptyMap()
        val obj = JSONObject(value)
        val out = mutableMapOf<String, Float>()
        obj.keys().forEach { k -> out[k] = obj.getDouble(k).toFloat() }
        return out
    }
}
