package com.wordmaster.app.data.model

/**
 * 复习状态机枚举。算法专员据此决定下一步走向。
 */
enum class ReviewOutcome { KNOWN, FORGOTTEN }

/**
 * 一张学习卡片的视图层数据(UI 直接消费的形态)。
 *
 * 把 Word 与其 WordProgress 合并,ViewModel 一份就能喂饱 Compose,
 * 避免 UI 层自己再做 join。
 */
data class StudyCard(
    val word: Word,
    val progress: WordProgress,
) {
    val isDue: Boolean
        get() = progress.nextReviewAt <= System.currentTimeMillis() && progress.stage > 0
}