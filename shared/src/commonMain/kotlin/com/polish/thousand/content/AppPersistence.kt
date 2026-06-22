package com.polish.thousand.content

import androidx.compose.runtime.Composable

internal interface AppPersistence {
    fun loadSupportLanguage(): SupportLanguage?
    fun saveSupportLanguage(language: SupportLanguage)
    fun loadCompletedLessonIds(): Set<String>
    fun saveCompletedLessonIds(lessonIds: Set<String>)
    fun loadLearnedWordIds(): Set<String>
    fun saveLearnedWordIds(wordIds: Set<String>)
    fun loadReviewStates(): Map<String, WordReviewState>
    fun saveReviewStates(states: Map<String, WordReviewState>)
    fun loadPendingQuickReview(): PendingQuickReview?
    fun savePendingQuickReview(review: PendingQuickReview?)
    fun loadHasPremium(): Boolean
    fun saveHasPremium(hasPremium: Boolean)
    fun loadHasSeenPaywall(): Boolean
    fun saveHasSeenPaywall(hasSeenPaywall: Boolean)
}

@Composable
internal expect fun rememberAppPersistence(): AppPersistence

internal data class PendingQuickReview(
    val lessonId: String,
    val wordIds: Set<String>,
    val createdEpochDay: Long
)
