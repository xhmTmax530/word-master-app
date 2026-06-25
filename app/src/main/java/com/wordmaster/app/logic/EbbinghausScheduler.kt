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
     * B-4 fix: Long 左移最大安全位数。1L shl 63 会变成 Long.MIN_VALUE,
     * 限制到 62 保证 shift 操作本身不溢出。乘法溢出由 Math.multiplyExact 兜底。
     */
    private const val MAX_SAFE_SHIFT_COUNT = 62

    /**
     * Milliseconds per day.
     */
    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    /**
     * Compute next review state based on current state and outcome.
     *
     * Algorithm:
     * - New word (stage=0) + KNOWN -> stage=1, interval=1 day
     * - New word (stage=0) + FORGOTTEN -> stay stage=0, interval=1 day (避免立即重抽死循环)
     * - Stage>=1 + KNOWN -> stage++, interval from BASE_INTERVALS (or double after stage 5, cap 60 days)
     * - Stage>=1 + FORGOTTEN -> reset to stage=0, interval=1 day
     * - lastReviewedAt is always set to now
     * - correctCount/wrongCount incremented based on outcome
     *
     * 修复:
     * - I-1: 新词忘了时 interval=1 而非 0,避免死循环(interval=0 -> isDue=false -> 立即再抽一次)
     * - B-4: 阶段≥62 时直接返回封顶值,避免 `1L shl 62` 溢出变负导致 nextReviewAt 倒退
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
                        // I-1 fix: 新词忘了也推迟 1 天,避免立即重抽死循环
                        ReviewOutcome.FORGOTTEN -> 0 to 1L
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
                                    // B-4 fix: After stage 5, double the previous interval, cap at MAX_INTERVAL_DAYS。
                                    // 两层防护:
                                    //   1. shiftCount ≤ 62 保证 `1L shl shiftCount` 不溢出
                                    //   2. Math.multiplyExact 检测乘法溢出,溢出时直接给 Long.MAX_VALUE 让 coerceAtMost 兜底
                                    val prevInterval = BASE_INTERVALS.last()
                                    val shiftCount =
                                        (current.stage - BASE_INTERVALS.size)
                                            .coerceAtMost(MAX_SAFE_SHIFT_COUNT)
                                    val candidate =
                                        try {
                                            Math.multiplyExact(prevInterval, 1L shl shiftCount)
                                        } catch (e: ArithmeticException) {
                                            // 乘法溢出 → 直接给最大值,外层 coerceAtMost 兜底
                                            Long.MAX_VALUE
                                        }
                                    candidate.coerceAtMost(MAX_INTERVAL_DAYS)
                                }
                            nextStage to intervalDays
                        }

                        ReviewOutcome.FORGOTTEN -> {
                            // Reset to new word state, but defer 1 day to avoid immediate re-pick
                            0 to 1L
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
