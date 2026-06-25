package com.wordmaster.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wordmaster.app.WordMasterApp
import com.wordmaster.app.data.db.WordProgressEntity
import com.wordmaster.app.data.model.ReviewOutcome
import com.wordmaster.app.data.repository.WordRepository
import com.wordmaster.app.logic.WordSelector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the study/review screen.
 *
 * 由 Logic Agent 实现,通过 Room + EbbinghausScheduler 驱动单词复习队列。
 * 状态契约见同包下的 StudyUiState。
 *
 * 修复:
 * - B-3: initializeProgress 用 IGNORE 而非 REPLACE,避免重启清空用户进度
 * - B-6: isFlipped 改为 Compose remember 派生,本 ViewModel 不再持有该状态
 * - I-2: init 块加 runCatching,失败时设置 errorMessage / errorType
 */
class StudyViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val database = (application as WordMasterApp).database
    private val dao = database.wordProgressDao()
    private val repository = WordRepository(application, dao)

    private val _state =
        MutableStateFlow(
            StudyUiState(
                targetToday = calculateTarget(runCatching { repository.allWords().size }.getOrDefault(0)),
            ),
        )
    val state: StateFlow<StudyUiState> = _state.asStateFlow()

    /**
     * Calculate daily target: total words / 5, minimum 10.
     */
    private fun calculateTarget(totalWords: Int): Int = (totalWords / 5).coerceAtLeast(10)

    init {
        viewModelScope.launch {
            // I-2 fix: init 加 try-catch,JSON 损坏或 DB 异常时不再让 CircularProgressIndicator 永远转
            runCatching {
                initializeProgress()
                loadNext()
            }.onFailure { e ->
                Log.e(TAG, "init failed", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "初始化失败:${e.message ?: e::class.simpleName}",
                        errorType = classifyError(e),
                    )
                }
            }
        }
    }

    /**
     * B-3 fix: 用 IGNORE 策略,只插入新词。已存在的记录不动,用户进度不丢。
     */
    private suspend fun initializeProgress() {
        val words = repository.allWords()
        if (words.isEmpty()) {
            // I-3 触发:词库空也算初始化,但要提示
            _state.update {
                it.copy(
                    errorMessage = "词库为空,请检查 assets/words.json",
                    errorType = ErrorType.WORDS_LOAD_FAILED,
                )
            }
            return
        }
        val defaultProgressList =
            words.map { word ->
                WordProgressEntity.fromDomain(
                    com.wordmaster.app.data.model
                        .WordProgress(wordId = word.id),
                )
            }
        dao.initializeAllWords(defaultProgressList)
    }

    /**
     * I-2 / C-1: 根据异常类型推断错误分类。
     */
    private fun classifyError(e: Throwable): ErrorType =
        when (e) {
            is kotlinx.serialization.SerializationException -> ErrorType.WORDS_LOAD_FAILED
            is android.database.sqlite.SQLiteException -> ErrorType.DB_INIT_FAILED
            else -> ErrorType.UNKNOWN
        }

    /**
     * Mark current card as known and load next.
     */
    fun markKnown() {
        viewModelScope.launch {
            val current = _state.value.currentCard ?: return@launch
            runCatching { repository.updateProgress(current.word.id, ReviewOutcome.KNOWN) }
                .onFailure { e ->
                    Log.e(TAG, "markKnown failed", e)
                    _state.update { it.copy(errorMessage = "保存进度失败:${e.message}", errorType = classifyError(e)) }
                    return@launch
                }
            _state.update { it.copy(learnedToday = it.learnedToday + 1) }
            loadNext()
        }
    }

    /**
     * Mark current card as forgotten and load next.
     */
    fun markForgotten() {
        viewModelScope.launch {
            val current = _state.value.currentCard ?: return@launch
            runCatching { repository.updateProgress(current.word.id, ReviewOutcome.FORGOTTEN) }
                .onFailure { e ->
                    Log.e(TAG, "markForgotten failed", e)
                    _state.update { it.copy(errorMessage = "保存进度失败:${e.message}", errorType = classifyError(e)) }
                    return@launch
                }
            loadNext()
        }
    }

    /**
     * Clear error state (e.g. user dismissed error toast).
     */
    fun dismissError() {
        _state.update { it.copy(errorMessage = null, errorType = null) }
    }

    /**
     * Load the next card to study.
     */
    private fun loadNext() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val cards = repository.observeStudyCards().first()
                WordSelector.pickNext(cards)
            }.onSuccess { nextCard ->
                _state.update { it.copy(currentCard = nextCard, isLoading = false) }
            }.onFailure { e ->
                Log.e(TAG, "loadNext failed", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载单词失败:${e.message ?: e::class.simpleName}",
                        errorType = classifyError(e),
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "StudyViewModel"
    }
}
