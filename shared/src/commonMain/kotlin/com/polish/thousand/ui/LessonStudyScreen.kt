package com.polish.thousand.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.polish.thousand.content.LessonContent
import com.polish.thousand.content.LessonItemContent
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.TopicContent
import com.polish.thousand.content.exampleForSelectedLanguage
import com.polish.thousand.content.translationForSelectedLanguage
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

private enum class LessonPhase {
    Learn,
    Practice
}

@Composable
internal fun LessonStudyScreen(
    topic: TopicContent,
    lesson: LessonContent,
    supportLanguage: SupportLanguage = SupportLanguage.Ukrainian,
    onBackClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {}
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    var phase by remember(lesson.id) { mutableStateOf(LessonPhase.Learn) }
    var currentIndex by remember(lesson.id) { mutableIntStateOf(0) }
    var quizIndex by remember(lesson.id) { mutableIntStateOf(0) }
    var selectedAnswer by remember(lesson.id) { mutableStateOf<String?>(null) }
    var submittedAnswer by remember(lesson.id) { mutableStateOf<String?>(null) }
    val learningItem = lesson.items[currentIndex]
    val quizItem = lesson.items[quizIndex]
    val isLastLearningCard = currentIndex == lesson.items.lastIndex
    val isLastQuizQuestion = quizIndex == lesson.items.lastIndex
    val progress = when (phase) {
        LessonPhase.Learn -> (currentIndex + 1f) / (lesson.items.size * 2f)
        LessonPhase.Practice -> (lesson.items.size + quizIndex + 1f) / (lesson.items.size * 2f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        colors.heroStart,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                    )
                )
            )
    ) {
        LessonGlow(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-56).dp, y = 16.dp)
                .size(180.dp),
            brush = Brush.radialGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0f)
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = spacing.screenHorizontal,
                    vertical = spacing.screenVertical
                )
        ) {
            LessonHeader(
                overline = topic.title,
                title = lesson.title,
                onBackClick = onBackClick
            )
            Spacer(modifier = Modifier.height(spacing.lg))

            Text(
                text = when (phase) {
                    LessonPhase.Learn -> "Learn ${currentIndex + 1} of ${lesson.items.size}"
                    LessonPhase.Practice -> "Practice ${quizIndex + 1} of ${lesson.items.size}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(spacing.sm))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(colors.progressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(spacing.xl))
            when (phase) {
                LessonPhase.Learn -> LessonWordCard(
                    item = learningItem,
                    supportLanguage = supportLanguage
                )
                LessonPhase.Practice -> ChooseTranslationCard(
                    lesson = lesson,
                    item = quizItem,
                    supportLanguage = supportLanguage,
                    selectedAnswer = selectedAnswer,
                    submittedAnswer = submittedAnswer,
                    onAnswerSelected = { answer ->
                        if (submittedAnswer == null) {
                            selectedAnswer = answer
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(spacing.lg))
            when (phase) {
                LessonPhase.Learn -> {
                    LessonExerciseCard(lesson = lesson)
                    Spacer(modifier = Modifier.height(spacing.xl))
                }
                LessonPhase.Practice -> {
                    if (submittedAnswer != null) {
                        AnswerFeedbackCard(
                            isCorrect = submittedAnswer == quizItem.translationForSelectedLanguage(supportLanguage),
                            correctAnswer = quizItem.translationForSelectedLanguage(supportLanguage)
                        )
                        Spacer(modifier = Modifier.height(spacing.xl))
                    } else {
                        Spacer(modifier = Modifier.height(spacing.xl))
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
                ) {
                    Button(
                        onClick = {
                            when (phase) {
                                LessonPhase.Learn -> {
                                    if (currentIndex > 0) currentIndex -= 1 else onBackClick()
                                }
                                LessonPhase.Practice -> {
                                    if (quizIndex > 0) {
                                        quizIndex -= 1
                                        selectedAnswer = null
                                        submittedAnswer = null
                                    } else {
                                        phase = LessonPhase.Learn
                                        currentIndex = lesson.items.lastIndex
                                        selectedAnswer = null
                                        submittedAnswer = null
                                    }
                                }
                            }
                        },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f)
                        ),
                        elevation = null
                    ) {
                        Text(text = "Back", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Button(
                    onClick = {
                        when (phase) {
                            LessonPhase.Learn -> {
                                if (isLastLearningCard) {
                                    phase = LessonPhase.Practice
                                    selectedAnswer = null
                                    submittedAnswer = null
                                } else {
                                    currentIndex += 1
                                }
                            }
                            LessonPhase.Practice -> {
                                if (submittedAnswer == null) {
                                    submittedAnswer = selectedAnswer
                                } else if (isLastQuizQuestion) {
                                    onCompleteClick()
                                } else {
                                    quizIndex += 1
                                    selectedAnswer = null
                                    submittedAnswer = null
                                }
                            }
                        }
                    },
                    enabled = when (phase) {
                        LessonPhase.Learn -> true
                        LessonPhase.Practice -> selectedAnswer != null || submittedAnswer != null
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                    )
                ) {
                    Text(
                        text = when (phase) {
                            LessonPhase.Learn -> if (isLastLearningCard) "Start practice" else "Next phrase"
                            LessonPhase.Practice -> when {
                                submittedAnswer == null -> "Check answer"
                                isLastQuizQuestion -> "Finish lesson"
                                else -> "Next question"
                            }
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChooseTranslationCard(
    lesson: LessonContent,
    item: LessonItemContent,
    supportLanguage: SupportLanguage,
    selectedAnswer: String?,
    submittedAnswer: String?,
    onAnswerSelected: (String) -> Unit
) {
    val spacing = MaterialTheme.appSpacing
    val correctAnswer = item.translationForSelectedLanguage(supportLanguage)
    val options = remember(lesson.id, item.id, supportLanguage) {
        buildTranslationOptions(lesson, item, supportLanguage)
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
    ) {
        Column(
            modifier = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            Text(
                text = "Choose the correct translation",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.polish,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                options.forEach { option ->
                    val isSelected = selectedAnswer == option
                    val isSubmitted = submittedAnswer != null
                    val isCorrect = option == correctAnswer
                    val containerColor = when {
                        isSubmitted && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                        isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.secondaryContainer
                        isSelected -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val borderColor = when {
                        isSubmitted && isCorrect -> MaterialTheme.colorScheme.primary
                        isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.secondary
                        isSelected -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isSubmitted) { onAnswerSelected(option) },
                        shape = MaterialTheme.shapes.medium,
                        color = containerColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(horizontal = spacing.lg, vertical = 18.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonWordCard(
    item: LessonItemContent,
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
    ) {
        Column(
            modifier = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            item.note?.let { note ->
                LessonPill(text = note)
            }

            Text(
                text = item.polish,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LessonTranslationLine(
                    label = supportLanguage.code,
                    value = item.translationForSelectedLanguage(supportLanguage)
                )
            }

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
            ) {
                Column(
                    modifier = Modifier.padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.examplePolish,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.exampleForSelectedLanguage(supportLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonTranslationLine(
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LessonPill(text = label)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f)
        )
    }
}

@Composable
private fun LessonExerciseCard(lesson: LessonContent) {
    val spacing = MaterialTheme.appSpacing

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
    ) {
        Column(
            modifier = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Text(
                text = "Lesson practice",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            lesson.exerciseTypes.forEach { exerciseType ->
                Text(
                    text = when (exerciseType) {
                        com.polish.thousand.content.ExerciseType.ChooseTranslation -> "Choose the correct translation"
                        com.polish.thousand.content.ExerciseType.ListenAndChoose -> "Listen and choose the phrase"
                        com.polish.thousand.content.ExerciseType.UnderstandInContext -> "Understand the phrase in context"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                )
            }
        }
    }
}

@Composable
private fun AnswerFeedbackCard(
    isCorrect: Boolean,
    correctAnswer: String
) {
    val spacing = MaterialTheme.appSpacing
    val containerColor = if (isCorrect) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f)
    }
    val title = if (isCorrect) "Correct" else "Not quite"
    val message = if (isCorrect) {
        "Good. Keep the phrase moving."
    } else {
        "The correct translation is: $correctAnswer"
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
            )
        }
    }
}

private fun buildTranslationOptions(
    lesson: LessonContent,
    item: LessonItemContent,
    supportLanguage: SupportLanguage
): List<String> {
    val correctAnswer = item.translationForSelectedLanguage(supportLanguage)
    val distractors = lesson.items
        .asSequence()
        .filterNot { it.id == item.id }
        .map { it.translationForSelectedLanguage(supportLanguage) }
        .distinct()
        .take(3)
        .toList()

    return buildList {
        add(correctAnswer)
        addAll(distractors)
    }.distinct()
}

@Preview
@Composable
private fun LessonStudyScreenPreview() {
    PolishThousandTheme {
        val topic = MvpSeedContent.topics.first()
        LessonStudyScreen(
            topic = topic,
            lesson = topic.lessons.first()
        )
    }
}
