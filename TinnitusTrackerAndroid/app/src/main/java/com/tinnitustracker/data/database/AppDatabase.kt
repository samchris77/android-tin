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

import com.tinnitustracker.data.database.dao.SoundPresetDao
import com.tinnitustracker.data.database.entities.SoundPreset

@Database(
    entities = [DiaryEntry::class, TFIAssessment::class, ListeningSession::class, SoundPreset::class, com.tinnitustracker.data.database.entities.ListeningSessionSegment::class],
    version = 6,
    exportSchema = true
)
@TypeConverters(MapTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao
    abstract fun tfiAssessmentDao(): TfiAssessmentDao
    abstract fun listeningSessionDao(): ListeningSessionDao
    abstract fun soundPresetDao(): SoundPresetDao

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

        // Additive: introduces sound_presets table and a default preset.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sound_presets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `processingMode` TEXT NOT NULL,
                        `colorNoise` TEXT NOT NULL,
                        `colorNoiseVolume` REAL NOT NULL,
                        `ambientMix` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `sound_presets` (`name`, `processingMode`, `colorNoise`, `colorNoiseVolume`, `ambientMix`)
                    VALUES ('저녁 휴식', 'notch', 'pink', 0.5, '{"rain":0.6,"waves":0.0}')
                    """.trimIndent()
                )
            }
        }

        // Additive: introduces listening_session_segments table.
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `listening_session_segments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `startedAtEpochMs` INTEGER NOT NULL,
                        `durationMs` INTEGER NOT NULL,
                        `presetName` TEXT,
                        FOREIGN KEY(`sessionId`) REFERENCES `listening_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_listening_session_segments_sessionId` ON `listening_session_segments` (`sessionId`)")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                    .also { instance = it }
            }
    }
}
