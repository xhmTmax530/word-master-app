package com.wordmaster.app.data.model

import kotlinx.serialization.Serializable

/**
 * 词条基础数据(来自 assets/words.json 的只读记录)。
 *
 * MVP 字段精简版:只含单词本体与中文释义。
 * 后续可扩展 phonetic / partOfSpeech / example / audio。
 */
@Serializable
data class Word(
    val id: Int,
    val word: String,
    val meaning: String,
)
