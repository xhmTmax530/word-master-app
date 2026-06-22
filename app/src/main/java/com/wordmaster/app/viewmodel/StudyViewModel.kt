package com.wordmaster.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * 学习界面的 ViewModel 接口。
 * Phase 2 由 Logic Agent 提供真实实现。
 */
abstract class StudyViewModel : ViewModel() {
    abstract val state: StateFlow<StudyUiState>
    abstract fun flipCard()
    abstract fun markKnown()
    abstract fun markForgotten()
}
