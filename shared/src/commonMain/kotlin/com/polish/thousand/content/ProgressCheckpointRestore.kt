package com.polish.thousand.content

internal data class RestoredProgressState(
    val supportLanguage: SupportLanguage,
    val completedLessonIds: Set<String>,
    val learnedWordIds: Set<String>,
    val activeSession: ActiveSession?,
    val lessonSession: PersistedLessonSession?
)

internal fun ProgressCheckpoint.toRestoredProgressState(): RestoredProgressState? {
    val topic = MvpSeedContent.path
    val lessonIndex = topic.lessons.indexOfFirst { it.id == lessonId }
    if (lessonIndex < 0) return null

    val completedLessonIds = topic.lessons
        .take(lessonIndex)
        .mapTo(mutableSetOf()) { it.id }
    val learnedWordIds = topic.lessons
        .take(lessonIndex)
        .flatMapTo(mutableSetOf()) { lesson -> lesson.items.map { it.id } }
    val lesson = topic.lessons[lessonIndex]

    if (wordId == null) {
        if (lessonIndex != topic.lessons.lastIndex) return null
        return RestoredProgressState(
            supportLanguage = supportLanguage,
            completedLessonIds = topic.lessons.mapTo(mutableSetOf()) { it.id },
            learnedWordIds = topic.lessons
                .flatMapTo(mutableSetOf()) { currentLesson -> currentLesson.items.map { it.id } },
            activeSession = null,
            lessonSession = null
        )
    }

    val itemIndex = lesson.items.indexOfFirst { it.id == wordId }
    if (itemIndex < 0) return null
    val normalizedPhase = phase.takeUnless { it == PersistedLessonPhase.Review }
        ?: PersistedLessonPhase.Learn
    val sessionKey = "lesson:${topic.id}:${lesson.id}"
    return RestoredProgressState(
        supportLanguage = supportLanguage,
        completedLessonIds = completedLessonIds,
        learnedWordIds = learnedWordIds,
        activeSession = ActiveSession(
            type = ActiveSessionType.Lesson,
            topicId = topic.id,
            lessonId = lesson.id
        ),
        lessonSession = PersistedLessonSession(
            sessionKey = sessionKey,
            phase = normalizedPhase,
            reviewIndex = 0,
            learnIndex = if (normalizedPhase == PersistedLessonPhase.Learn) itemIndex else 0,
            practiceIndex = if (normalizedPhase == PersistedLessonPhase.Practice) itemIndex else 0,
            selectedAnswer = null,
            submittedAnswer = null,
            correctPracticeWordIds = emptySet(),
            isReviewAnswerVisible = false
        )
    )
}
