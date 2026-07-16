package com.polish.thousand.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polish.thousand.content.LearningPath
import com.polish.thousand.content.CompletionRecognition
import com.polish.thousand.content.LessonContent
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

@Composable
internal fun LessonCompletionScreen(
    lesson: LessonContent,
    supportLanguage: SupportLanguage = SupportLanguage.Ukrainian,
    learnedWords: Int,
    addedWords: Int,
    attemptedWords: Int,
    recognition: CompletionRecognition? = null,
    quickReviewWords: Int = 0,
    continuesToNextLesson: Boolean = false,
    onQuickReviewClick: () -> Unit = {},
    onContinueClick: () -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    val milestone = LearningPath.nextMilestone(learnedWords)
    val stageStartWords = LearningPath.milestones
        .lastOrNull { it.wordCount <= learnedWords }
        ?.wordCount
        ?: 0
    val remaining = (milestone.wordCount - learnedWords).coerceAtLeast(0)
    val milestoneProgress = LearningPath.milestoneProgress(learnedWords)
    val missedWords = (attemptedWords - addedWords).coerceAtLeast(0)
    // The first-step screen has one deliberate next action; mistakes remain optional on Home.
    val showQuickReview = quickReviewWords > 0 && recognition != CompletionRecognition.FirstStep
    val bottomContentPadding = if (showQuickReview) 190.dp else 124.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        colors.heroStart,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                    )
                )
            )
            .padding(horizontal = spacing.screenHorizontal)
    ) {
        LessonGlow(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(180.dp),
            brush = Brush.radialGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0f)
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 46.dp, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.Center
        ) {
            if (recognition == CompletionRecognition.FirstStep) {
                FirstStepCompletion(
                    learnedWords = learnedWords,
                    supportLanguage = supportLanguage
                )
            } else {
                CompletionResult(
                    addedWords = addedWords,
                    attemptedWords = attemptedWords,
                    missedWords = missedWords,
                    supportLanguage = supportLanguage
                )
            }

            if (recognition != CompletionRecognition.FirstStep) {
                Spacer(modifier = Modifier.height(spacing.xl))

                CompletionRecognitionNote(
                    recognition = recognition,
                    supportLanguage = supportLanguage
                )

                MilestoneProgressCard(
                    stageStartWords = stageStartWords,
                    milestoneWords = milestone.wordCount,
                    milestoneTitle = milestone.titleFor(supportLanguage),
                    remaining = remaining,
                    milestoneProgress = milestoneProgress,
                    supportLanguage = supportLanguage
                )
            }

        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showQuickReview) {
                Button(
                    onClick = onQuickReviewClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = completionQuickReviewActionLabel(
                            supportLanguage = supportLanguage,
                            hasMistakes = missedWords > 0
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
                ) {
                    Button(
                        onClick = onContinueClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = null
                    ) {
                        Text(
                            text = completionPrimaryActionLabel(
                                supportLanguage = supportLanguage,
                                continuesToNextLesson = continuesToNextLesson,
                                recognition = recognition
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = completionPrimaryActionLabel(
                            supportLanguage = supportLanguage,
                            continuesToNextLesson = continuesToNextLesson,
                            recognition = recognition
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            TextButton(
                onClick = onHomeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = completionHomeActionLabel(supportLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
        }
    }
}

private fun completionHomeActionLabel(supportLanguage: SupportLanguage): String =
    if (supportLanguage == SupportLanguage.Ukrainian) "На головну" else "На главную"

private fun completionPrimaryActionLabel(
    supportLanguage: SupportLanguage,
    continuesToNextLesson: Boolean,
    recognition: CompletionRecognition?
): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> when {
        recognition == CompletionRecognition.FirstStep && continuesToNextLesson -> "Наступні 10 слів"
        continuesToNextLesson -> "Наступний урок"
        else -> "На головний екран"
    }
    SupportLanguage.Russian -> when {
        recognition == CompletionRecognition.FirstStep && continuesToNextLesson -> "Следующие 10 слов"
        continuesToNextLesson -> "Следующий урок"
        else -> "На главный экран"
    }
}

private fun completionQuickReviewActionLabel(
    supportLanguage: SupportLanguage,
    hasMistakes: Boolean
): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> if (hasMistakes) "Повторити помилки" else "Повторити зараз"
    SupportLanguage.Russian -> if (hasMistakes) "Повторить ошибки" else "Повторить сейчас"
}

@Composable
private fun CompletionResult(
    addedWords: Int,
    attemptedWords: Int,
    missedWords: Int,
    supportLanguage: SupportLanguage
) {
    Column {
        Text(
            text = "+$addedWords",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 62.sp,
                lineHeight = 58.sp,
                letterSpacing = (-2.4).sp
            ),
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (supportLanguage == SupportLanguage.Ukrainian) "слів" else "слов",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 36.sp,
                lineHeight = 38.sp,
                letterSpacing = (-1.2).sp
            ),
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (supportLanguage == SupportLanguage.Ukrainian) {
                if (missedWords == 0) {
                    "Зараховано $addedWords з $attemptedWords."
                } else {
                    "Зараховано $addedWords з $attemptedWords. Помилки повернуться в повторення."
                }
            } else {
                if (missedWords == 0) {
                    "Засчитано $addedWords из $attemptedWords."
                } else {
                    "Засчитано $addedWords из $attemptedWords. Ошибки вернутся в повторение."
                }
            },
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 21.sp,
                lineHeight = 28.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f)
        )
    }
}

@Composable
private fun FirstStepCompletion(
    learnedWords: Int,
    supportLanguage: SupportLanguage
) {
    val remainingToFirstMilestone = (100 - learnedWords).coerceAtLeast(0)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (supportLanguage == SupportLanguage.Ukrainian) "ПЕРШИЙ КРОК" else "ПЕРВЫЙ ШАГ",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (supportLanguage == SupportLanguage.Ukrainian) {
                if (learnedWords == 10) "Перші 10 слів пройдено."
                else "Вже $learnedWords слів пройдено."
            } else {
                if (learnedWords == 10) "Первые 10 слов пройдены."
                else "Уже $learnedWords слов пройдено."
            },
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 38.sp,
                lineHeight = 40.sp,
                letterSpacing = (-1.7).sp
            ),
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (supportLanguage == SupportLanguage.Ukrainian) {
                "До першої великої відмітки залишилося $remainingToFirstMilestone."
            } else {
                "До первой большой отметки осталось $remainingToFirstMilestone."
            },
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, lineHeight = 27.sp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f)
        )
    }
}

