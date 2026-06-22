package com.wordmaster.app.logic

import com.wordmaster.app.data.model.ReviewOutcome
import com.wordmaster.app.data.model.WordProgress

/**
 * Ebbinghaus forgetting curve based scheduler.
 * Computes next review time and stage based on review outcome.
 */
object EbbinghausScheduler {
    /**
     * Base intervals in days for stages 1-5.
     * Stage 1: 1 day, Stage 2: 2 days, Stage 3: 4 days, Stage 4: 7 days, Stage 5: 15 days.
     */
    private val BASE_INTERVALS = listOf(1L, 2L, 4L, 7L, 15L)

    /**
     * Maximum interval cap in days.
     */
    private const val MAX_INTERVAL_DAYS = 60L

    /**
     * Milliseconds per day.
     */
    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    /**
     * Compute next review state based on current state and outcome.
     *
     * Algorithm:
     * - New word (stage=0) + KNOWN -> stage=1, interval=1 day
     * - New word (stage=0) + FORGOTTEN -> stay stage=0, interval=0 (review immediately)
     * - Stage>=1 + KNOWN -> stage++, interval from BASE_INTERVALS (or double after stage 5, cap 60 days)
     * - Stage>=1 + FORGOTTEN -> reset to stage=0, interval=0
     * - lastReviewedAt is always set to now
     * - correctCount/wrongCount incremented based on outcome
     */
    fun computeNextReview(
        current: WordProgress,
        outcome: ReviewOutcome,
        now: Long,
    ): WordProgress {
        val (newStage, newIntervalDays) =
            when {
                // New word (stage=0) cases
                current.stage == 0 -> {
                    when (outcome) {
                        ReviewOutcome.KNOWN -> 1 to 1L
                        ReviewOutcome.FORGOTTEN -> 0 to 0L
                    }
                }

                // Stage >= 1 cases
                else -> {
                    when (outcome) {
                        ReviewOutcome.KNOWN -> {
                            val nextStage = current.stage + 1
                            val intervalDays =
                                if (current.stage <= BASE_INTERVALS.size) {
                                    BASE_INTERVALS[current.stage - 1]
                                } else {
                                    // After stage 5, double the previous interval, cap at MAX_INTERVAL_DAYS
                                    val prevInterval = BASE_INTERVALS.last()
                                    (prevInterval * (1L shl (current.stage - BASE_INTERVALS.size)))
                                        .coerceAtMost(MAX_INTERVAL_DAYS)
                                }
                            nextStage to intervalDays
                        }

                        ReviewOutcome.FORGOTTEN -> {
                            // Reset to new word state
                            0 to 0L
                        }
                    }
                }
            }

        val nextReviewAt =
            if (newIntervalDays > 0) {
                now + newIntervalDays * DAY_MILLIS
            } else {
                0L // 0 means review immediately / always due
            }

        return current.copy(
            stage = newStage,
            nextReviewAt = nextReviewAt,
            correctCount = if (outcome == ReviewOutcome.KNOWN) current.correctCount + 1 else current.correctCount,
            wrongCount = if (outcome == ReviewOutcome.FORGOTTEN) current.wrongCount + 1 else current.wrongCount,
            lastReviewedAt = now,
        )
    }
}
