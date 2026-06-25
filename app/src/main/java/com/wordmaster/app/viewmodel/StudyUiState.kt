package com.wordmaster.app.viewmodel

import com.wordmaster.app.data.model.StudyCard

/**
 * StudyScreen 的 UI 状态。
 *
 * @param currentCard 当前待学的卡片;null 表示没有更多词
 * @param learnedToday 今日已学单词数
 * @param targetToday  今日目标单词数
 * @param isLoading   是否正在加载
 * @param errorMessage 用户可读的错误信息;null 表示无错误
 * @param errorType    错误类型,UI 据此区分重试/退出等动作
 */
data class StudyUiState(
    val currentCard: StudyCard? = null,
    val learnedToday: Int = 0,
    val targetToday: Int = 20,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val errorType: ErrorType? = null,
)

/**
 * C-1 fix: 错误类型枚举,UI 层据此选择处理策略。
 */
enum class ErrorType {
    /** 词库加载失败(JSON 损坏 / 资产丢失) */
    WORDS_LOAD_FAILED,

    /** 数据库初始化失败 */
    DB_INIT_FAILED,

    /** 其他未知异常 */
    UNKNOWN,
}
