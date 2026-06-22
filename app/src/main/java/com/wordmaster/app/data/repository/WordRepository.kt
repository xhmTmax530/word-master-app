package com.wordmaster.app.data.repository

import android.content.Context
import com.wordmaster.app.data.db.WordProgressDao
import com.wordmaster.app.data.db.WordProgressEntity
import com.wordmaster.app.data.model.ReviewOutcome
import com.wordmaster.app.data.model.StudyCard
import com.wordmaster.app.data.model.Word
import com.wordmaster.app.data.model.WordProgress
import com.wordmaster.app.logic.EbbinghausScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json

/**
 * Repository for word data and study progress.
 * Loads words from assets and manages progress via DAO.
 */
class WordRepository(
    private val context: Context,
    private val dao: WordProgressDao,
) {
    /**
     * Lazily load words from assets/words.json.
     */
    private val words: List<Word> by lazy {
        context.assets.open("words.json").bufferedReader().use { reader ->
            Json.decodeFromString<List<Word>>(reader.readText())
        }
    }

    /**
     * Default progress for a new word (stage 0, no review scheduled).
     */
    private fun defaultProgress(wordId: Int): WordProgress =
        WordProgress(
            wordId = wordId,
            stage = 0,
            nextReviewAt = 0L,
            correctCount = 0,
            wrongCount = 0,
            lastReviewedAt = 0L,
        )

    /**
     * Observe all study cards by combining words with their progress.
     * Words without progress get default progress.
     */
    fun observeStudyCards(): Flow<List<StudyCard>> =
        dao.observeAll().combine(flowOf(words)) { entities, wordList ->
            val progressMap = entities.associateBy { it.wordId }
            wordList.map { word ->
                StudyCard(
                    word = word,
                    progress = progressMap[word.id]?.toDomain() ?: defaultProgress(word.id),
                )
            }
        }

    /**
     * Update progress for a word after a review.
     */
    suspend fun updateProgress(
        wordId: Int,
        outcome: ReviewOutcome,
        now: Long = System.currentTimeMillis(),
    ) {
        val current = getOrCreateProgress(wordId)
        val updated = EbbinghausScheduler.computeNextReview(current, outcome, now)
        dao.upsert(WordProgressEntity.fromDomain(updated))
    }

    /**
     * Get existing progress or create default for a word.
     */
    suspend fun getOrCreateProgress(wordId: Int): WordProgress = dao.getByWordId(wordId)?.toDomain() ?: defaultProgress(wordId)

    /**
     * Get all words (for ViewModel to compute daily target).
     */
    fun allWords(): List<Word> = words
}
