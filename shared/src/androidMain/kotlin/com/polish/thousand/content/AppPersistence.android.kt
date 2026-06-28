package com.polish.thousand.content

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

@Composable
internal actual fun rememberAppPersistence(): AppPersistence {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidAppPersistence(context) }
}

private class AndroidAppPersistence(context: Context) : AppPersistence {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    override fun loadSupportLanguage(): SupportLanguage? = preferences
        .getString(SupportLanguageKey, null)
        ?.let { stored -> SupportLanguage.entries.firstOrNull { it.name == stored } }

    override fun saveSupportLanguage(language: SupportLanguage) {
        preferences.edit { putString(SupportLanguageKey, language.name) }
    }

    override fun loadCompletedLessonIds(): Set<String> =
        preferences.getStringSet(CompletedLessonsKey, emptySet()).orEmpty()

    override fun saveCompletedLessonIds(lessonIds: Set<String>) {
        preferences.edit { putStringSet(CompletedLessonsKey, lessonIds) }
    }

    override fun loadLearnedWordIds(): Set<String> =
        preferences.getStringSet(LearnedWordsKey, emptySet()).orEmpty()

    override fun saveLearnedWordIds(wordIds: Set<String>) {
        preferences.edit { putStringSet(LearnedWordsKey, wordIds) }
    }

    override fun loadReviewStates(): Map<String, WordReviewState> = preferences
        .getStringSet(ReviewStatesKey, emptySet())
        .orEmpty()
        .mapNotNull { it.toWordReviewStateOrNull() }
        .associateBy { it.wordId }

    override fun saveReviewStates(states: Map<String, WordReviewState>) {
        preferences.edit {
            putStringSet(
                ReviewStatesKey,
                states.values.mapTo(mutableSetOf()) { it.toStorageString() })
        }
    }

    override fun loadPendingQuickReview(): PendingQuickReview? = preferences
        .getString(PendingQuickReviewKey, null)
        ?.toPendingQuickReviewOrNull()

    override fun savePendingQuickReview(review: PendingQuickReview?) {
        preferences.edit {
            if (review == null) {
                remove(PendingQuickReviewKey)
            } else {
                putString(PendingQuickReviewKey, review.toStorageString())
            }
        }
    }

    override fun loadActiveSession(): ActiveSession? = preferences
        .getString(ActiveSessionKey, null)
        ?.toActiveSessionOrNull()

    override fun saveActiveSession(session: ActiveSession?) {
        preferences.edit {
            if (session == null) remove(ActiveSessionKey)
            else putString(ActiveSessionKey, session.toStorageString())
        }
    }

    override fun loadLessonSession(): PersistedLessonSession? = preferences
        .getString(LessonSessionKey, null)
        ?.toPersistedLessonSessionOrNull()

    override fun saveLessonSession(session: PersistedLessonSession?) {
        preferences.edit {
            if (session == null) remove(LessonSessionKey)
            else putString(LessonSessionKey, session.toStorageString())
        }
    }

    override fun loadHasPremium(): Boolean = preferences.getBoolean(HasPremiumKey, false)

    override fun saveHasPremium(hasPremium: Boolean) {
        preferences.edit { putBoolean(HasPremiumKey, hasPremium) }
    }

    override fun loadHasSeenPaywall(): Boolean = preferences.getBoolean(HasSeenPaywallKey, false)

    override fun saveHasSeenPaywall(hasSeenPaywall: Boolean) {
        preferences.edit { putBoolean(HasSeenPaywallKey, hasSeenPaywall) }
    }

    override fun loadActiveDays(): Set<Long> = preferences
        .getStringSet(ActiveDaysKey, emptySet())
        .orEmpty()
        .mapNotNullTo(mutableSetOf()) { it.toLongOrNull() }

    override fun saveActiveDays(days: Set<Long>) {
        preferences.edit {
            putStringSet(ActiveDaysKey, days.mapTo(mutableSetOf()) { it.toString() })
        }
    }
}

