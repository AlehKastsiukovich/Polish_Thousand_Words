package com.polish.thousand.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.polish.thousand.content.ActivityOverview
import com.polish.thousand.content.LearningActivity
import com.polish.thousand.content.LearningMilestone
import com.polish.thousand.content.LearningPath
import com.polish.thousand.content.LearningTargetWords
import com.polish.thousand.content.LessonContent
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.titleFor
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
    onOpenFreeReviewClick: () -> Unit = {},
    onOpenSettingsClick: () -> Unit = {}
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    val milestone = LearningPath.nextMilestone(learnedWords)
    val remainingToMilestone = (milestone.wordCount - learnedWords).coerceAtLeast(0)
    var showMilestones by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal)
                .padding(bottom = spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsActionButton(onClick = onOpenSettingsClick)
            }

            Spacer(modifier = Modifier.height(spacing.sm))

            AchievementProgress(
                learnedWords = learnedWords,
                milestone = milestone,
                remainingWords = remainingToMilestone,
                supportLanguage = supportLanguage,
                onClick = { showMilestones = true }
            )

            Spacer(modifier = Modifier.height(spacing.xxl))

            nextLesson?.let { lesson ->
                NextLessonCard(
                    lesson = lesson,
                    lessonNumber = completedLessonIds.size + 1,
                    supportLanguage = supportLanguage,
                    onClick = onContinueClick
                )
                Spacer(modifier = Modifier.height(spacing.md))
            }

            if (dueReviewCount > 0 || quickReviewCount > 0) {
                UnifiedReviewCard(
                    dueReviewCount = dueReviewCount,
                    quickReviewCount = quickReviewCount,
                    supportLanguage = supportLanguage,
                    onClick = if (dueReviewCount > 0) {
                        onOpenDueReviewClick
                    } else {
                        onOpenQuickReviewClick
                    }
                )
                Spacer(modifier = Modifier.height(spacing.xl))
            } else if (learnedWords >= LearningTargetWords) {
                ScheduledReviewEmptyState(supportLanguage = supportLanguage)
                Spacer(modifier = Modifier.height(spacing.xl))
            }

            if (learnedWords >= LearningTargetWords) {
                FreePracticeCard(
                    supportLanguage = supportLanguage,
                    onClick = onOpenFreeReviewClick
                )
                Spacer(modifier = Modifier.height(spacing.xl))
            }

            Spacer(modifier = Modifier.height(spacing.md))

            CompactRhythm(
                activityOverview = activityOverview,
                supportLanguage = supportLanguage
            )
        }

        if (showMilestones) {
            MilestonePathDialog(
                onDismissRequest = { showMilestones = false },
                learnedWords = learnedWords,
                supportLanguage = supportLanguage
            )
        }
    }
}

