package com.tinnitustracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tinnitustracker.data.database.dao.DiaryDao
import com.tinnitustracker.data.database.dao.ListeningSessionDao
import com.tinnitustracker.data.database.dao.TfiAssessmentDao
import com.tinnitustracker.data.database.entities.DiaryEntry
import com.tinnitustracker.data.database.entities.ListeningSession
import com.tinnitustracker.data.database.entities.MapTypeConverters
import com.tinnitustracker.data.database.entities.TFIAssessment

@Database(
    entities = [DiaryEntry::class, TFIAssessment::class, ListeningSession::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(MapTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao
    abstract fun tfiAssessmentDao(): TfiAssessmentDao
    abstract fun listeningSessionDao(): ListeningSessionDao

    companion object {
        private const val DB_NAME = "tinnitus_tracker.db"

        // Additive: introduces tfi_assessments alongside the existing diary_entries.
        // diary_entries is untouched so the v1 sanity check keeps passing.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tfi_assessments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `takenAtEpochMs` INTEGER NOT NULL,
                        `items` TEXT NOT NULL,
                        `totalScore` INTEGER NOT NULL,
                        `subscaleScores` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // Additive: introduces listening_sessions alongside existing tables.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `listening_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `startedAtEpochMs` INTEGER NOT NULL,
                        `endedAtEpochMs` INTEGER NOT NULL,
                        `durationMs` INTEGER NOT NULL,
                        `presetLabel` TEXT
                    )
                    """.trimIndent()
                )
            }
        }

        // Additive: three nullable mock-label columns on listening_sessions
        // (colorNoise, ambient, activity). Mock until presets ship — plan #11.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `listening_sessions` ADD COLUMN `colorNoise` TEXT")
                db.execSQL("ALTER TABLE `listening_sessions` ADD COLUMN `ambient` TEXT")
                db.execSQL("ALTER TABLE `listening_sessions` ADD COLUMN `activity` TEXT")
            }
        }

        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }
    }
}