private const val PreferencesName = "polish_thousand_progress"
private const val SupportLanguageKey = "support_language"
private const val CompletedLessonsKey = "completed_lesson_ids"
private const val LearnedWordsKey = "learned_word_ids"
private const val ReviewStatesKey = "review_states"
private const val PendingQuickReviewKey = "pending_quick_review"
private const val ActiveSessionKey = "active_session"
private const val LessonSessionKey = "lesson_session"
private const val HasPremiumKey = "has_premium"
private const val HasSeenPaywallKey = "has_seen_paywall"
private const val ActiveDaysKey = "active_days"
private const val ReviewStateSeparator = "|"
private const val PendingQuickReviewWordSeparator = ","
private const val SessionFieldSeparator = "\u001F"
private const val SessionItemSeparator = "\u001E"

private fun WordReviewState.toStorageString(): String = listOf(
    wordId,
    intervalDays.toString(),
    dueEpochDay.toString(),
    successfulReviews.toString()
).joinToString(ReviewStateSeparator)

private fun String.toWordReviewStateOrNull(): WordReviewState? {
    val parts = split(ReviewStateSeparator)
    if (parts.size != 4) return null
    return WordReviewState(
        wordId = parts[0],
        intervalDays = parts[1].toIntOrNull() ?: return null,
        dueEpochDay = parts[2].toLongOrNull() ?: return null,
        successfulReviews = parts[3].toIntOrNull() ?: return null
    )
}

private fun PendingQuickReview.toStorageString(): String = listOf(
    lessonId,
    createdEpochDay.toString(),
    wordIds.sorted().joinToString(PendingQuickReviewWordSeparator)
).joinToString(ReviewStateSeparator)

private fun String.toPendingQuickReviewOrNull(): PendingQuickReview? {
    val parts = split(ReviewStateSeparator)
    if (parts.size != 3) return null
    return PendingQuickReview(
        lessonId = parts[0],
        createdEpochDay = parts[1].toLongOrNull() ?: return null,
        wordIds = parts[2]
            .split(PendingQuickReviewWordSeparator)
            .filterTo(mutableSetOf()) { it.isNotBlank() }
    )
}

private fun ActiveSession.toStorageString(): String = listOf(
    type.name,
    topicId,
    lessonId,
    wordIds.joinToString(SessionItemSeparator)
).joinToString(SessionFieldSeparator)

private fun String.toActiveSessionOrNull(): ActiveSession? {
    val parts = split(SessionFieldSeparator)
    if (parts.size != 4) return null
    return ActiveSession(
        type = ActiveSessionType.entries.firstOrNull { it.name == parts[0] } ?: return null,
        topicId = parts[1],
        lessonId = parts[2],
        wordIds = parts[3].split(SessionItemSeparator).filter { it.isNotBlank() }
    )
}

private fun PersistedLessonSession.toStorageString(): String = listOf(
    sessionKey,
    phase.name,
    reviewIndex.toString(),
    learnIndex.toString(),
    practiceIndex.toString(),
    selectedAnswer.orEmpty(),
    submittedAnswer.orEmpty(),
    correctPracticeWordIds.joinToString(SessionItemSeparator),
    isReviewAnswerVisible.toString()
).joinToString(SessionFieldSeparator)

private fun String.toPersistedLessonSessionOrNull(): PersistedLessonSession? {
    val parts = split(SessionFieldSeparator)
    if (parts.size != 9) return null
    return PersistedLessonSession(
        sessionKey = parts[0],
        phase = PersistedLessonPhase.entries.firstOrNull { it.name == parts[1] } ?: return null,
        reviewIndex = parts[2].toIntOrNull() ?: return null,
        learnIndex = parts[3].toIntOrNull() ?: return null,
        practiceIndex = parts[4].toIntOrNull() ?: return null,
        selectedAnswer = parts[5].ifBlank { null },
        submittedAnswer = parts[6].ifBlank { null },
        correctPracticeWordIds = parts[7]
            .split(SessionItemSeparator)
            .filterTo(mutableSetOf()) { it.isNotBlank() },
        isReviewAnswerVisible = parts[8].toBooleanStrictOrNull() ?: false
    )
}
