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
    fun loadProgressCheckpoint(): ProgressCheckpoint?
    fun saveProgressCheckpoint(checkpoint: ProgressCheckpoint?)
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
    QuickReview,
    FreeReview
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

internal data class ProgressCheckpoint(
    val contentVersion: String,
    val supportLanguage: SupportLanguage,
    val lessonId: String,
    val wordId: String?,
    val phase: PersistedLessonPhase
)

private const val ProgressCheckpointFieldSeparator = "\u001F"

internal fun ProgressCheckpoint.toStorageString(): String = listOf(
    contentVersion,
    supportLanguage.name,
    lessonId,
    wordId.orEmpty(),
    phase.name
).joinToString(ProgressCheckpointFieldSeparator)

internal fun String.toProgressCheckpointOrNull(): ProgressCheckpoint? {
    val parts = split(ProgressCheckpointFieldSeparator)
    if (parts.size != 5) return null
    return ProgressCheckpoint(
        contentVersion = parts[0],
        supportLanguage = SupportLanguage.entries.firstOrNull { it.name == parts[1] } ?: return null,
        lessonId = parts[2],
        wordId = parts[3].ifBlank { null },
        phase = PersistedLessonPhase.entries.firstOrNull { it.name == parts[4] } ?: return null
    )
}
