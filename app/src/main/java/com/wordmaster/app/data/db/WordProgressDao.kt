package com.wordmaster.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for word_progress table.
 */
@Dao
interface WordProgressDao {
    /**
     * Insert or update a single progress record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WordProgressEntity)

    /**
     * Batch insert or update progress records.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<WordProgressEntity>)

    /**
     * Observe all progress records as a Flow.
     */
    @Query("SELECT * FROM word_progress")
    fun observeAll(): Flow<List<WordProgressEntity>>

    /**
     * Get progress for a specific word.
     */
    @Query("SELECT * FROM word_progress WHERE wordId = :wordId")
    suspend fun getByWordId(wordId: Int): WordProgressEntity?

    /**
     * Get all words that are due for review.
     * A word is due when stage > 0 AND nextReviewAt <= now.
     */
    @Query("SELECT * FROM word_progress WHERE stage > 0 AND nextReviewAt <= :now")
    suspend fun getDueWords(now: Long): List<WordProgressEntity>
}
