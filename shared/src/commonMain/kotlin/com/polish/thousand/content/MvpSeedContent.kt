package com.polish.thousand.content

internal enum class ExerciseType {
    ChooseTranslation,
    ListenAndChoose,
    UnderstandInContext
}

internal data class LessonExampleContent(
    val polish: String,
    val russian: String,
    val ukrainian: String
)

internal data class LessonItemContent(
    val id: String,
    val polish: String,
    val russian: String,
    val ukrainian: String,
    val examples: List<LessonExampleContent>,
    val note: String? = null
)

internal data class LessonContent(
    val id: String,
    val title: String,
    val description: String,
    val estimatedMinutes: Int,
    val exerciseTypes: List<ExerciseType>,
    val items: List<LessonItemContent>
)

internal data class TopicContent(
    val id: String,
    val title: String,
    val description: String,
    val lessons: List<LessonContent>
)

internal object MvpSeedContent {
    val path: TopicContent = corePath
    val lessons: List<LessonContent> = path.lessons
    val totalLessons: Int = lessons.size
    val totalItems: Int = lessons.sumOf { it.items.size }

    fun completedWordCount(completedLessonIds: Set<String>): Int = lessons
        .filter { it.id in completedLessonIds }
        .sumOf { it.items.size }

    fun nextLesson(completedLessonIds: Set<String>): LessonContent? =
        lessons.firstOrNull { it.id !in completedLessonIds }
}

internal val defaultExerciseTypes = listOf(
    ExerciseType.ChooseTranslation,
    ExerciseType.ListenAndChoose,
    ExerciseType.UnderstandInContext
)

private val corePath = TopicContent(
    id = "b1_core",
    title = "Polish 1000",
    description = "The most useful Polish words and phrases for confident everyday communication.",
    lessons = generatedB1RuLessons
)
