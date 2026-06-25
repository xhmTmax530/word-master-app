package com.wordmaster.app.data.repository

import com.wordmaster.app.data.db.WordProgressDao
import com.wordmaster.app.data.db.WordProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 测试用 Fake DAO — 实现 WordProgressDao 接口,用内存 Map 模拟 Room。
 * 不依赖 Android 框架,跑在普通 JVM 上。
 *
 * 行为:
 * - upsert/upsertAll/initializeAllWords 全部写入内部 Map(模拟 REPLACE/IGNORE 策略由 Repository 决定)
 * - observeAll 返回 StateFlow,可被 observeStudyCards 收集
 * - getByWordId/getDueWords 按需查询
 */
class FakeWordProgressDao(
    initial: Map<Int, WordProgressEntity> = emptyMap(),
) : WordProgressDao {
    private val state = MutableStateFlow(initial.values.toList())
    val rows: Flow<List<WordProgressEntity>> = state.asStateFlow()

    override suspend fun upsert(entity: WordProgressEntity) {
        val newMap = state.value.associateBy { it.wordId }.toMutableMap()
        newMap[entity.wordId] = entity
        state.value = newMap.values.toList()
    }

    override suspend fun upsertAll(entities: List<WordProgressEntity>) {
        val newMap = state.value.associateBy { it.wordId }.toMutableMap()
        entities.forEach { newMap[it.wordId] = it }
        state.value = newMap.values.toList()
    }

    override suspend fun initializeAllWords(entities: List<WordProgressEntity>) {
        // 模拟 IGNORE 策略:已存在的 wordId 不覆盖
        val newMap = state.value.associateBy { it.wordId }.toMutableMap()
        entities.forEach { e -> if (e.wordId !in newMap) newMap[e.wordId] = e }
        state.value = newMap.values.toList()
    }

    override fun observeAll(): Flow<List<WordProgressEntity>> = rows

    override suspend fun getByWordId(wordId: Int): WordProgressEntity? = state.value.firstOrNull { it.wordId == wordId }

    override suspend fun getDueWords(now: Long): List<WordProgressEntity> = state.value.filter { it.stage > 0 && it.nextReviewAt <= now }
}
