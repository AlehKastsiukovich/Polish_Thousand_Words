package com.polish.thousand.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.polish.thousand.content.LearningPath
import com.polish.thousand.content.LearningTargetWords
import com.polish.thousand.content.LessonContent
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.appText
import com.polish.thousand.content.titleFor
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

@Composable
internal fun WelcomeScreen(
    supportLanguage: SupportLanguage = SupportLanguage.Ukrainian,
    learnedWords: Int = 0,
    nextLesson: LessonContent? = MvpSeedContent.lessons.firstOrNull(),
    completedLessonIds: Set<String> = emptySet(),
    dueReviewCount: Int = 0,
    quickReviewCount: Int = 0,
    onContinueClick: () -> Unit = {},
    onOpenDueReviewClick: () -> Unit = {},
    onOpenQuickReviewClick: () -> Unit = {},
    onOpenSettingsClick: () -> Unit = {}
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    val text = supportLanguage.appText
    val totalProgress = learnedWords.toFloat() / LearningTargetWords
    val milestone = LearningPath.nextMilestone(learnedWords)
    val remainingToMilestone = (milestone.wordCount - learnedWords).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        colors.heroStart,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.screenHorizontal)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(bottom = 94.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PolishThousand",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onOpenSettingsClick) {
                        Text(
                            text = text.settings,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.md))

                OverallProgressRing(
                    progress = totalProgress,
                    learnedWords = learnedWords,
                    supportLanguage = supportLanguage
                )

                Spacer(modifier = Modifier.height(spacing.xl))

                MilestoneCard(
                    learnedWords = learnedWords,
                    remainingWords = remainingToMilestone,
                    supportLanguage = supportLanguage
                )

                Spacer(modifier = Modifier.height(spacing.lg))

                if (dueReviewCount > 0) {
                    ReviewDueCard(
                        dueReviewCount = dueReviewCount,
                        supportLanguage = supportLanguage,
                        onClick = onOpenDueReviewClick
                    )

                    Spacer(modifier = Modifier.height(spacing.lg))
                }

                if (quickReviewCount > 0) {
                    QuickReviewCard(
                        quickReviewCount = quickReviewCount,
                        supportLanguage = supportLanguage,
                        onClick = onOpenQuickReviewClick
                    )

                    Spacer(modifier = Modifier.height(spacing.lg))
                }

                nextLesson?.let { lesson ->
                    NextLessonCard(
                        lesson = lesson,
                        lessonNumber = completedLessonIds.size + 1,
                        supportLanguage = supportLanguage
                    )
                }

                if (nextLesson == null) {
                    Text(
                        text = if (supportLanguage == SupportLanguage.Ukrainian) {
                            "Усі доступні уроки завершено. Нові слова вже готуються."
                        } else {
                            "Все доступные уроки завершены. Новые слова уже готовятся."
                        },
                        modifier = Modifier.padding(vertical = spacing.xl),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                    )
                }
            }

            if (nextLesson != null || dueReviewCount > 0) {
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(58.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = when {
                            nextLesson == null && dueReviewCount > 0 -> reviewOnlyActionLabel(supportLanguage)
                            learnedWords == 0 -> text.startLearning
                            else -> text.continueLearning
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun reviewOnlyActionLabel(supportLanguage: SupportLanguage): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> "Повторити слова"
    SupportLanguage.Russian -> "Повторить слова"
}

@Composable
private fun QuickReviewCard(
    quickReviewCount: Int,
    supportLanguage: SupportLanguage,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Швидке повторення"
                    } else {
                        "Быстрое повторение"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "$quickReviewCount ${wordCountLabel(quickReviewCount, supportLanguage)}"
                    } else {
                        "$quickReviewCount ${wordCountLabel(quickReviewCount, supportLanguage)}"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Помилки з останнього уроку"
                    } else {
                        "Ошибки из последнего урока"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
            TimeChip(
                text = reviewDurationLabel(quickReviewCount, supportLanguage)
            )
        }
    }
}

@Composable
private fun ReviewDueCard(
    dueReviewCount: Int,
    supportLanguage: SupportLanguage,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Повторення"
                    } else {
                        "Повторение"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "$dueReviewCount ${wordCountLabel(dueReviewCount, supportLanguage)}"
                    } else {
                        "$dueReviewCount ${wordCountLabel(dueReviewCount, supportLanguage)}"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = reviewQueueSubtitle(dueReviewCount, supportLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
            TimeChip(
                text = reviewDurationLabel(dueReviewCount.coerceAtMost(10), supportLanguage)
            )
        }
    }
}

