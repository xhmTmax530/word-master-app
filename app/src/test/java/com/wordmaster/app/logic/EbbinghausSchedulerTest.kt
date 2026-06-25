package com.wordmaster.app.logic

import com.wordmaster.app.data.model.ReviewOutcome
import com.wordmaster.app.data.model.WordProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * EbbinghausScheduler 单元测试 — 覆盖 stage 0/1/3/5/6/10/70+ 与 Known/Forgotten 的全部分支。
 *
 * 设计要点:
 * - now 用固定基准时间,避免依赖系统时钟
 * - DAY_MILLIS = 24h,直接验证 nextReviewAt - now == intervalDays * DAY_MILLIS
 * - B-4 修复回归:stage≥62 不溢出,封顶 60 天
 * - I-1 修复回归:stage=0+FORGOTTEN 推迟 1 天,不再死循环
 */
class EbbinghausSchedulerTest {
    private companion object {
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
        const val NOW = 1_700_000_000_000L // 2023-11-14 固定基准时间
    }

    private fun progress(stage: Int = 0) =
        WordProgress(
            wordId = 1,
            stage = stage,
            nextReviewAt = 0L,
            correctCount = 0,
            wrongCount = 0,
            lastReviewedAt = 0L,
        )

    /** 断言 nextReviewAt 与 stage 符合期望,无视 correctCount/wrongCount 自增。 */
    private fun assertNext(
        result: WordProgress,
        expectedStage: Int,
        expectedDaysFromNow: Long,
    ) {
        assertEquals("stage 应为 $expectedStage", expectedStage, result.stage)
        val expectedAt = NOW + expectedDaysFromNow * DAY_MILLIS
        assertEquals(
            "nextReviewAt 应为 NOW + $expectedDaysFromNow 天",
            expectedAt,
            result.nextReviewAt,
        )
        assertEquals("lastReviewedAt 应等于 now", NOW, result.lastReviewedAt)
    }

    @Test
    fun `stage 0 + KNOWN advances to stage 1, 1 day`() {
        val result = EbbinghausScheduler.computeNextReview(progress(0), ReviewOutcome.KNOWN, NOW)
        assertNext(result, expectedStage = 1, expectedDaysFromNow = 1)
        assertEquals(1, result.correctCount)
        assertEquals(0, result.wrongCount)
    }

    @Test
    fun `stage 0 + FORGOTTEN stays stage 0 but defers 1 day (I-1 fix)`() {
        // 修复前:interval=0 → nextReviewAt=0 → isDue=false 但立即可重抽,死循环
        // 修复后:interval=1 → 推迟 1 天,避免死循环
        val result = EbbinghausScheduler.computeNextReview(progress(0), ReviewOutcome.FORGOTTEN, NOW)
        assertEquals(0, result.stage)
        assertEquals("I-1 fix: interval 必须是 1 天而非 0", NOW + DAY_MILLIS, result.nextReviewAt)
        assertEquals(0, result.correctCount)
        assertEquals(1, result.wrongCount)
    }

    @Test
    fun `stage 1 + KNOWN advances to stage 2, 1 day`() {
        // BASE_INTERVALS[0] = 1 — 算法语义:OLD stage=1 → 等待 1 天后晋级 stage 2
        val result = EbbinghausScheduler.computeNextReview(progress(1), ReviewOutcome.KNOWN, NOW)
        assertNext(result, expectedStage = 2, expectedDaysFromNow = 1)
    }

    @Test
    fun `stage 3 + KNOWN advances to stage 4, 4 days`() {
        // BASE_INTERVALS[2] = 4(stage 3 → 4)
        val result = EbbinghausScheduler.computeNextReview(progress(3), ReviewOutcome.KNOWN, NOW)
        assertNext(result, expectedStage = 4, expectedDaysFromNow = 4)
    }

    @Test
    fun `stage 5 + KNOWN advances to stage 6, 15 days`() {
        // BASE_INTERVALS[4] = 15(stage 5 → 6)
        val result = EbbinghausScheduler.computeNextReview(progress(5), ReviewOutcome.KNOWN, NOW)
        assertNext(result, expectedStage = 6, expectedDaysFromNow = 15)
    }

    @Test
    fun `stage 6 + KNOWN doubles previous interval to 30 days`() {
        // stage 6 > BASE_INTERVALS.size(5),进入翻倍分支
        // shiftCount = 6 - 5 = 1,1L shl 1 = 2,15*2 = 30
        val result = EbbinghausScheduler.computeNextReview(progress(6), ReviewOutcome.KNOWN, NOW)
        assertNext(result, expectedStage = 7, expectedDaysFromNow = 30)
    }

    @Test
    fun `stage 10 + KNOWN caps at 60 days (B-4 fix)`() {
        // stage 10 > 5,shiftCount = 5,1L shl 5 = 32,15*32 = 480 → coerceAtMost(60) = 60
        val result = EbbinghausScheduler.computeNextReview(progress(10), ReviewOutcome.KNOWN, NOW)
        assertNext(result, expectedStage = 11, expectedDaysFromNow = 60)
    }

    @Test
    fun `stage 70 + KNOWN does NOT overflow Long, caps at 60 days`() {
        // B-4 修复前:stage 62+ → 1L shl N 溢出 + 乘法溢出为负数 → nextReviewAt 倒退到过去
        // B-4 修复后:Math.multiplyExact 捕获溢出 + coerceAtMost 兜底,确保 interval 永远 ≤ 60 天
        val result = EbbinghausScheduler.computeNextReview(progress(70), ReviewOutcome.KNOWN, NOW)
        assertNext(result, expectedStage = 71, expectedDaysFromNow = 60)
        assertTrue("nextReviewAt 必须为正数,不允许 Long 溢出", result.nextReviewAt > 0)
    }
}
