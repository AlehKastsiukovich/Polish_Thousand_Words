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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polish.thousand.content.ActivityOverview
import com.polish.thousand.content.LearningPath
import com.polish.thousand.content.LearningActivity
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
    activityOverview: ActivityOverview = LearningActivity.overview(
        activeDays = emptySet(),
        todayEpochDay = 0
    ),
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
                    Spacer(modifier = Modifier.size(44.dp))
                    SettingsActionButton(onClick = onOpenSettingsClick)
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

                ActivityCard(
                    activityOverview = activityOverview,
                    supportLanguage = supportLanguage
                )

                Spacer(modifier = Modifier.height(spacing.lg))

                if (dueReviewCount > 0) {
                    ReviewDueCard(
                        dueReviewCount = dueReviewCount,
                        supportLanguage = supportLanguage,
                        onClick = onOpenDueReviewClick,
                        tone = HomeCardTone.Review
                    )

                    Spacer(modifier = Modifier.height(spacing.lg))
                }

                if (quickReviewCount > 0) {
                    QuickReviewCard(
                        quickReviewCount = quickReviewCount,
                        supportLanguage = supportLanguage,
                        onClick = onOpenQuickReviewClick,
                        tone = HomeCardTone.QuickReview
                    )

                    Spacer(modifier = Modifier.height(spacing.lg))
                }

                nextLesson?.let { lesson ->
                    NextLessonCard(
                        lesson = lesson,
                        lessonNumber = completedLessonIds.size + 1,
                        supportLanguage = supportLanguage,
                        tone = HomeCardTone.NextLesson
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
private fun ActivityCard(
    activityOverview: ActivityOverview,
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing
    val streakLabel = activityStreakLabel(activityOverview.streakDays, supportLanguage)
    val historyLabel = activityHistoryLabel(
        activeDays = activityOverview.activeDaysInWindow,
        supportLanguage = supportLanguage
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = Color(0xFFFCFBF8)
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            HomeCardAccent(tone = HomeCardTone.Activity)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (supportLanguage == SupportLanguage.Ukrainian) "Ритм" else "Ритм",
                        style = MaterialTheme.typography.labelSmall,
                        color = HomeCardTone.Activity.titleColor()
                    )
                    Text(
                        text = streakLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = historyLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = HomeCardTone.Activity.chipContainerColor()
                ) {
                    Text(
                        text = "${activityOverview.activeDaysInWindow}/14",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = HomeCardTone.Activity.chipContentColor()
                    )
                }
            }

            ActivityGrid(activityOverview = activityOverview)
        }
    }
}

@Composable
private fun ActivityGrid(activityOverview: ActivityOverview) {
    val days = activityOverview.recentDays
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.chunked(7).forEachIndexed { rowIndex, week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                week.forEachIndexed { index, day ->
                    val globalIndex = rowIndex * 7 + index
                    val isRecent = globalIndex >= days.lastIndex - 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .background(
                                color = when {
                                    day.isActive && isRecent -> Color(0xFF78C8E8)
                                    day.isActive -> Color(0xFF6BC5B7)
                                    else -> Color(0xFFF1ECE5)
                                },
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
        }
    }
}

private fun activityStreakLabel(
    streakDays: Int,
    supportLanguage: SupportLanguage
): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> when {
        streakDays <= 0 -> "Почніть новий ритм"
        streakDays % 10 == 1 && streakDays % 100 != 11 -> "$streakDays день поспіль"
        streakDays % 10 in 2..4 && streakDays % 100 !in 12..14 -> "$streakDays дні поспіль"
        else -> "$streakDays днів поспіль"
    }
    SupportLanguage.Russian -> when {
        streakDays <= 0 -> "Начните новый ритм"
        streakDays % 10 == 1 && streakDays % 100 != 11 -> "$streakDays день подряд"
        streakDays % 10 in 2..4 && streakDays % 100 !in 12..14 -> "$streakDays дня подряд"
        else -> "$streakDays дней подряд"
    }
}

private fun activityHistoryLabel(
    activeDays: Int,
    supportLanguage: SupportLanguage
): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> when {
        activeDays == 1 -> "1 активний день за останні 14"
        activeDays % 10 in 2..4 && activeDays % 100 !in 12..14 -> "$activeDays активні дні за останні 14"
        else -> "$activeDays активних днів за останні 14"
    }
    SupportLanguage.Russian -> when {
        activeDays == 1 -> "1 активный день за последние 14"
        activeDays % 10 in 2..4 && activeDays % 100 !in 12..14 -> "$activeDays активных дня за последние 14"
        else -> "$activeDays активных дней за последние 14"
    }
}

@Composable
private fun QuickReviewCard(
    quickReviewCount: Int,
    supportLanguage: SupportLanguage,
    onClick: () -> Unit,
    tone: HomeCardTone
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = tone.containerColor(),
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
                HomeCardAccent(tone = tone)
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Швидке повторення"
                    } else {
                        "Быстрое повторение"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = tone.titleColor()
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
                text = reviewDurationLabel(quickReviewCount, supportLanguage),
                tone = tone
            )
        }
    }
}

