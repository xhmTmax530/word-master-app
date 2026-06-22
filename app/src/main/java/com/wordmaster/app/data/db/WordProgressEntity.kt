package com.wordmaster.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wordmaster.app.data.model.WordProgress

/**
 * Room Entity for word_progress table.
 * Mirrors WordProgress domain model for persistence.
 */
@Entity(tableName = "word_progress")
data class WordProgressEntity(
    @PrimaryKey
    val wordId: Int,
    val stage: Int,
    val nextReviewAt: Long,
    val correctCount: Int,
    val wrongCount: Int,
    val lastReviewedAt: Long,
) {
    /**
     * Convert entity to domain model.
     */
    fun toDomain(): WordProgress =
        WordProgress(
            wordId = wordId,
            stage = stage,
            nextReviewAt = nextReviewAt,
            correctCount = correctCount,
            wrongCount = wrongCount,
            lastReviewedAt = lastReviewedAt,
        )

    companion object {
        /**
         * Create entity from domain model.
         */
        fun fromDomain(progress: WordProgress): WordProgressEntity =
            WordProgressEntity(
                wordId = progress.wordId,
                stage = progress.stage,
                nextReviewAt = progress.nextReviewAt,
                correctCount = progress.correctCount,
                wrongCount = progress.wrongCount,
                lastReviewedAt = progress.lastReviewedAt,
            )
    }
}
