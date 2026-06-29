package com.polish.thousand.content

internal data class ActivityOverview(
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
        val recentDays = (0 until normalizedWindow).map { offset ->
            val epochDay = todayEpochDay - offset
            ActivityDay(
                epochDay = epochDay,
                isActive = epochDay in activeDays
            )
        }

        return ActivityOverview(
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
}
