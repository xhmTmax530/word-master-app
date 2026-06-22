package com.wordmaster.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for WordMaster app.
 * Currently holds only word_progress table.
 */
@Database(
    entities = [WordProgressEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordProgressDao(): WordProgressDao

    companion object {
        @Volatile
        private var instance: WordDatabase? = null

        /**
         * Get singleton instance of the database.
         */
        fun getInstance(context: Context): WordDatabase =
            instance ?: synchronized(this) {
                val newInstance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            WordDatabase::class.java,
                            "word_master_db",
                        ).build()
                instance = newInstance
                newInstance
            }
    }
}
