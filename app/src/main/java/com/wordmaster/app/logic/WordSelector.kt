package com.wordmaster.app.logic

import com.wordmaster.app.data.model.StudyCard

/**
 * Selects the next word to study based on study cards.
 */
object WordSelector {
    /**
     * Pick the next card to study.
     *
     * Strategy:
     * 1. Prioritize due cards (isDue == true), pick one randomly
     * 2. Otherwise pick from new words (stage == 0) randomly
     * 3. Otherwise pick the one with earliest nextReviewAt
     * 4. Return null if no cards available
     */
    fun pickNext(
        cards: List<StudyCard>,
        now: Long = System.currentTimeMillis(),
    ): StudyCard? {
        if (cards.isEmpty()) return null

        // Separate due cards from non-due
        val dueCards = cards.filter { it.isDue }
        if (dueCards.isNotEmpty()) {
            return dueCards.random()
        }

        // New words (stage == 0)
        val newWords = cards.filter { it.progress.stage == 0 }
        if (newWords.isNotEmpty()) {
            return newWords.random()
        }

        // Sort by nextReviewAt ascending and pick the earliest
        return cards
            .filter { it.progress.nextReviewAt > 0 }
            .minByOrNull { it.progress.nextReviewAt }
    }
}
