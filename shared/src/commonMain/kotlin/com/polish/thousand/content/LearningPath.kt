package com.polish.thousand.content

internal const val LearningTargetWords = 1000

internal data class LearningMilestone(
    val wordCount: Int,
    val titleUkrainian: String,
    val titleRussian: String
) {
    fun titleFor(language: SupportLanguage): String = when (language) {
        SupportLanguage.Ukrainian -> titleUkrainian
        SupportLanguage.Russian -> titleRussian
    }
}

internal sealed interface CompletionRecognition {
    data object FirstStep : CompletionRecognition
    data object HalfwayToFirstMilestone : CompletionRecognition
    data class CompactMilestone(val wordCount: Int) : CompletionRecognition
}

internal object LearningPath {
    val celebrationMilestones = setOf(100, 500, 1000)

    val milestones = listOf(
        LearningMilestone(100, "Розігрів", "Разогрев"),
        LearningMilestone(250, "База", "База"),
        LearningMilestone(500, "Впевненість", "Уверенность"),
        LearningMilestone(750, "У потоці", "В потоке"),
        LearningMilestone(1000, "Тисяча в дії", "Тысяча в деле")
    )

    fun nextMilestone(learnedWords: Int): LearningMilestone =
        milestones.firstOrNull { learnedWords < it.wordCount } ?: milestones.last()

    fun milestoneProgress(learnedWords: Int): Float {
        if (learnedWords >= milestones.last().wordCount) return 1f
        val next = nextMilestone(learnedWords)
        val previous = milestones.lastOrNull { it.wordCount <= learnedWords }?.wordCount ?: 0
        val range = (next.wordCount - previous).coerceAtLeast(1)
        return ((learnedWords - previous).toFloat() / range).coerceIn(0f, 1f)
    }

    fun crossedMilestone(previousLearnedWords: Int, learnedWords: Int): LearningMilestone? =
        milestones.lastOrNull { previousLearnedWords < it.wordCount && learnedWords >= it.wordCount }

    fun crossedCelebrationMilestone(previousLearnedWords: Int, learnedWords: Int): LearningMilestone? =
        milestones.lastOrNull {
            it.wordCount in celebrationMilestones &&
                previousLearnedWords < it.wordCount &&
                learnedWords >= it.wordCount
        }

    fun completionRecognition(
        previousLearnedWords: Int,
        learnedWords: Int
    ): CompletionRecognition? = when {
        previousLearnedWords < 10 && learnedWords >= 10 -> CompletionRecognition.FirstStep
        previousLearnedWords < 50 && learnedWords >= 50 -> CompletionRecognition.HalfwayToFirstMilestone
        previousLearnedWords < 250 && learnedWords >= 250 -> CompletionRecognition.CompactMilestone(250)
        previousLearnedWords < 750 && learnedWords >= 750 -> CompletionRecognition.CompactMilestone(750)
        else -> null
    }
}
