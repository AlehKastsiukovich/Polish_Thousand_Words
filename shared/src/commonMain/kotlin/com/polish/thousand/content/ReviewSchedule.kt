package com.polish.thousand.content

internal enum class ReviewQuality {
    Forgot,
    Almost,
    Know
}

internal data class WordReviewState(
    val wordId: String,
    val intervalDays: Int,
    val dueEpochDay: Long,
    val successfulReviews: Int
)

internal object ReviewSchedule {
    private val graduationIntervals = listOf(1, 3, 7, 14, 30, 60, 120)

    fun dueWordCount(
        states: Map<String, WordReviewState>,
        todayEpochDay: Long
    ): Int = states.values.count { it.dueEpochDay <= todayEpochDay }

    fun dueWordIds(
        states: Map<String, WordReviewState>,
        todayEpochDay: Long,
        limit: Int = 10
    ): List<String> = states.values
        .asSequence()
        .filter { it.dueEpochDay <= todayEpochDay }
        .sortedWith(compareBy<WordReviewState> { it.dueEpochDay }.thenBy { it.wordId })
        .take(limit)
        .map { it.wordId }
        .toList()

    fun scheduleNewWords(
        states: Map<String, WordReviewState>,
        words: List<LessonItemContent>,
        todayEpochDay: Long
    ): Map<String, WordReviewState> {
        val updated = states.toMutableMap()
        words.forEach { word ->
            if (word.id !in updated) {
                updated[word.id] = WordReviewState(
                    wordId = word.id,
                    intervalDays = 1,
                    dueEpochDay = todayEpochDay + 1,
                    successfulReviews = 0
                )
            }
        }
        return updated
    }

    fun recordScheduledAnswer(
        states: Map<String, WordReviewState>,
        wordId: String,
        quality: ReviewQuality,
        todayEpochDay: Long
    ): Map<String, WordReviewState> {
        val current = states[wordId] ?: WordReviewState(
            wordId = wordId,
            intervalDays = 1,
            dueEpochDay = todayEpochDay,
            successfulReviews = 0
        )

        val next = when (quality) {
            ReviewQuality.Forgot -> current.copy(
                intervalDays = 1,
                dueEpochDay = todayEpochDay + 1,
                successfulReviews = 0
            )
            ReviewQuality.Almost -> {
                val interval = (current.intervalDays / 2).coerceAtLeast(1)
                current.copy(
                    intervalDays = interval,
                    dueEpochDay = todayEpochDay + interval,
                    successfulReviews = (current.successfulReviews - 1).coerceAtLeast(0)
                )
            }
            ReviewQuality.Know -> {
                val interval = graduationIntervals
                    .firstOrNull { it > current.intervalDays }
                    ?: graduationIntervals.last()
                current.copy(
                    intervalDays = interval,
                    dueEpochDay = todayEpochDay + interval,
                    successfulReviews = current.successfulReviews + 1
                )
            }
        }

        return states + (wordId to next)
    }

    fun recordEarlyAnswer(
        states: Map<String, WordReviewState>,
        wordId: String,
        quality: ReviewQuality,
        todayEpochDay: Long
    ): Map<String, WordReviewState> {
        val current = states[wordId] ?: WordReviewState(
            wordId = wordId,
            intervalDays = 1,
            dueEpochDay = todayEpochDay + 1,
            successfulReviews = 0
        )
        val next = when (quality) {
            ReviewQuality.Know -> current
            ReviewQuality.Almost -> current.copy(
                intervalDays = 1,
                dueEpochDay = minOf(current.dueEpochDay, todayEpochDay + 1)
            )
            ReviewQuality.Forgot -> current.copy(
                intervalDays = 1,
                dueEpochDay = todayEpochDay + 1,
                successfulReviews = 0
            )
        }
        return states + (wordId to next)
    }
}

internal fun List<LessonContent>.itemsByIds(ids: Iterable<String>): List<LessonItemContent> {
    val itemsById = flatMap { it.items }.associateBy { it.id }
    return ids.mapNotNull(itemsById::get)
}

internal expect fun currentEpochDay(): Long
