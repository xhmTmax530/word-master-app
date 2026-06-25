package com.wordmaster.app.data.repository

import com.wordmaster.app.data.db.WordProgressEntity
import com.wordmaster.app.data.model.ReviewOutcome
import com.wordmaster.app.data.model.Word
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WordRepository 单元测试 — 覆盖 3 个关键路径:
 *  1. 注入词表后 allWords() 返回该表(可测试性构造器 OK)
 *  2. getOrCreateProgress 对未存在的 wordId 返回 defaultProgress(stage=0)
 *  3. observeStudyCards combine 正确合并 words + DAO 进度
 */
class WordRepositoryTest {
    private val sampleWords =
        listOf(
            Word(id = 1, word = "apple", meaning = "苹果"),
            Word(id = 2, word = "banana", meaning = "香蕉"),
            Word(id = 3, word = "cherry", meaning = "樱桃"),
        )

    @Test
    fun `allWords returns the preloaded word list`() {
        // 用 internal 测试构造器注入词表,跳过 assets 读取
        val dao = FakeWordProgressDao()
        val repo = WordRepository(preloadedWords = sampleWords, dao = dao)

        val words = repo.allWords()
        assertEquals(3, words.size)
        assertEquals("apple", words[0].word)
        assertEquals("banana", words[1].word)
        assertEquals("cherry", words[2].word)
    }

    @Test
    fun `getOrCreateProgress returns default for unknown wordId`() =
        runBlocking {
            val dao = FakeWordProgressDao()
            val repo = WordRepository(sampleWords, dao)

            // 词表里有 id=1,2,3;查询 id=999 应该走 default 分支
            val progress = repo.getOrCreateProgress(wordId = 999)
            assertEquals(0, progress.stage)
            assertEquals(0L, progress.nextReviewAt)
            assertEquals(0, progress.correctCount)
            assertEquals(0, progress.wrongCount)
            assertEquals(0L, progress.lastReviewedAt)
            assertEquals(999, progress.wordId)
        }

    @Test
    fun `observeStudyCards combines words with DAO progress, defaults missing`() =
        runBlocking {
            // DAO 里只有 id=1 的进度;id=2/3 应走 default
            val dao =
                FakeWordProgressDao(
                    initial =
                        mapOf(
                            1 to
                                WordProgressEntity(
                                    wordId = 1,
                                    stage = 3,
                                    nextReviewAt = 1000L,
                                    correctCount = 2,
                                    wrongCount = 0,
                                    lastReviewedAt = 500L,
                                ),
                        ),
                )
            val repo = WordRepository(sampleWords, dao)

            val cards = repo.observeStudyCards().first()
            assertEquals(3, cards.size)

            // id=1:来自 DAO
            assertEquals(1, cards[0].word.id)
            assertEquals(3, cards[0].progress.stage)
            assertEquals(1000L, cards[0].progress.nextReviewAt)
            assertEquals(2, cards[0].progress.correctCount)

            // id=2,3:走 default
            assertEquals(2, cards[1].word.id)
            assertEquals(0, cards[1].progress.stage)
            assertEquals(0L, cards[1].progress.nextReviewAt)

            assertEquals(3, cards[2].word.id)
            assertEquals(0, cards[2].progress.stage)
        }

    @Test
    fun `updateProgress writes via dao upsert`() =
        runBlocking {
            val dao = FakeWordProgressDao()
            val repo = WordRepository(sampleWords, dao)

            repo.updateProgress(wordId = 1, outcome = ReviewOutcome.KNOWN, now = 1000L)

            val saved = dao.getByWordId(1)
            assertNotNull(saved)
            // 新词 + KNOWN → stage=1, nextReviewAt = 1000 + 1 day
            assertEquals(1, saved!!.stage)
            assertTrue("nextReviewAt 应该 = now + 1 day", saved.nextReviewAt > 1000L)
            assertEquals(1, saved.correctCount)
            assertEquals(0, saved.wrongCount)

            // 查询不存在的 word 应返回 null(而不是创建新记录 — updateProgress 才会写)
            assertNull(dao.getByWordId(999))
        }
}