@Composable
private fun CompletionRecognitionNote(
    recognition: CompletionRecognition?,
    supportLanguage: SupportLanguage
) {
    val message = when (recognition) {
        CompletionRecognition.HalfwayToFirstMilestone -> if (supportLanguage == SupportLanguage.Ukrainian) {
            "Половина до першої відмітки."
        } else {
            "Половина до первой отметки."
        }
        is CompletionRecognition.CompactMilestone -> when (recognition.wordCount) {
            250 -> if (supportLanguage == SupportLanguage.Ukrainian) {
                "250 слів. База вже є."
            } else {
                "250 слов. База уже есть."
            }
            750 -> if (supportLanguage == SupportLanguage.Ukrainian) {
                "750 слів. Фініш уже близько."
            } else {
                "750 слов. Финиш уже близко."
            }
            else -> null
        }
        else -> null
    } ?: return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun MilestoneProgressCard(
    stageStartWords: Int,
    milestoneWords: Int,
    milestoneTitle: String,
    remaining: Int,
    milestoneProgress: Float,
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (supportLanguage == SupportLanguage.Ukrainian) "Наступна ціль" else "Следующая цель",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$milestoneWords · $milestoneTitle",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (supportLanguage == SupportLanguage.Ukrainian) {
                            "ще $remaining"
                        } else {
                            "ещё $remaining"
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            ProgressLine(progress = milestoneProgress)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stageStartWords.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.48f)
                )
                Text(
                    text = milestoneWords.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.48f)
                )
            }
        }
    }
}

@Composable
private fun ProgressLine(progress: Float) {
    val colors = MaterialTheme.appColors
    val height = 12.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                color = colors.progressTrack,
                shape = MaterialTheme.shapes.extraLarge
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            colors.progressStart,
                            colors.progressMiddle,
                            colors.progressEnd
                        )
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                )
        )
    }
}

@Preview
@Composable
private fun LessonCompletionScreenPreview() {
    PolishThousandTheme {
        val lesson = MvpSeedContent.lessons.first()
        LessonCompletionScreen(
            lesson = lesson,
            learnedWords = 9,
            addedWords = 9,
            attemptedWords = 10
        )
    }
}
