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
    fun loadActiveSession(): ActiveSession?
    fun saveActiveSession(session: ActiveSession?)
    fun loadLessonSession(): PersistedLessonSession?
    fun saveLessonSession(session: PersistedLessonSession?)
    fun loadHasPremium(): Boolean
    fun saveHasPremium(hasPremium: Boolean)
    fun loadHasSeenPaywall(): Boolean
    fun saveHasSeenPaywall(hasSeenPaywall: Boolean)
    fun loadActiveDays(): Set<Long>
    fun saveActiveDays(days: Set<Long>)
}

@Composable
internal expect fun rememberAppPersistence(): AppPersistence

internal data class PendingQuickReview(
    val lessonId: String,
    val wordIds: Set<String>,
    val createdEpochDay: Long
)

internal enum class ActiveSessionType {
    Lesson,
    ScheduledReview,
    QuickReview
}

internal data class ActiveSession(
    val type: ActiveSessionType,
    val topicId: String,
    val lessonId: String,
    val wordIds: List<String> = emptyList(),
    val learnedWordsBeforeReview: Int? = null
)

internal enum class PersistedLessonPhase {
    Review,
    Learn,
    Practice
}

internal data class PersistedLessonSession(
    val sessionKey: String,
    val phase: PersistedLessonPhase,
    val reviewIndex: Int,
    val learnIndex: Int,
    val practiceIndex: Int,
    val selectedAnswer: String?,
    val submittedAnswer: String?,
    val correctPracticeWordIds: Set<String>,
    val isReviewAnswerVisible: Boolean
)
