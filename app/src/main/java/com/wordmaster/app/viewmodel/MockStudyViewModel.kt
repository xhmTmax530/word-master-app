package com.wordmaster.app.viewmodel

import com.wordmaster.app.data.model.StudyCard
import com.wordmaster.app.data.model.Word
import com.wordmaster.app.data.model.WordProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 临时兜底的 Mock ViewModel。
 * Phase 2 由 Logic Agent 提供的真实 StudyViewModel 替换。
 */
class MockStudyViewModel : StudyViewModel() {

    private val _state = MutableStateFlow(
        StudyUiState(
            currentCard = StudyCard(
                word = Word(id = 0, word = "apple", meaning = "苹果"),
                progress = WordProgress(wordId = 0),
            ),
            isFlipped = false,
            learnedToday = 5,
            targetToday = 20,
            isLoading = false,
        ),
    )
    override val state: StateFlow<StudyUiState> = _state

    override fun flipCard() {
        _state.update { it.copy(isFlipped = !it.isFlipped) }
    }

    override fun markKnown() {
        _state.update {
            it.copy(
                learnedToday = it.learnedToday + 1,
                isFlipped = false,
            )
        }
    }

    override fun markForgotten() {
        _state.update {
            it.copy(
                learnedToday = (it.learnedToday + 1).coerceAtMost(it.targetToday),
                isFlipped = false,
            )
        }
    }
}
