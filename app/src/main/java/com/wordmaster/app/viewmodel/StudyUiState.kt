package com.wordmaster.app.viewmodel

import com.wordmaster.app.data.model.StudyCard

/**
 * StudyScreen 的 UI 状态。
 *
 * @param currentCard 当前待学的卡片;null 表示没有更多词
 * @param isFlipped  卡片是否已翻转
 * @param learnedToday 今日已学单词数
 * @param targetToday  今日目标单词数
 * @param isLoading   是否正在加载
 */
data class StudyUiState(
    val currentCard: StudyCard? = null,
    val isFlipped: Boolean = false,
    val learnedToday: Int = 0,
    val targetToday: Int = 20,
    val isLoading: Boolean = false,
)
