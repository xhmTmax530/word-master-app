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
    /**
     * 是否到期:`nextReviewAt <= now` 且已学过(stage>0)。
     *
     * now 作为参数传入,不在此处调 `System.currentTimeMillis()`,
     * 保证单测稳定且 GitHub Actions runner 上不依赖 wall clock。
     */
    fun isDue(now: Long): Boolean = progress.nextReviewAt <= now && progress.stage > 0
}
