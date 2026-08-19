package com.polish.thousand.content

import kotlin.test.Test
import kotlin.test.assertEquals

class ProgressCheckpointTest {

    @Test
    fun checkpointRoundTripsWithCurrentWord() {
        val checkpoint = ProgressCheckpoint(
            contentVersion = MvpContentVersion,
            supportLanguage = SupportLanguage.Ukrainian,
            lessonId = "lesson-3",
            wordId = "0021_word",
            phase = PersistedLessonPhase.Practice
        )

        assertEquals(checkpoint, checkpoint.toStorageString().toProgressCheckpointOrNull())
    }

    @Test
    fun completedCheckpointRoundTripsWithoutWord() {
        val checkpoint = ProgressCheckpoint(
            contentVersion = MvpContentVersion,
            supportLanguage = SupportLanguage.Russian,
            lessonId = "lesson-100",
            wordId = null,
            phase = PersistedLessonPhase.Practice
        )

        assertEquals(checkpoint, checkpoint.toStorageString().toProgressCheckpointOrNull())
    }

    @Test
    fun malformedCheckpointIsIgnored() {
        assertEquals(null, "not-a-checkpoint".toProgressCheckpointOrNull())
    }

    @Test
    fun currentWordCheckpointRestoresTheLessonSession() {
        val lesson = MvpSeedContent.lessons.first()
        val checkpoint = ProgressCheckpoint(
            contentVersion = MvpContentVersion,
            supportLanguage = SupportLanguage.Russian,
            lessonId = lesson.id,
            wordId = lesson.items[2].id,
            phase = PersistedLessonPhase.Learn
        )

        val restored = checkpoint.toRestoredProgressState()

        assertEquals(emptySet(), restored?.completedLessonIds)
        assertEquals(2, restored?.lessonSession?.learnIndex)
        assertEquals(lesson.id, restored?.activeSession?.lessonId)
    }

    @Test
    fun completedCheckpointRestoresTheWholePath() {
        val lastLesson = MvpSeedContent.lessons.last()
        val checkpoint = ProgressCheckpoint(
            contentVersion = MvpContentVersion,
            supportLanguage = SupportLanguage.Ukrainian,
            lessonId = lastLesson.id,
            wordId = null,
            phase = PersistedLessonPhase.Practice
        )

        val restored = checkpoint.toRestoredProgressState()

        assertEquals(MvpSeedContent.lessons.size, restored?.completedLessonIds?.size)
        assertEquals(null, restored?.activeSession)
        assertEquals(null, restored?.lessonSession)
    }
}