@Composable
private fun AchievementProgress(
    learnedWords: Int,
    milestone: LearningMilestone,
    remainingWords: Int,
    supportLanguage: SupportLanguage,
    onClick: () -> Unit
) {
    val ringSize = 228.dp
    val ringStroke = 15.dp
    val backgroundColor = MaterialTheme.colorScheme.background
    val trackColor = MaterialTheme.appColors.progressTrack
    val milestones = LearningPath.milestones
    val boundaries = listOf(0) + milestones.map { it.wordCount }
    val segmentColors = milestoneColors()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(ringSize)
                .clip(CircleShape)
                .clickable(
                    role = Role.Button,
                    onClickLabel = milestonePathLabel(supportLanguage),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = ringStroke.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val diameter = radius * 2f
                val topLeft = Offset(
                    x = (size.width - diameter) / 2f,
                    y = (size.height - diameter) / 2f
                )
                val center = Offset(size.width / 2f, size.height / 2f)
                val gapDegrees = 2.4f

                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                segmentColors.forEachIndexed { index, color ->
                    val start = boundaries[index].toFloat() / LearningTargetWords
                    val end = boundaries[index + 1].toFloat() / LearningTargetWords
                    drawArc(
                        color = color.copy(alpha = 0.22f),
                        startAngle = -90f + (360f * start) + gapDegrees / 2f,
                        sweepAngle = (360f * (end - start) - gapDegrees).coerceAtLeast(0f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }

                segmentColors.forEachIndexed { index, color ->
                    val stageStart = boundaries[index]
                    val stageEnd = boundaries[index + 1]
                    val stageLength = stageEnd - stageStart
                    val completedInStage = (learnedWords - stageStart)
                        .coerceIn(0, stageLength)
                    if (completedInStage > 0) {
                        val start = stageStart.toFloat() / LearningTargetWords
                        val fullSweep = (360f * stageLength / LearningTargetWords - gapDegrees)
                            .coerceAtLeast(0f)
                        drawArc(
                            color = color,
                            startAngle = -90f + (360f * start) + gapDegrees / 2f,
                            sweepAngle = fullSweep * completedInStage / stageLength,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                milestones.forEachIndexed { index, item ->
                    val angleDegrees = -90f + 360f * (item.wordCount.toFloat() / LearningTargetWords)
                    val angleRadians = angleDegrees.toDouble() * PI / 180.0
                    val markerCenter = Offset(
                        x = center.x + radius * cos(angleRadians).toFloat(),
                        y = center.y + radius * sin(angleRadians).toFloat()
                    )
                    drawCircle(
                        color = backgroundColor,
                        radius = 8.dp.toPx(),
                        center = markerCenter
                    )
                    drawCircle(
                        color = segmentColors[index].copy(
                            alpha = if (learnedWords >= item.wordCount) 1f else 0.58f
                        ),
                        radius = 5.dp.toPx(),
                        center = markerCenter
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = learnedWords.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 50.sp,
                        lineHeight = 54.sp
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "з 1 000 слів"
                    } else {
                        "из 1 000 слов"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.56f)
                )
                Surface(
                    modifier = Modifier.padding(top = 12.dp),
                    shape = CircleShape,
                    color = Color(0xFFF2F8F5)
                ) {
                    Text(
                        text = progressGoalLabel(
                            milestone = milestone,
                            remainingWords = remainingWords,
                            supportLanguage = supportLanguage
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF255C54)
                    )
                }
            }
        }

    }
}

@Composable
private fun NextLessonCard(
    lesson: LessonContent,
    lessonNumber: Int,
    supportLanguage: SupportLanguage,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 8.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "НАСТУПНІ ${lesson.items.size} СЛІВ"
                    } else {
                        "СЛЕДУЮЩИЕ ${lesson.items.size} СЛОВ"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                )
                Text(
                    text = lessonRangeLabel(lessonNumber, lesson.items.size),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 25.sp),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lesson.items.take(3).joinToString(" · ") { it.polish },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)
            ) {
                ChevronRightGlyph(
                    modifier = Modifier.padding(14.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun UnifiedReviewCard(
    dueReviewCount: Int,
    quickReviewCount: Int,
    supportLanguage: SupportLanguage,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.appSpacing
    val isScheduledReview = dueReviewCount > 0
    val count = if (isScheduledReview) dueReviewCount else quickReviewCount
    val approachSize = count.coerceAtMost(10)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFFD6ECE5)),
        shadowElevation = 3.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(0xFFDFF1EB)
            ) {
                RepeatGlyph(
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = if (isScheduledReview) {
                        if (supportLanguage == SupportLanguage.Ukrainian) "Повторити слова" else "Повторить слова"
                    } else {
                        if (supportLanguage == SupportLanguage.Ukrainian) "Повторити помилки" else "Повторить ошибки"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = reviewCardSubtitle(
                        count = count,
                        approachSize = approachSize,
                        isScheduledReview = isScheduledReview,
                        supportLanguage = supportLanguage
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFFE9F5F1)
            ) {
                Text(
                    text = reviewDurationLabel(approachSize, supportLanguage),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF255C54)
                )
            }
            ChevronRightGlyph(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ScheduledReviewEmptyState(supportLanguage: SupportLanguage) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = Color(0xFFF8FCFA),
        border = BorderStroke(1.dp, Color(0xFFDDEAE5))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = Color(0xFFE5F3ED)
            ) {
                RepeatGlyph(
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Повторення за розкладом"
                    } else {
                        "Повторения по расписанию"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Слова з'являться тут, коли настане час їх повторити."
                    } else {
                        "Слова появятся здесь, когда придёт время их повторить."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FreePracticeCard(
    supportLanguage: SupportLanguage,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = Color(0xFFE8F1FB)
            ) {
                Text(
                    text = "1K",
                    modifier = Modifier.padding(top = 13.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF367FC4)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) "Усі 1 000 слів" else "Все 1 000 слов",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) "Вільна практика · 10 слів" else "Свободная практика · 10 слов",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ChevronRightGlyph(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CompactRhythm(
    activityOverview: ActivityOverview,
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing
    val days = activityOverview.recentDays
    val todayEpochDay = activityOverview.todayEpochDay

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
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Ритм тижня"
                    } else {
                        "Ритм недели"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = weeklyRhythmStatusLabel(
                        activeDays = activityOverview.activeDaysInWindow,
                        totalDays = days.size,
                        supportLanguage = supportLanguage
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6D54A3)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                days.forEach { day ->
                    val isToday = day.epochDay == todayEpochDay
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = CircleShape,
                        color = when {
                            day.isActive -> Color(0xFF8B68D8)
                            isToday -> MaterialTheme.colorScheme.surface
                            else -> Color(0xFFF1ECE6)
                        },
                        border = if (isToday && !day.isActive) {
                            BorderStroke(2.dp, Color(0xFFA986EB))
                        } else {
                            null
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = weekdayInitial(day.epochDay, supportLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    day.isActive -> Color.White
                                    isToday -> Color(0xFF7655BB)
                                    else -> Color(0xFF97948F)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestonePathDialog(
    onDismissRequest: () -> Unit,
    learnedWords: Int,
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing
    val nextMilestone = LearningPath.nextMilestone(learnedWords)
    val colors = milestoneColors()
    val learnedLabel = when (supportLanguage) {
        SupportLanguage.Ukrainian -> "$learnedWords слів вивчено"
        SupportLanguage.Russian -> "$learnedWords слов изучено"
    }
    val nextGoalLabel = when {
        learnedWords >= LearningTargetWords && supportLanguage == SupportLanguage.Ukrainian -> "Усі етапи пройдено"
        learnedWords >= LearningTargetWords -> "Все этапы пройдены"
        supportLanguage == SupportLanguage.Ukrainian -> "Наступна ціль: ${milestoneWordCountLabel(nextMilestone.wordCount)} слів"
        else -> "Следующая цель: ${milestoneWordCountLabel(nextMilestone.wordCount)} слов"
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier.padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = if (supportLanguage == SupportLanguage.Ukrainian) "ВАШ ПРОГРЕС" else "ВАШ ПРОГРЕСС",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (supportLanguage == SupportLanguage.Ukrainian) "Шлях до 1 000" else "Путь к 1 000",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = learnedLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                        )
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f)
                ) {
                    Column(modifier = Modifier.padding(vertical = spacing.sm)) {
                        LearningPath.milestones.forEachIndexed { index, milestone ->
                            MilestoneDialogRow(
                                milestone = milestone,
                                color = colors[index],
                                isCompleted = learnedWords >= milestone.wordCount,
                                isCurrent = milestone == nextMilestone && learnedWords < milestone.wordCount,
                                isLast = index == LearningPath.milestones.lastIndex,
                                supportLanguage = supportLanguage
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nextGoalLabel,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f)
                    )
                    Surface(
                        onClick = onDismissRequest,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(
                            text = if (supportLanguage == SupportLanguage.Ukrainian) "Готово" else "Готово",
                            modifier = Modifier.padding(horizontal = spacing.md, vertical = 9.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneDialogRow(
    milestone: LearningMilestone,
    color: Color,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing
    val titleColor = MaterialTheme.colorScheme.onSurface.copy(
        alpha = if (isCompleted || isCurrent) 0.92f else 0.58f
    )

    Row(
        modifier = Modifier.padding(horizontal = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(30.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(1.dp)
                        .height(25.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
            Surface(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = 2.dp,
                    color = if (isCompleted || isCurrent) color else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 10.dp else 7.dp)
                                .background(
                                    color = if (isCurrent) color else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            color = if (isCurrent) color.copy(alpha = 0.12f) else Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(horizontal = spacing.sm, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        Text(
                            text = milestoneWordCountLabel(milestone.wordCount),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = milestone.titleFor(supportLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCompleted || isCurrent) FontWeight.Medium else FontWeight.Normal,
                            color = titleColor
                        )
                    }
                    if (isCompleted || isCurrent) {
                        Text(
                            text = when {
                                isCompleted && supportLanguage == SupportLanguage.Ukrainian -> "Пройдено"
                                isCompleted -> "Пройдено"
                                supportLanguage == SupportLanguage.Ukrainian -> "Поточна ціль"
                                else -> "Текущая цель"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = color.copy(alpha = 0.9f)
                        )
                    }
                }
                if (isCurrent) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = if (supportLanguage == SupportLanguage.Ukrainian) "ЗАРАЗ" else "СЕЙЧАС",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

private fun milestoneWordCountLabel(wordCount: Int): String =
    if (wordCount == LearningTargetWords) "1 000" else wordCount.toString()

private fun progressGoalLabel(
    milestone: LearningMilestone,
    remainingWords: Int,
    supportLanguage: SupportLanguage
): String = when {
    remainingWords == 0 && supportLanguage == SupportLanguage.Ukrainian -> "Ціль 1 000 досягнута"
    remainingWords == 0 -> "Цель 1 000 достигнута"
    supportLanguage == SupportLanguage.Ukrainian ->
        "До цілі ${milestone.wordCount} — $remainingWords ${wordForm(remainingWords, supportLanguage)}"
    else -> "До цели ${milestone.wordCount} — $remainingWords ${wordForm(remainingWords, supportLanguage)}"
}

private fun milestonePathLabel(supportLanguage: SupportLanguage): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> "Відкрити шлях до 1 000 слів"
    SupportLanguage.Russian -> "Открыть путь к 1 000 слов"
}

private fun lessonRangeLabel(lessonNumber: Int, wordCount: Int): String {
    val firstWord = (lessonNumber - 1) * 10 + 1
    val lastWord = firstWord + wordCount - 1
    return "Слова $firstWord–$lastWord"
}

private fun reviewCardSubtitle(
    count: Int,
    approachSize: Int,
    isScheduledReview: Boolean,
    supportLanguage: SupportLanguage
): String = when {
    isScheduledReview && supportLanguage == SupportLanguage.Ukrainian -> "$count чекають · почнемо з $approachSize"
    isScheduledReview -> "$count ждут · начнем с $approachSize"
    supportLanguage == SupportLanguage.Ukrainian -> "$count помилок · короткий підхід"
    else -> "$count ошибок · короткий подход"
}

private fun reviewDurationLabel(
    wordCount: Int,
    supportLanguage: SupportLanguage
): String {
    val minutes = ((wordCount + 2) / 3).coerceAtLeast(1)
    return if (supportLanguage == SupportLanguage.Ukrainian) "$minutes хв" else "$minutes мин"
}

private fun weeklyRhythmStatusLabel(
    activeDays: Int,
    totalDays: Int,
    supportLanguage: SupportLanguage
): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> if (activeDays == 0) "Почніть сьогодні" else "$activeDays з $totalDays днів"
    SupportLanguage.Russian -> if (activeDays == 0) "Начните сегодня" else "$activeDays из $totalDays дней"
}

private fun weekdayInitial(
    epochDay: Long,
    supportLanguage: SupportLanguage
): String {
    val weekdayIndex = (((epochDay + 3) % 7 + 7) % 7).toInt()
    return when (supportLanguage) {
        SupportLanguage.Ukrainian -> listOf("П", "В", "С", "Ч", "П", "С", "Н")[weekdayIndex]
        SupportLanguage.Russian -> listOf("П", "В", "С", "Ч", "П", "С", "В")[weekdayIndex]
    }
}

private fun wordForm(
    count: Int,
    supportLanguage: SupportLanguage
): String {
    val lastTwoDigits = count % 100
    val lastDigit = count % 10
    return when (supportLanguage) {
        SupportLanguage.Ukrainian -> when {
            lastTwoDigits in 11..14 -> "слів"
            lastDigit == 1 -> "слово"
            lastDigit in 2..4 -> "слова"
            else -> "слів"
        }
        SupportLanguage.Russian -> when {
            lastTwoDigits in 11..14 -> "слов"
            lastDigit == 1 -> "слово"
            lastDigit in 2..4 -> "слова"
            else -> "слов"
        }
    }
}

@Composable
private fun milestoneColors(): List<Color> {
    val colors = MaterialTheme.appColors
    return listOf(
        colors.milestoneWarmup,
        colors.milestoneBase,
        colors.milestoneConfidence,
        colors.milestoneMomentum,
        colors.milestoneMastery
    )
}

@Composable
private fun ChevronRightGlyph(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.38f, size.height * 0.22f),
            end = Offset(size.width * 0.66f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.66f, size.height * 0.5f),
            end = Offset(size.width * 0.38f, size.height * 0.78f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun RepeatGlyph(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        val arrowSize = 4.dp.toPx()
        drawArc(
            color = color,
            startAngle = 205f,
            sweepAngle = 220f,
            useCenter = false,
            topLeft = Offset(size.width * 0.14f, size.height * 0.14f),
            size = Size(size.width * 0.72f, size.height * 0.72f),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.5f),
            end = Offset(size.width * 0.18f + arrowSize, size.height * 0.5f - arrowSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.5f),
            end = Offset(size.width * 0.18f - arrowSize, size.height * 0.5f - arrowSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SettingsActionButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp)
    ) {
        SettingsGlyph()
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

@Preview
@Composable
private fun WelcomeScreenPreview() {
    PolishThousandTheme {
        WelcomeScreen(
            supportLanguage = SupportLanguage.Russian,
            learnedWords = 1000,
            activityOverview = LearningActivity.overview(
                activeDays = emptySet(),
                todayEpochDay = 13L
            ),
            dueReviewCount = 47,
            quickReviewCount = 8
        )
    }
}
