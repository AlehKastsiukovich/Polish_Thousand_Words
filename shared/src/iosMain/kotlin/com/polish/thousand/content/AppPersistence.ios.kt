package com.polish.thousand.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
internal actual fun rememberAppPersistence(): AppPersistence = remember {
    IosAppPersistence(NSUserDefaults.standardUserDefaults)
}

private class IosAppPersistence(
    private val defaults: NSUserDefaults
) : AppPersistence {
    override fun loadSupportLanguage(): SupportLanguage? = defaults
        .stringForKey(SupportLanguageKey)
        ?.let { stored -> SupportLanguage.entries.firstOrNull { it.name == stored } }

    override fun saveSupportLanguage(language: SupportLanguage) {
        defaults.setObject(language.name, forKey = SupportLanguageKey)
    }

    override fun loadCompletedLessonIds(): Set<String> = defaults
        .stringForKey(CompletedLessonsKey)
        .orEmpty()
        .split(LessonIdSeparator)
        .filterTo(mutableSetOf()) { it.isNotBlank() }

    override fun saveCompletedLessonIds(lessonIds: Set<String>) {
        defaults.setObject(lessonIds.sorted().joinToString(LessonIdSeparator), forKey = CompletedLessonsKey)
    }

    override fun loadLearnedWordIds(): Set<String> = defaults
        .stringForKey(LearnedWordsKey)
        .orEmpty()
        .split(LessonIdSeparator)
        .filterTo(mutableSetOf()) { it.isNotBlank() }

    override fun saveLearnedWordIds(wordIds: Set<String>) {
        defaults.setObject(wordIds.sorted().joinToString(LessonIdSeparator), forKey = LearnedWordsKey)
    }

    override fun loadReviewStates(): Map<String, WordReviewState> = defaults
        .stringForKey(ReviewStatesKey)
        .orEmpty()
        .split(ReviewRecordSeparator)
        .mapNotNull { it.toWordReviewStateOrNull() }
        .associateBy { it.wordId }

    override fun saveReviewStates(states: Map<String, WordReviewState>) {
        defaults.setObject(
            states.values.joinToString(ReviewRecordSeparator) { it.toStorageString() },
            forKey = ReviewStatesKey
        )
    }

    override fun loadPendingQuickReview(): PendingQuickReview? = defaults
        .stringForKey(PendingQuickReviewKey)
        ?.toPendingQuickReviewOrNull()

    override fun savePendingQuickReview(review: PendingQuickReview?) {
        if (review == null) {
            defaults.removeObjectForKey(PendingQuickReviewKey)
        } else {
            defaults.setObject(review.toStorageString(), forKey = PendingQuickReviewKey)
        }
    }

    override fun loadActiveSession(): ActiveSession? = defaults
        .stringForKey(ActiveSessionKey)
        ?.toActiveSessionOrNull()

    override fun saveActiveSession(session: ActiveSession?) {
        if (session == null) defaults.removeObjectForKey(ActiveSessionKey)
        else defaults.setObject(session.toStorageString(), forKey = ActiveSessionKey)
    }

    override fun loadLessonSession(): PersistedLessonSession? = defaults
        .stringForKey(LessonSessionKey)
        ?.toPersistedLessonSessionOrNull()

    override fun saveLessonSession(session: PersistedLessonSession?) {
        if (session == null) defaults.removeObjectForKey(LessonSessionKey)
        else defaults.setObject(session.toStorageString(), forKey = LessonSessionKey)
    }

    override fun loadHasPremium(): Boolean = defaults.boolForKey(HasPremiumKey)

    override fun saveHasPremium(hasPremium: Boolean) {
        defaults.setBool(hasPremium, forKey = HasPremiumKey)
    }

    override fun loadHasSeenPaywall(): Boolean = defaults.boolForKey(HasSeenPaywallKey)

    override fun saveHasSeenPaywall(hasSeenPaywall: Boolean) {
        defaults.setBool(hasSeenPaywall, forKey = HasSeenPaywallKey)
    }

    override fun loadActiveDays(): Set<Long> = defaults
        .stringForKey(ActiveDaysKey)
        .orEmpty()
        .split(LessonIdSeparator)
        .mapNotNullTo(mutableSetOf()) { it.toLongOrNull() }

    override fun saveActiveDays(days: Set<Long>) {
        defaults.setObject(
            days.sorted().joinToString(LessonIdSeparator),
            forKey = ActiveDaysKey
        )
    }
}

private const val SupportLanguageKey = "support_language"
private const val CompletedLessonsKey = "completed_lesson_ids"
private const val LearnedWordsKey = "learned_word_ids"
private const val LessonIdSeparator = ","
private const val ReviewStatesKey = "review_states"
private const val ReviewStateSeparator = "|"
private const val ReviewRecordSeparator = ";"
private const val PendingQuickReviewKey = "pending_quick_review"
private const val ActiveSessionKey = "active_session"
private const val LessonSessionKey = "lesson_session"
private const val PendingQuickReviewWordSeparator = ","
private const val SessionFieldSeparator = "\u001F"
private const val SessionItemSeparator = "\u001E"
private const val HasPremiumKey = "has_premium"
private const val HasSeenPaywallKey = "has_seen_paywall"
private const val ActiveDaysKey = "active_days"

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
    wordIds.joinToString(SessionItemSeparator),
    learnedWordsBeforeReview?.toString().orEmpty()
).joinToString(SessionFieldSeparator)

private fun String.toActiveSessionOrNull(): ActiveSession? {
    val parts = split(SessionFieldSeparator)
    if (parts.size !in 4..5) return null
    return ActiveSession(
        type = ActiveSessionType.entries.firstOrNull { it.name == parts[0] } ?: return null,
        topicId = parts[1],
        lessonId = parts[2],
        wordIds = parts[3].split(SessionItemSeparator).filter { it.isNotBlank() },
        learnedWordsBeforeReview = parts.getOrNull(4)?.toIntOrNull()
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