private fun reviewQueueSubtitle(
    dueReviewCount: Int,
    supportLanguage: SupportLanguage
): String = when {
    dueReviewCount > 10 && supportLanguage == SupportLanguage.Ukrainian -> "10 слів у цьому підході"
    dueReviewCount > 10 -> "10 слов в этом подходе"
    supportLanguage == SupportLanguage.Ukrainian -> "Закріпити сьогодні"
    else -> "Закрепить сегодня"
}

private fun reviewDurationLabel(
    wordCount: Int,
    supportLanguage: SupportLanguage
): String {
    val minutes = ((wordCount + 2) / 3).coerceAtLeast(1)
    return if (supportLanguage == SupportLanguage.Ukrainian) "$minutes хв" else "$minutes мин"
}

@Composable
private fun TimeChip(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

private fun wordCountLabel(
    count: Int,
    supportLanguage: SupportLanguage
): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> if (count == 1) "слово" else "слів"
    SupportLanguage.Russian -> when {
        count % 10 == 1 && count % 100 != 11 -> "слово"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "слова"
        else -> "слов"
    }
}

@Composable
private fun OverallProgressRing(
    progress: Float,
    learnedWords: Int,
    supportLanguage: SupportLanguage
) {
    val colors = MaterialTheme.appColors
    Box(modifier = Modifier.size(196.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 15.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            drawArc(
                color = androidx.compose.ui.graphics.Color(0xFFEDE4DA),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            colors.progressStart,
                            colors.progressMiddle,
                            colors.progressEnd,
                            colors.progressStart
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = learnedWords.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (supportLanguage == SupportLanguage.Ukrainian) "з 1 000 слів" else "из 1 000 слов",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.56f)
            )
        }
    }
}

@Composable
private fun MilestoneCard(
    learnedWords: Int,
    remainingWords: Int,
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing
    val milestone = LearningPath.nextMilestone(learnedWords)
    val progress = (learnedWords.toFloat() / LearningTargetWords).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (supportLanguage == SupportLanguage.Ukrainian) "Наступна відмітка" else "Следующая отметка",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                    )
                    Text(
                        text = "${milestone.wordCount} · ${milestone.titleFor(supportLanguage)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (supportLanguage == SupportLanguage.Ukrainian) "ще $remainingWords" else "ещё $remainingWords",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .background(
                        color = MaterialTheme.appColors.progressTrack,
                        shape = MaterialTheme.shapes.extraLarge
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(9.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.appColors.progressStart,
                                    MaterialTheme.appColors.progressMiddle,
                                    MaterialTheme.appColors.progressEnd
                                )
                            ),
                            shape = MaterialTheme.shapes.extraLarge
                        )
                )
            }

            MilestoneScale(
                learnedWords = learnedWords
            )
        }
    }
}

@Composable
private fun MilestoneScale(
    learnedWords: Int
) {
    val scalePoints = listOf(0) + LearningPath.milestones.map { it.wordCount }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
    ) {
        scalePoints.forEach { point ->
            val position = maxWidth * (point.toFloat() / LearningTargetWords)
            val isStart = point == 0
            val isCompleted = learnedWords >= point && !isStart
            val isActive = if (isStart) learnedWords < LearningPath.milestones.first().wordCount else isCompleted
            Column(
                modifier = Modifier.offset(
                    x = when (point) {
                        0 -> 0.dp
                        LearningTargetWords -> position - 32.dp
                        else -> position - 16.dp
                    }
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = point.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
                    }
                )
            }
        }
    }
}

@Composable
private fun NextLessonCard(
    lesson: LessonContent,
    lessonNumber: Int,
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Урок $lessonNumber · наступні ${lesson.items.size} слів"
                    } else {
                        "Урок $lessonNumber · следующие ${lesson.items.size} слов"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                )
                Text(
                    text = lesson.titleFor(supportLanguage),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = lesson.items.take(3).joinToString(" · ") { it.polish },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${lesson.estimatedMinutes} ${if (supportLanguage == SupportLanguage.Ukrainian) "хв" else "мин"}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    PolishThousandTheme {
        WelcomeScreen(learnedWords = 20)
    }
}
