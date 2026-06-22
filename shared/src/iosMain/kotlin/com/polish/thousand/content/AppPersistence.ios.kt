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

    override fun loadHasPremium(): Boolean = defaults.boolForKey(HasPremiumKey)

    override fun saveHasPremium(hasPremium: Boolean) {
        defaults.setBool(hasPremium, forKey = HasPremiumKey)
    }

    override fun loadHasSeenPaywall(): Boolean = defaults.boolForKey(HasSeenPaywallKey)

    override fun saveHasSeenPaywall(hasSeenPaywall: Boolean) {
        defaults.setBool(hasSeenPaywall, forKey = HasSeenPaywallKey)
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
private const val PendingQuickReviewWordSeparator = ","
private const val HasPremiumKey = "has_premium"
private const val HasSeenPaywallKey = "has_seen_paywall"

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
