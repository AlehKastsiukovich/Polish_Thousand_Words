package com.polish.thousand.content

import kotlin.test.Test
import kotlin.test.assertEquals

class LearningActivityTest {

    @Test
    fun overviewUsesSevenDayWindowByDefault() {
        val overview = LearningActivity.overview(
            activeDays = setOf(3L, 8L, 10L),
            todayEpochDay = 10L
        )

        assertEquals(
            expected = listOf(10L, 9L, 8L, 7L, 6L, 5L, 4L),
            actual = overview.recentDays.map { it.epochDay }
        )
        assertEquals(expected = 2, actual = overview.activeDaysInWindow)
    }

    @Test
    fun overviewPlacesFirstStudyDayIntoFirstCell() {
        val overview = LearningActivity.overview(
            activeDays = setOf(10L),
            todayEpochDay = 10L
        )

        assertEquals(expected = true, actual = overview.recentDays.first().isActive)
        assertEquals(expected = false, actual = overview.recentDays.last().isActive)
    }

    @Test
    fun overviewKeepsStreakIndependentFromWindowCount() {
        val overview = LearningActivity.overview(
            activeDays = setOf(6L, 7L, 8L, 9L, 10L),
            todayEpochDay = 10L
        )

        assertEquals(expected = 5, actual = overview.streakDays)
        assertEquals(expected = 5, actual = overview.activeDaysInWindow)
    }
}
