package com.wordmaster.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wordmaster.app.WordMasterApp
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
                targetToday = calculateTarget(repository.allWords().size),
            ),
        )
    val state: StateFlow<StudyUiState> = _state.asStateFlow()

    /**
     * Calculate daily target: total words / 5, minimum 10.
     */
    private fun calculateTarget(totalWords: Int): Int = (totalWords / 5).coerceAtLeast(10)

    init {
        viewModelScope.launch {
            initializeProgress()
            loadNext()
        }
    }

    /**
     * Ensure all words have a progress record (stage 0).
     */
    private suspend fun initializeProgress() {
        val words = repository.allWords()
        val defaultProgressList =
            words.map { word ->
                com.wordmaster.app.data.db.WordProgressEntity.fromDomain(
                    com.wordmaster.app.data.model
                        .WordProgress(wordId = word.id),
                )
            }
        dao.upsertAll(defaultProgressList)
    }

    /**
     * Flip the current card to show answer.
     */
    fun flipCard() {
        _state.update { it.copy(isFlipped = !it.isFlipped) }
    }

    /**
     * Mark current card as known and load next.
     */
    fun markKnown() {
        viewModelScope.launch {
            val current = _state.value.currentCard ?: return@launch
            repository.updateProgress(current.word.id, ReviewOutcome.KNOWN)
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
            repository.updateProgress(current.word.id, ReviewOutcome.FORGOTTEN)
            loadNext()
        }
    }

    /**
     * Load the next card to study.
     */
    private fun loadNext() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val cards = repository.observeStudyCards().first()
            val nextCard = WordSelector.pickNext(cards)
            _state.update {
                it.copy(
                    currentCard = nextCard,
                    isFlipped = false,
                    isLoading = false,
                )
            }
        }
    }
}
