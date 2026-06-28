package com.polish.thousand.content

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LearningPathTest {

    @Test
    fun crossedMilestoneReturnsNullWhenThresholdNotReached() {
        assertNull(LearningPath.crossedMilestone(previousLearnedWords = 80, learnedWords = 99))
    }

    @Test
    fun crossedMilestoneReturnsThresholdWhenReachedExactly() {
        assertEquals(
            expected = 100,
            actual = LearningPath.crossedMilestone(
                previousLearnedWords = 90,
                learnedWords = 100
            )?.wordCount
        )
    }

    @Test
    fun crossedMilestoneReturnsThresholdWhenLessonOvershootsIt() {
        assertEquals(
            expected = 250,
            actual = LearningPath.crossedMilestone(
                previousLearnedWords = 244,
                learnedWords = 252
            )?.wordCount
        )
    }

    @Test
    fun crossedCelebrationMilestoneSkipsNonCelebrationThresholds() {
        assertNull(
            LearningPath.crossedCelebrationMilestone(
                previousLearnedWords = 244,
                learnedWords = 252
            )
        )
    }

    @Test
    fun crossedCelebrationMilestoneReturnsFiveHundred() {
        assertEquals(
            expected = 500,
            actual = LearningPath.crossedCelebrationMilestone(
                previousLearnedWords = 492,
                learnedWords = 503
            )?.wordCount
        )
    }
}
