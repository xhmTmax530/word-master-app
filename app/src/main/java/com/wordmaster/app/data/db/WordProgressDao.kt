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
     * B-3 fix: 仅插入新词进度,已存在的记录不覆盖。
     * 用 IGNORE 策略,避免 App 重启时把用户的复习进度(stage/correctCount/wrongCount)清零。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun initializeAllWords(entities: List<WordProgressEntity>)

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
