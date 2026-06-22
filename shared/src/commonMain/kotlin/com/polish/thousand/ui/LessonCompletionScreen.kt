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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polish.thousand.content.LearningPath
import com.polish.thousand.content.LearningTargetWords
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
    quickReviewWords: Int = 0,
    continuesToNextLesson: Boolean = false,
    onQuickReviewClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
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
    val totalProgress = (learnedWords.toFloat() / LearningTargetWords).coerceIn(0f, 1f)
    val missedWords = (attemptedWords - addedWords).coerceAtLeast(0)
    val bottomContentPadding = if (quickReviewWords > 0) 154.dp else 92.dp

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
            CompletionResult(
                addedWords = addedWords,
                attemptedWords = attemptedWords,
                missedWords = missedWords,
                supportLanguage = supportLanguage
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            MilestoneProgressCard(
                stageStartWords = stageStartWords,
                milestoneWords = milestone.wordCount,
                milestoneTitle = milestone.titleFor(supportLanguage),
                remaining = remaining,
                milestoneProgress = milestoneProgress,
                supportLanguage = supportLanguage
            )

            Spacer(modifier = Modifier.height(spacing.md))

            OverallPathCard(
                totalProgress = totalProgress,
                supportLanguage = supportLanguage
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (quickReviewWords > 0) {
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
                                continuesToNextLesson = continuesToNextLesson
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
                            continuesToNextLesson = continuesToNextLesson
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun completionPrimaryActionLabel(
    supportLanguage: SupportLanguage,
    continuesToNextLesson: Boolean
): String = when (supportLanguage) {
    SupportLanguage.Ukrainian -> if (continuesToNextLesson) "Наступний урок" else "На головний екран"
    SupportLanguage.Russian -> if (continuesToNextLesson) "Следующий урок" else "На главный экран"
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
                verticalAlignment = Alignment.Bottom
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
                    Column(
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = remaining.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (supportLanguage == SupportLanguage.Ukrainian) "лишилось" else "осталось",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Ще $remaining слів до цієї відмітки."
                    } else {
                        "Еще $remaining слов до этой отметки."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
        }
    }
}

@Composable
private fun OverallPathCard(
    totalProgress: Float,
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = spacing.xl, vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Text(
                text = if (supportLanguage == SupportLanguage.Ukrainian) "Загальний шлях" else "Общий путь",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
            )
            ProgressLine(progress = totalProgress, compact = true)
            Text(
                text = if (supportLanguage == SupportLanguage.Ukrainian) {
                    "Рух іде далі крок за кроком."
                } else {
                    "Движение идет дальше шаг за шагом."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f)
            )
        }
    }
}

@Composable
private fun ProgressLine(
    progress: Float,
    compact: Boolean = false
) {
    val colors = MaterialTheme.appColors
    val height = if (compact) 7.dp else 12.dp
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
