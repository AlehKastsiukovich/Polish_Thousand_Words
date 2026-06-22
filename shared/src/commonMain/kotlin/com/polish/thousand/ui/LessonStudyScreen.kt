package com.polish.thousand.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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

private enum class LessonPhase {
    Review,
    Learn,
    Practice
}

@Composable
internal fun LessonStudyScreen(
    lesson: LessonContent,
    reviewItems: List<LessonItemContent> = emptyList(),
    reviewOnly: Boolean = false,
    supportLanguage: SupportLanguage = SupportLanguage.Ukrainian,
    onBackClick: () -> Unit = {},
    onReviewAnswer: (String, ReviewQuality) -> Unit = { _, _ -> },
    onReviewCompleteClick: () -> Unit = {},
    onCompleteClick: (Set<String>) -> Unit = {}
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    val text = supportLanguage.appText
    val reviewKey = remember(reviewItems) { reviewItems.joinToString(separator = "|") { it.id } }
    var phase by remember(lesson.id, reviewKey) {
        mutableStateOf(if (reviewItems.isEmpty()) LessonPhase.Learn else LessonPhase.Review)
    }
    var reviewIndex by remember(lesson.id, reviewKey) { mutableIntStateOf(0) }
    var currentIndex by remember(lesson.id) { mutableIntStateOf(0) }
    var quizIndex by remember(lesson.id) { mutableIntStateOf(0) }
    var selectedAnswer by remember(lesson.id) { mutableStateOf<String?>(null) }
    var submittedAnswer by remember(lesson.id) { mutableStateOf<String?>(null) }
    var correctPracticeWordIds by remember(lesson.id) { mutableStateOf(emptySet<String>()) }
    var isReviewAnswerVisible by remember(lesson.id, reviewKey, reviewIndex) { mutableStateOf(false) }

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
    val submitReviewAnswer: (LessonItemContent, ReviewQuality) -> Unit = { item, quality ->
        onReviewAnswer(item.id, quality)
        isReviewAnswerVisible = false
        if (reviewIndex == reviewItems.lastIndex) {
            if (reviewOnly) {
                onReviewCompleteClick()
            } else {
                phase = LessonPhase.Learn
            }
        } else {
            reviewIndex += 1
        }
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
                            answerVisible = isReviewAnswerVisible
                        )
                    }
                    LessonPhase.Learn -> LessonLearnCard(
                        item = learningItem,
                        supportLanguage = supportLanguage
                    )
                    LessonPhase.Practice -> LessonPracticeCard(
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
                                onClick = { submitReviewAnswer(item, ReviewQuality.Know) },
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
                                    onAnswer = { submitReviewAnswer(item, it) }
                                )
                                ReviewAnswerButton(
                                    text = reviewAnswerLabel(supportLanguage, ReviewQuality.Forgot),
                                    quality = ReviewQuality.Forgot,
                                    modifier = Modifier.weight(1f),
                                    onAnswer = { submitReviewAnswer(item, it) }
                                )
                            }
                        } else {
                            Button(
                                onClick = { isReviewAnswerVisible = true },
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
                                when (phase) {
                                    LessonPhase.Review -> Unit
                                    LessonPhase.Learn -> {
                                        if (currentIndex > 0) currentIndex -= 1 else onBackClick()
                                    }
                                    LessonPhase.Practice -> {
                                        if (quizIndex > 0) {
                                            val currentItem = lesson.items[quizIndex]
                                            val targetItem = lesson.items[quizIndex - 1]
                                            correctPracticeWordIds = correctPracticeWordIds -
                                                currentItem.id -
                                                targetItem.id
                                            quizIndex -= 1
                                            selectedAnswer = null
                                            submittedAnswer = null
                                        } else {
                                            correctPracticeWordIds -= quizItem.id
                                            phase = LessonPhase.Learn
                                            currentIndex = lesson.items.lastIndex
                                            selectedAnswer = null
                                            submittedAnswer = null
                                        }
                                    }
                                }
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
                        onClick = {
                            when (phase) {
                                LessonPhase.Review -> Unit
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
                                        correctPracticeWordIds = if (
                                            selectedAnswer == quizItem.translationForSelectedLanguage(supportLanguage)
                                        ) {
                                            correctPracticeWordIds + quizItem.id
                                        } else {
                                            correctPracticeWordIds - quizItem.id
                                        }
                                    } else if (isLastQuizQuestion) {
                                        onCompleteClick(correctPracticeWordIds)
                                    } else {
                                        quizIndex += 1
                                        selectedAnswer = null
                                        submittedAnswer = null
                                    }
                                }
                            }
                        },
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
    answerVisible: Boolean
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
            AdaptiveWordText(
                text = item.polish,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                candidateSizes = lessonWordSizes
            )

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
                            translation = example.translationForSelectedLanguage(supportLanguage)
                        )
                    }
                }
            } else {
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
    supportLanguage: SupportLanguage
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

            Column(
                modifier = Modifier.padding(top = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                item.examples.take(3).forEachIndexed { index, example ->
                    LessonExampleCard(
                        index = index + 1,
                        polish = example.polish,
                        translation = example.translationForSelectedLanguage(supportLanguage)
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
private fun LessonExampleCard(
    index: Int,
    polish: String,
    translation: String
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
        }
    }
}

@Composable
private fun LessonPracticeCard(
    lesson: LessonContent,
    item: LessonItemContent,
    supportLanguage: SupportLanguage,
    selectedAnswer: String?,
    submittedAnswer: String?,
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
                    text = supportLanguage.appText.chooseCorrectTranslation,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
                AdaptiveWordText(
                    text = item.polish,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    candidateSizes = listOf(62.sp, 58.sp, 54.sp, 50.sp, 46.sp, 42.sp, 38.sp, 34.sp, 30.sp, 28.sp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            options.forEach { option ->
                val isSelected = selectedAnswer == option
                val isSubmitted = submittedAnswer != null
                val isCorrect = option == correctAnswer
                val isWrongSelection = isSubmitted && isSelected && !isCorrect
                val containerColor = when {
                    isSubmitted && isCorrect -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                    isWrongSelection -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
                    isSelected -> colors.chipContainer
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                }
                val borderColor = when {
                    isSubmitted && isCorrect -> colors.progressStart
                    isWrongSelection -> MaterialTheme.colorScheme.secondary
                    isSelected -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.76f)
                }
                val statusLabel = when {
                    isSubmitted && isCorrect -> lessonCorrectOptionLabel(supportLanguage)
                    isWrongSelection -> lessonWrongOptionLabel(supportLanguage)
                    else -> null
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isSubmitted) { onAnswerSelected(option) },
                    shape = MaterialTheme.shapes.medium,
                    color = containerColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
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
                            color = MaterialTheme.colorScheme.onSurface
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
                }
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
            lesson = topic.lessons.first()
        )
    }
}
