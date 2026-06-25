package com.wordmaster.app.data.repository

import android.content.Context
import android.util.Log
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
 *
 * 修复:
 * - I-3: JSON 解析失败时返回空列表 + Log,而不是抛出未捕获异常导致 App 崩溃
 * - 可测试性:提供 internal constructor 接受预加载的 words 列表,跳过 assets 读取
 */
class WordRepository internal constructor(
    private val wordsLoader: () -> List<Word>,
    private val dao: WordProgressDao,
) {
    /** 生产代码入口:从 assets/words.json 加载。 */
    constructor(context: Context, dao: WordProgressDao) : this(
        wordsLoader = { loadWordsFromAssets(context) },
        dao = dao,
    )

    /**
     * 测试入口:跳过 assets 读取,直接使用预加载列表。
     * 用 internal 限定为同模块可见,避免被业务代码误用。
     */
    internal constructor(preloadedWords: List<Word>, dao: WordProgressDao) : this(
        wordsLoader = { preloadedWords },
        dao = dao,
    )

    /**
     * 缓存的词条列表。生产路径从 assets 加载,测试路径直接注入。
     */
    private val words: List<Word> by lazy { wordsLoader() }

    companion object {
        private const val TAG = "WordRepository"

        /**
         * I-3 fix: JSON 解析失败时返回空列表 + Log,而不是崩溃。
         */
        private fun loadWordsFromAssets(context: Context): List<Word> =
            runCatching {
                context.assets.open("words.json").bufferedReader().use { reader ->
                    Json.decodeFromString<List<Word>>(reader.readText())
                }
            }.getOrElse { e ->
                Log.e(TAG, "Failed to load assets/words.json: ${e.message}", e)
                // 返回空列表,UI 层会显示"暂无单词"状态,而不是崩溃
                emptyList()
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
