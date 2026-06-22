package com.wordmaster.app.data.model

/**
 * 单个单词的学习进度(Room 表 word_progress 的实体)。
 *
 * @param wordId 关联的 Word.id
 * @param stage  艾宾浩斯阶段序号(0 = 新词,1..N = 第 N 次复习)
 * @param nextReviewAt 下次复习的时间戳(epoch millis)。<=now 表示到期。
 * @param correctCount 累计答对次数
 * @param wrongCount   累计答错次数
 * @param lastReviewedAt 最近一次复习的时间戳(epoch millis);未复习过则为 0L
 */
data class WordProgress(
    val wordId: Int,
    val stage: Int = 0,
    val nextReviewAt: Long = 0L,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewedAt: Long = 0L,
)