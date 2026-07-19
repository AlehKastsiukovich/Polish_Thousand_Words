package com.polish.thousand.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.path
import com.polish.thousand.content.LessonContent
import com.polish.thousand.content.LessonItemContent
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.ReviewQuality
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.appText
import com.polish.thousand.content.titleFor
import com.polish.thousand.content.translationForSelectedLanguage
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

@Composable
internal fun LessonStudyScreen(
    state: LessonUiState,
    onBackClick: () -> Unit = {},
    onIntent: (LessonIntent) -> Unit = {},
    onPlayWordClick: (LessonItemContent) -> Unit = {},
    onPlayExampleClick: (LessonItemContent, Int) -> Unit = { _, _ -> }
) {
    val lesson = state.lesson ?: return
    val reviewItems = state.reviewItems
    val reviewOnly = state.reviewOnly
    val supportLanguage = state.supportLanguage
    val phase = state.phase
    val reviewIndex = state.reviewIndex
    val reviewQuestionMode = state.reviewQuestionMode
    val currentIndex = state.learnIndex
    val quizIndex = state.practiceIndex
    val practiceQuestionMode = state.practiceQuestionMode
    val selectedAnswer = state.selectedAnswer
    val submittedAnswer = state.submittedAnswer
    val isReviewAnswerVisible = state.isReviewAnswerVisible
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    val text = supportLanguage.appText

    val reviewItem = reviewItems.getOrNull(reviewIndex)
    val learningItem = lesson.items[currentIndex]
    val quizItem = lesson.items[quizIndex]
    val isLastLearningCard = currentIndex == lesson.items.lastIndex
    val isLastQuizQuestion = quizIndex == lesson.items.lastIndex
    val bottomBarPadding = if (phase == LessonPhase.Review) 210.dp else 148.dp
    val totalSteps = if (reviewOnly) {
        reviewItems.size.coerceAtLeast(1)
    } else {
        reviewItems.size + lesson.items.size * 2
    }
    val progress = when (phase) {
        LessonPhase.Review -> (reviewIndex + 1f) / totalSteps
        LessonPhase.Learn -> (reviewItems.size + currentIndex + 1f) / totalSteps
        LessonPhase.Practice -> (reviewItems.size + lesson.items.size + quizIndex + 1f) / totalSteps
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        colors.heroStart,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                    )
                )
            )
    ) {
        LessonGlow(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = (-12).dp)
                .size(180.dp),
            brush = Brush.radialGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0f)
                )
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = spacing.screenHorizontal,
                    vertical = spacing.screenVertical
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomBarPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.lg)
            ) {
                AppTopBar(
                    title = when (phase) {
                        LessonPhase.Review -> reviewTitle(supportLanguage)
                        else -> lesson.titleFor(supportLanguage)
                    },
                    onBackClick = onBackClick
                )

                Text(
                    text = when (phase) {
                        LessonPhase.Review -> reviewProgressLabel(
                            supportLanguage = supportLanguage,
                            current = reviewIndex + 1,
                            total = reviewItems.size
                        )
                        LessonPhase.Learn -> "${text.learnPhasePrefix} ${currentIndex + 1} / ${lesson.items.size}"
                        LessonPhase.Practice -> "${text.practicePhasePrefix} ${quizIndex + 1} / ${lesson.items.size}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(
                            color = colors.progressTrack,
                            shape = MaterialTheme.shapes.extraLarge
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(10.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        colors.progressStart,
                                        colors.progressMiddle,
                                        colors.progressEnd
                                    )
                                ),
                                shape = MaterialTheme.shapes.extraLarge
                            )
                    )
                }

                when (phase) {
                    LessonPhase.Review -> reviewItem?.let { item ->
                        LessonReviewCard(
                            item = item,
                            supportLanguage = supportLanguage,
                            questionMode = reviewQuestionMode,
                            answerVisible = isReviewAnswerVisible,
                            onPlayWordClick = { onPlayWordClick(item) },
                            onPlayExampleClick = { index -> onPlayExampleClick(item, index) }
                        )
                    }
                    LessonPhase.Learn -> LessonLearnCard(
                        item = learningItem,
                        supportLanguage = supportLanguage,
                        onPlayWordClick = { onPlayWordClick(learningItem) },
                        onPlayExampleClick = { index -> onPlayExampleClick(learningItem, index) }
                    )
                    LessonPhase.Practice -> LessonPracticeCard(
                        lesson = lesson,
                        item = quizItem,
                        supportLanguage = supportLanguage,
                        questionMode = practiceQuestionMode,
                        selectedAnswer = selectedAnswer,
                        submittedAnswer = submittedAnswer,
                        onPlayWordClick = { onPlayWordClick(quizItem) },
                        onAnswerSelected = { answer ->
                            onIntent(LessonIntent.SelectAnswer(answer))
                        }
                    )
                }

            }

            if (phase == LessonPhase.Review) {
                reviewItem?.let { item ->
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        if (isReviewAnswerVisible) {
                            Button(
                                onClick = {
                                    onIntent(LessonIntent.SubmitReviewAnswer(ReviewQuality.Know))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = reviewAnswerLabel(supportLanguage, ReviewQuality.Know),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                            ) {
                                ReviewAnswerButton(
                                    text = reviewAnswerLabel(supportLanguage, ReviewQuality.Almost),
                                    quality = ReviewQuality.Almost,
                                    modifier = Modifier.weight(1f),
                                    onAnswer = { quality ->
                                        onIntent(LessonIntent.SubmitReviewAnswer(quality))
                                    }
                                )
                                ReviewAnswerButton(
                                    text = reviewAnswerLabel(supportLanguage, ReviewQuality.Forgot),
                                    quality = ReviewQuality.Forgot,
                                    modifier = Modifier.weight(1f),
                                    onAnswer = { quality ->
                                        onIntent(LessonIntent.SubmitReviewAnswer(quality))
                                    }
                                )
                            }
                        } else {
                            Button(
                                onClick = { onIntent(LessonIntent.RevealReviewAnswer) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = reviewRevealLabel(supportLanguage),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
                    ) {
                        Button(
                            onClick = {
                                if (phase == LessonPhase.Learn && currentIndex == 0) onBackClick()
                                else onIntent(LessonIntent.Previous)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = null
                        ) {
                            Text(
                                text = lessonPreviousLabel(supportLanguage),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Button(
                        onClick = { onIntent(LessonIntent.PrimaryAction) },
                        enabled = when (phase) {
                            LessonPhase.Review -> true
                            LessonPhase.Learn -> true
                            LessonPhase.Practice -> selectedAnswer != null || submittedAnswer != null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
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
                                LessonPhase.Review -> text.nextPhrase
                                LessonPhase.Learn -> if (isLastLearningCard) text.startPractice else text.nextPhrase
                                LessonPhase.Practice -> when {
                                    submittedAnswer == null -> text.checkAnswer
                                    isLastQuizQuestion -> text.finishLesson
                                    else -> text.nextQuestion
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
}

@Composable
private fun LessonReviewCard(
    item: LessonItemContent,
    supportLanguage: SupportLanguage,
    questionMode: PracticeQuestionMode,
    answerVisible: Boolean,
    onPlayWordClick: () -> Unit,
    onPlayExampleClick: (Int) -> Unit
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            if (answerVisible || questionMode == PracticeQuestionMode.Read) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    AdaptiveWordText(
                        text = item.polish,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        candidateSizes = lessonWordSizes
                    )
                    AudioActionButton(onClick = onPlayWordClick)
                }
            }

            if (answerVisible) {
                Text(
                    text = item.translationForSelectedLanguage(supportLanguage),
                    style = lessonTranslationStyle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    item.examples.take(2).forEachIndexed { index, example ->
                        LessonExampleCard(
                            index = index + 1,
                            polish = example.polish,
                            translation = example.translationForSelectedLanguage(supportLanguage),
                            onPlayClick = null
                        )
                    }
                }
            } else {
                if (questionMode == PracticeQuestionMode.Listen) {
                    Text(
                        text = listeningQuestionTitle(supportLanguage),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )

                    ListeningPrompt(
                        submittedAnswer = null,
                        item = item,
                        supportLanguage = supportLanguage,
                        onPlayWordClick = onPlayWordClick
                    )
                }

                Text(
                    text = reviewRecallHint(supportLanguage),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                )

                Text(
                    text = reviewRevealBottomHint(supportLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                )
            }
        }
    }
}

@Composable
private fun ReviewAnswerButton(
    text: String,
    quality: ReviewQuality,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    onAnswer: (ReviewQuality) -> Unit
) {
    Button(
        onClick = { onAnswer(quality) },
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.medium,
        colors = if (emphasized) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LessonLearnCard(
    item: LessonItemContent,
    supportLanguage: SupportLanguage,
    onPlayWordClick: () -> Unit,
    onPlayExampleClick: (Int) -> Unit
) {
    val spacing = MaterialTheme.appSpacing

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
    ) {
        Column(
            modifier = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.Top
            ) {
                AdaptiveWordText(
                    text = item.polish,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    candidateSizes = lessonWordSizes
                )
                AudioActionButton(onClick = onPlayWordClick)
            }

            Text(
                text = item.translationForSelectedLanguage(supportLanguage),
                style = lessonTranslationStyle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Column(
                modifier = Modifier.padding(top = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                item.examples.take(3).forEachIndexed { index, example ->
                    LessonExampleCard(
                        index = index + 1,
                        polish = example.polish,
                        translation = example.translationForSelectedLanguage(supportLanguage),
                        onPlayClick = null
                    )
                }
            }
        }
    }
}

private val lessonWordSizes = listOf(40.sp, 38.sp, 36.sp, 34.sp, 32.sp, 30.sp, 28.sp)

private val lessonTranslationStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 22.sp,
    lineHeight = 28.sp
)

@Composable
private fun AudioActionButton(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SpeakerIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AudioExampleButton(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .padding(7.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SpeakerIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private val SpeakerIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "SpeakerIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.9f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(5f, 9f)
            verticalLineTo(15f)
            horizontalLineTo(9f)
            lineTo(14f, 19f)
            verticalLineTo(5f)
            lineTo(9f, 9f)
            close()
        }
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.9f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(17f, 9.5f)
            curveTo(19.4f, 11.3f, 19.4f, 12.7f, 17f, 14.5f)
        }
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.9f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(18.7f, 6.8f)
            curveTo(22.0f, 10.0f, 22.0f, 14.0f, 18.7f, 17.2f)
        }
    }.build()
}

@Composable
private fun LessonExampleCard(
    index: Int,
    polish: String,
    translation: String,
    onPlayClick: (() -> Unit)?
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = index.toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = polish,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            }
            if (onPlayClick != null) {
                AudioExampleButton(onClick = onPlayClick)
            }
        }
    }
}

@Composable
private fun LessonPracticeCard(
    lesson: LessonContent,
    item: LessonItemContent,
    supportLanguage: SupportLanguage,
    questionMode: PracticeQuestionMode,
    selectedAnswer: String?,
    submittedAnswer: String?,
    onPlayWordClick: () -> Unit,
    onAnswerSelected: (String) -> Unit
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    val correctAnswer = item.translationForSelectedLanguage(supportLanguage)
    val options = remember(lesson.id, item.id, supportLanguage) {
        buildTranslationOptions(lesson, item, supportLanguage)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.lg)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ) {
            Column(
                modifier = Modifier.padding(spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Text(
                    text = when (questionMode) {
                        PracticeQuestionMode.Read -> supportLanguage.appText.chooseCorrectTranslation
                        PracticeQuestionMode.Listen -> listeningQuestionTitle(supportLanguage)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
                when (questionMode) {
                    PracticeQuestionMode.Read -> AdaptiveWordText(
                        text = item.polish,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        candidateSizes = listOf(62.sp, 58.sp, 54.sp, 50.sp, 46.sp, 42.sp, 38.sp, 34.sp, 30.sp, 28.sp)
                    )
                    PracticeQuestionMode.Listen -> ListeningPrompt(
                        submittedAnswer = submittedAnswer,
                        item = item,
                        supportLanguage = supportLanguage,
                        onPlayWordClick = onPlayWordClick
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            options.forEach { option ->
                val isSelected = selectedAnswer == option
                val isSubmitted = submittedAnswer != null
                val isCorrect = option == correctAnswer
                val isWrongSelection = isSubmitted && isSelected && !isCorrect
                val selectionColor = colors.progressEnd
                val containerColor = when {
                    isSubmitted && isCorrect -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                    isWrongSelection -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
                    isSelected -> MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                }
                val borderColor = when {
                    isSubmitted && isCorrect -> MaterialTheme.colorScheme.primary
                    isWrongSelection -> MaterialTheme.colorScheme.secondary
                    isSelected -> selectionColor.copy(alpha = 0.34f)
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.76f)
                }
                val statusLabel = when {
                    isSubmitted && isCorrect -> lessonCorrectOptionLabel(supportLanguage)
                    isWrongSelection -> lessonWrongOptionLabel(supportLanguage)
                    else -> null
                }

                Surface(
                    onClick = { onAnswerSelected(option) },
                    enabled = !isSubmitted,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = containerColor,
                    contentColor = when {
                        isSubmitted && isCorrect -> MaterialTheme.colorScheme.onPrimaryContainer
                        isWrongSelection -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    border = BorderStroke(
                        width = if (isSelected || isSubmitted && (isCorrect || isWrongSelection)) 2.dp else 1.dp,
                        color = borderColor
                    )
                ) {
                    Box {
                        Row(
                            modifier = Modifier.padding(horizontal = spacing.lg, vertical = 17.dp),
                            horizontalArrangement = Arrangement.spacedBy(spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = Color.Unspecified
                            )
                            if (statusLabel != null) {
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isCorrect) {
                                        colors.progressStart
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    }
                                )
                            }
                        }
                        if (isSelected && !isSubmitted) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(selectionColor)
                            )
                        }
                    }
                }
            }
        }

        if (questionMode == PracticeQuestionMode.Listen && submittedAnswer != null) {
            ListeningAnswerCard(
                item = item,
                supportLanguage = supportLanguage
            )
        }
    }
}

@Composable
private fun ListeningPrompt(
    submittedAnswer: String?,
    item: LessonItemContent,
    supportLanguage: SupportLanguage,
    onPlayWordClick: () -> Unit
) {
    val spacing = MaterialTheme.appSpacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        Surface(
            onClick = onPlayWordClick,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .padding(13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = SpeakerIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = listeningPlayLabel(supportLanguage),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = listeningOnlyHint(supportLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )
                }
            }
        }

        if (submittedAnswer != null) {
            AdaptiveWordText(
                text = item.polish,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                candidateSizes = lessonWordSizes
            )
            Text(
                text = item.translationForSelectedLanguage(supportLanguage),
                style = lessonTranslationStyle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ListeningAnswerCard(
    item: LessonItemContent,
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f)
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            item.examples.take(2).forEachIndexed { index, example ->
                LessonExampleCard(
                    index = index + 1,
                    polish = example.polish,
                    translation = example.translationForSelectedLanguage(supportLanguage),
                    onPlayClick = null
                )
            }
        }
    }
}

@Composable
private fun AdaptiveWordText(
    text: String,
    style: TextStyle,
    color: Color,
    candidateSizes: List<TextUnit>,
    fontWeight: FontWeight = FontWeight.Bold,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxWidthPx = with(density) { maxWidth.roundToPx() }
        val resolvedStyle = candidateSizes.firstOrNull { size ->
            val result = textMeasurer.measure(
                text = AnnotatedString(text),
                style = style.copy(
                    fontSize = size,
                    lineHeight = (size.value * 0.92f).sp,
                    fontWeight = fontWeight
                ),
                maxLines = 1,
                softWrap = false,
                constraints = Constraints(maxWidth = maxWidthPx)
            )
            !result.didOverflowWidth && !result.didOverflowHeight
        }?.let { fittedSize ->
            style.copy(
                fontSize = fittedSize,
                lineHeight = (fittedSize.value * 0.92f).sp,
                fontWeight = fontWeight
            )
        } ?: style.copy(
            fontSize = candidateSizes.last(),
            lineHeight = (candidateSizes.last().value * 0.92f).sp,
            fontWeight = fontWeight
        )

        Text(
            text = text,
            style = resolvedStyle,
            fontWeight = fontWeight,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            color = color
        )
    }
}

private fun lessonPreviousLabel(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "Попереднє"
    SupportLanguage.Russian -> "Предыдущее"
}

private fun lessonCorrectOptionLabel(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "вірно"
    SupportLanguage.Russian -> "верно"
}

private fun lessonWrongOptionLabel(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "ні"
    SupportLanguage.Russian -> "нет"
}

private fun listeningQuestionTitle(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "Прослухайте слово і виберіть переклад"
    SupportLanguage.Russian -> "Прослушайте слово и выберите перевод"
}

private fun listeningPlayLabel(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "Слухати слово"
    SupportLanguage.Russian -> "Слушать слово"
}

private fun listeningOnlyHint(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "Без тексту, тільки на слух"
    SupportLanguage.Russian -> "Без текста, только на слух"
}

private fun reviewTitle(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "Повторення"
    SupportLanguage.Russian -> "Повторение"
}

private fun reviewProgressLabel(
    supportLanguage: SupportLanguage,
    current: Int,
    total: Int
): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> "Слово $current з $total"
    SupportLanguage.Russian -> "Слово $current из $total"
}

private fun reviewRecallHint(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "Спочатку згадайте значення. Потім відкрийте переклад."
    SupportLanguage.Russian -> "Сначала вспомните значение. Потом откройте перевод."
}

private fun reviewRevealLabel(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "Показати переклад"
    SupportLanguage.Russian -> "Показать перевод"
}

private fun reviewRevealBottomHint(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "Відкрийте переклад нижче."
    SupportLanguage.Russian -> "Откройте перевод кнопкой ниже."
}

private fun reviewAnswerLabel(
    supportLanguage: SupportLanguage,
    quality: ReviewQuality
): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> when (quality) {
        ReviewQuality.Know -> "Знаю"
        ReviewQuality.Almost -> "Майже"
        ReviewQuality.Forgot -> "Не пам'ятаю"
    }
    SupportLanguage.Russian -> when (quality) {
        ReviewQuality.Know -> "Знаю"
        ReviewQuality.Almost -> "Почти"
        ReviewQuality.Forgot -> "Не помню"
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

    val options = buildList {
        add(correctAnswer)
        addAll(distractors)
    }.distinct()

    if (options.size < 2) return options

    val shift = item.id.fold(0) { sum, char -> sum + char.code } % options.size
    return options.drop(shift) + options.take(shift)
}

@Preview
@Composable
private fun LessonStudyScreenPreview() {
    PolishThousandTheme {
        val topic = MvpSeedContent.path
        LessonStudyScreen(
            state = LessonUiState(
                sessionKey = "preview",
                lesson = topic.lessons.first()
            )
        )
    }
}
