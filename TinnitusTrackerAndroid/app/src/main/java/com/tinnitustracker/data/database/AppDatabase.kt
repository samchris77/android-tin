package com.tinnitustracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tinnitustracker.data.database.dao.DiaryDao
import com.tinnitustracker.data.database.entities.DiaryEntry

@Database(
    entities = [DiaryEntry::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao

    companion object {
        private const val DB_NAME = "tinnitus_tracker.db"

        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build().also { instance = it }
            }
    }
}
