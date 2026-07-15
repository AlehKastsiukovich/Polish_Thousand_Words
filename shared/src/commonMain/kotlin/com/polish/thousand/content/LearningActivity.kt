package com.polish.thousand.content

internal data class ActivityOverview(
    val todayEpochDay: Long,
    val streakDays: Int,
    val activeDaysInWindow: Int,
    val recentDays: List<ActivityDay>
)

internal data class ActivityDay(
    val epochDay: Long,
    val isActive: Boolean
)

internal object LearningActivity {
    fun overview(
        activeDays: Set<Long>,
        todayEpochDay: Long,
        windowDays: Int = 7
    ): ActivityOverview {
        val normalizedWindow = windowDays.coerceAtLeast(1)
        val weekStart = todayEpochDay - weekdayIndexMondayFirst(todayEpochDay)
        val recentDays = (0 until normalizedWindow).map { offset ->
            val epochDay = weekStart + offset
            ActivityDay(
                epochDay = epochDay,
                isActive = epochDay in activeDays
            )
        }

        return ActivityOverview(
            todayEpochDay = todayEpochDay,
            streakDays = streakDays(activeDays, todayEpochDay),
            activeDaysInWindow = recentDays.count { it.isActive },
            recentDays = recentDays
        )
    }

    private fun streakDays(
        activeDays: Set<Long>,
        todayEpochDay: Long
    ): Int {
        var streak = 0
        var day = todayEpochDay
        while (day in activeDays) {
            streak += 1
            day -= 1
        }
        return streak
    }

    private fun weekdayIndexMondayFirst(epochDay: Long): Int =
        (((epochDay + 3) % 7 + 7) % 7).toInt()
}
