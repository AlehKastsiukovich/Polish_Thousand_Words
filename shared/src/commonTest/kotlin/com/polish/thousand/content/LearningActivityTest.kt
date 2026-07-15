package com.polish.thousand.content

import kotlin.test.Test
import kotlin.test.assertEquals

class LearningActivityTest {

    @Test
    fun overviewUsesCalendarWeekByDefault() {
        val overview = LearningActivity.overview(
            activeDays = setOf(3L, 8L, 10L),
            todayEpochDay = 10L
        )

        assertEquals(
            expected = listOf(4L, 5L, 6L, 7L, 8L, 9L, 10L),
            actual = overview.recentDays.map { it.epochDay }
        )
        assertEquals(expected = 2, actual = overview.activeDaysInWindow)
    }

    @Test
    fun overviewPlacesTodayIntoItsCalendarCell() {
        val overview = LearningActivity.overview(
            activeDays = setOf(10L),
            todayEpochDay = 10L
        )

        assertEquals(expected = false, actual = overview.recentDays.first().isActive)
        assertEquals(expected = true, actual = overview.recentDays.last().isActive)
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