@Composable
private fun ReviewDueCard(
    dueReviewCount: Int,
    supportLanguage: SupportLanguage,
    onClick: () -> Unit,
    tone: HomeCardTone
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = tone.containerColor(),
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
                HomeCardAccent(tone = tone)
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Повторення"
                    } else {
                        "Повторение"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = tone.titleColor()
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
                text = reviewDurationLabel(dueReviewCount.coerceAtMost(10), supportLanguage),
                tone = tone
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
private fun TimeChip(
    text: String,
    tone: HomeCardTone
) {
    Surface(
        shape = CircleShape,
        color = tone.chipContainerColor()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = tone.chipContentColor()
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
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 46.sp,
                    lineHeight = 50.sp
                ),
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
    val trackColor = MaterialTheme.appColors.progressTrack
    val progressBrush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.appColors.progressStart,
            MaterialTheme.appColors.progressMiddle,
            MaterialTheme.appColors.progressEnd
        )
    )

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

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
            ) {
                val strokeWidth = size.height
                val centerY = size.height / 2f
                val start = Offset(strokeWidth / 2f, centerY)
                val end = Offset(size.width - strokeWidth / 2f, centerY)

                drawLine(
                    color = trackColor,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )

                if (progress > 0f) {
                    val progressEnd = Offset(
                        x = start.x + ((end.x - start.x) * progress),
                        y = centerY
                    )
                    drawLine(
                        brush = progressBrush,
                        start = start,
                        end = progressEnd,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
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
    supportLanguage: SupportLanguage,
    tone: HomeCardTone
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = tone.containerColor()
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
                HomeCardAccent(tone = tone)
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Урок $lessonNumber · наступні ${lesson.items.size} слів"
                    } else {
                        "Урок $lessonNumber · следующие ${lesson.items.size} слов"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = tone.titleColor()
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
                color = tone.chipContainerColor()
            ) {
                Text(
                    text = "${lesson.estimatedMinutes} ${if (supportLanguage == SupportLanguage.Ukrainian) "хв" else "мин"}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = tone.chipContentColor()
                )
            }
        }
    }
}

@Composable
private fun SettingsActionButton(onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp)
        ) {
            SettingsGlyph()
        }
    }
}

@Composable
private fun SettingsGlyph() {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = 1.8.dp.toPx()
        val left = size.width * 0.14f
        val right = size.width * 0.86f
        val top = size.height * 0.22f
        val middle = size.height * 0.5f
        val bottom = size.height * 0.78f
        drawLine(color, Offset(left, top), Offset(right, top), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.28f, middle), Offset(size.width * 0.72f, middle), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.4f, bottom), Offset(size.width * 0.6f, bottom), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun HomeCardAccent(tone: HomeCardTone) {
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(6.dp)
            .background(color = tone.accentColor(), shape = CircleShape)
    )
}

private enum class HomeCardTone {
    Review,
    QuickReview,
    NextLesson,
    Activity
}

@Composable
private fun HomeCardTone.containerColor(): Color = when (this) {
    HomeCardTone.Review -> Color(0xFFF5FBF8)
    HomeCardTone.QuickReview -> Color(0xFFF4FAFD)
    HomeCardTone.NextLesson -> Color(0xFFFFFCF8)
    HomeCardTone.Activity -> Color(0xFFFCFBF8)
}

@Composable
private fun HomeCardTone.accentColor(): Color = when (this) {
    HomeCardTone.Review -> Color(0xFF5EC0B0)
    HomeCardTone.QuickReview -> Color(0xFF69A4E4)
    HomeCardTone.NextLesson -> Color(0xFFE1C9A5)
    HomeCardTone.Activity -> Color(0xFFB99AF1)
}

@Composable
private fun HomeCardTone.titleColor(): Color = when (this) {
    HomeCardTone.Review -> Color(0xFF2A7B71)
    HomeCardTone.QuickReview -> Color(0xFF2B7D9E)
    HomeCardTone.NextLesson -> Color(0xFF8D7A63)
    HomeCardTone.Activity -> Color(0xFF6D54A3)
}

@Composable
private fun HomeCardTone.chipContainerColor(): Color = when (this) {
    HomeCardTone.Review -> Color(0xFFDFF1EB)
    HomeCardTone.QuickReview -> Color(0xFFE3F0FB)
    HomeCardTone.NextLesson -> Color(0xFFF4ECE2)
    HomeCardTone.Activity -> Color(0xFFF1E9FF)
}

@Composable
private fun HomeCardTone.chipContentColor(): Color = when (this) {
    HomeCardTone.Review -> Color(0xFF255C54)
    HomeCardTone.QuickReview -> Color(0xFF27587B)
    HomeCardTone.NextLesson -> Color(0xFF5C534A)
    HomeCardTone.Activity -> Color(0xFF5F469C)
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    PolishThousandTheme {
        WelcomeScreen(
            learnedWords = 20,
            activityOverview = LearningActivity.overview(
                activeDays = setOf(4L, 5L, 7L, 8L, 10L, 12L, 13L),
                todayEpochDay = 13L
            )
        )
    }
}
