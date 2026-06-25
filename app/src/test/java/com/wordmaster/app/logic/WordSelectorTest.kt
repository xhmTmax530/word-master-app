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
 *  1. 到期(isDue) → 随机抽一个
 *  2. 否则新词(stage=0) → 随机抽一个
 *  3. 否则按 nextReviewAt 升序,取最早到期的
 *  4. 空列表 → 返回 null
 *
 * 注:StudyCard.isDue 内部用 System.currentTimeMillis(),所以测试通过构造:
 *   - nextReviewAt = 0L + stage > 0 → 必定 due(因为真实当前时间 > 0)
 *   - nextReviewAt = Long.MAX_VALUE → 永不 due
 */
class WordSelectorTest {
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
        assertNull(WordSelector.pickNext(emptyList()))
    }

    @Test
    fun `single card returns it`() {
        val only = card(id = 1, stage = 0)
        assertEquals(only, WordSelector.pickNext(listOf(only)))
    }

    @Test
    fun `due cards have priority over new words and future cards`() {
        val due = card(id = 1, stage = 2, nextReviewAt = 0L) // stage>0 + nextReviewAt=0 → isDue=true
        val newWord = card(id = 2, stage = 0)
        val future = card(id = 3, stage = 1, nextReviewAt = Long.MAX_VALUE)
        // 跑 50 次,每次都应抽到 due(due 只有 1 张,所以确定)
        repeat(50) {
            val picked = WordSelector.pickNext(listOf(due, newWord, future))
            assertEquals("due 卡片优先级最高", due, picked)
        }
    }

    @Test
    fun `when no due, new words are picked over future cards`() {
        val new1 = card(id = 1, stage = 0)
        val new2 = card(id = 2, stage = 0)
        val future = card(id = 3, stage = 1, nextReviewAt = Long.MAX_VALUE)
        val picked = WordSelector.pickNext(listOf(new1, new2, future))
        assertNotNull(picked)
        assertTrue("没 due 时应抽新词", picked!!.progress.stage == 0)
    }

    @Test
    fun `when no due and no new words, picks earliest nextReviewAt`() {
        val a = card(id = 1, stage = 1, nextReviewAt = NOW + 10 * DAY_MILLIS)
        val b = card(id = 2, stage = 1, nextReviewAt = NOW + 2 * DAY_MILLIS) // 最早
        val c = card(id = 3, stage = 1, nextReviewAt = NOW + 5 * DAY_MILLIS)
        val picked = WordSelector.pickNext(listOf(a, b, c))
        assertEquals("应选 nextReviewAt 最早(b=2 天)", b, picked)
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
