package com.wordmaster.app.logic

import com.wordmaster.app.data.model.StudyCard
import com.wordmaster.app.data.model.Word
import com.wordmaster.app.data.model.WordProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WordSelector.pickNext 单元测试 — 验证抽词优先级策略。
 *
 * 抽词策略(WordSelector.pickNext):
 *  1. 到期(isDue(now)) → 随机抽一个
 *  2. 否则新词(stage=0) → 随机抽一个
 *  3. 否则按 nextReviewAt 升序,取最早到期的
 *  4. 空列表 → 返回 null
 *
 * 重要修复:之前测试构造 nextReviewAt 用相对 NOW 的偏移 + 依赖 wall clock,
 * 在 GitHub Actions runner 上可能因时钟漂移挂掉。
 * 现改为:传一个固定的 now 给 pickNext,所有判断走 stage + nextReviewAt 偏移,
 * 不再依赖 System.currentTimeMillis()。
 */
class WordSelectorTest {
    private companion object {
        const val NOW = 1_700_000_000_000L // 2023-11-14 固定基准时间
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }

    private fun card(
        id: Int,
        stage: Int = 0,
        nextReviewAt: Long = 0L,
    ): StudyCard =
        StudyCard(
            word = Word(id = id, word = "w$id", meaning = "m$id"),
            progress =
                WordProgress(
                    wordId = id,
                    stage = stage,
                    nextReviewAt = nextReviewAt,
                ),
        )

    @Test
    fun `empty list returns null`() {
        assertNull(WordSelector.pickNext(emptyList(), now = NOW))
    }

    @Test
    fun `single card returns it`() {
        val only = card(id = 1, stage = 0)
        assertEquals(only, WordSelector.pickNext(listOf(only), now = NOW))
    }

    @Test
    fun `due cards have priority over new words and future cards`() {
        // stage=2 + nextReviewAt < NOW → isDue=true(已学 + 到期)
        // stage=2 + nextReviewAt > NOW → isDue=false(已学但未到期)
        // stage=0 → isDue=false(未学)
        val due = card(id = 1, stage = 2, nextReviewAt = NOW - DAY_MILLIS)
        val newWord = card(id = 2, stage = 0)
        val future = card(id = 3, stage = 2, nextReviewAt = NOW + 5 * DAY_MILLIS)
        repeat(50) {
            val picked = WordSelector.pickNext(listOf(due, newWord, future), now = NOW)
            assertEquals("due 卡片优先级最高", due, picked)
        }
    }

    @Test
    fun `when no due, new words are picked over future cards`() {
        // 全部 stage>=1 且 nextReviewAt > NOW → 没人 due
        // 但 stage=0 也算"新词",优先级高于 future(stage>=1)
        val new1 = card(id = 1, stage = 0, nextReviewAt = 0L)
        val new2 = card(id = 2, stage = 0, nextReviewAt = 0L)
        val future = card(id = 3, stage = 1, nextReviewAt = NOW + 5 * DAY_MILLIS)
        val picked = WordSelector.pickNext(listOf(new1, new2, future), now = NOW)
        assertNotNull(picked)
        assertTrue("没 due 时应抽新词", picked!!.progress.stage == 0)
    }

    @Test
    fun `when no due and no new words, picks earliest nextReviewAt`() {
        // 全部 stage>=1 且 nextReviewAt > NOW(全部未来)
        val a = card(id = 1, stage = 1, nextReviewAt = NOW + 10 * DAY_MILLIS)
        val b = card(id = 2, stage = 1, nextReviewAt = NOW + 2 * DAY_MILLIS) // 最早
        val c = card(id = 3, stage = 1, nextReviewAt = NOW + 5 * DAY_MILLIS)
        val picked = WordSelector.pickNext(listOf(a, b, c), now = NOW)
        assertEquals("应选 nextReviewAt 最早(b=2 天)", b, picked)
    }
}
