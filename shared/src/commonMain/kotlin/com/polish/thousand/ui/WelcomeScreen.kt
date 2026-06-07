package com.polish.thousand.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.appText
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

private const val WelcomeLearnedWords = 124
private const val WelcomeTotalWords = 1000
private const val WelcomeNextLessonMinutes = 6
private const val WelcomeNextLessonMinutesTotal = 10

@Composable
internal fun WelcomeScreen(
    supportLanguage: SupportLanguage = SupportLanguage.Ukrainian,
    onStartLearningClick: () -> Unit = {},
    onExploreTopicsClick: () -> Unit = {}
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    val text = supportLanguage.appText
    val learnedWords = WelcomeLearnedWords
    val totalWords = WelcomeTotalWords
    val remainingWords = totalWords - learnedWords
    val progress = learnedWords.toFloat() / totalWords.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        colors.heroEnd,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
                    )
            )
        )
    ) {
        WelcomeGlow(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = (-12).dp)
                .size(220.dp),
            brush = Brush.radialGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f),
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
                    .statusBarsPadding()
                    .padding(bottom = 156.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) "Ваш прогрес" else "Ваш прогресс",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(spacing.lg))

                WelcomeProgressRing(
                    progress = progress,
                    learnedWords = learnedWords,
                    totalWords = totalWords,
                    supportLanguage = supportLanguage
                )

                Spacer(modifier = Modifier.height(spacing.lg))

                Text(
                    text = when (supportLanguage) {
                        SupportLanguage.Ukrainian ->
                            "Ви вже вивчили $learnedWords слів. До повної цілі залишилось $remainingWords."
                        SupportLanguage.Russian ->
                            "Вы уже выучили $learnedWords слов. До полной цели осталось $remainingWords."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.76f)
                )

                Spacer(modifier = Modifier.height(spacing.xl))

                WelcomeNextStepCard(supportLanguage = supportLanguage)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onStartLearningClick,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = text.continueLearning,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(spacing.md))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Button(
                        onClick = onExploreTopicsClick,
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = null
                    ) {
                        Text(
                            text = text.exploreTopics,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.md))
            }
        }
    }
}

@Composable
private fun WelcomeProgressRing(
    progress: Float,
    learnedWords: Int,
    totalWords: Int,
    supportLanguage: SupportLanguage
) {
    Box(
        modifier = Modifier.size(188.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            drawArc(
                color = Color(0xFFEFE7DE),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF19877C),
                        Color(0xFF84C5C0),
                        Color(0xFF19877C)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = learnedWords.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = when (supportLanguage) {
                    SupportLanguage.Ukrainian -> if (totalWords >= 1000) "з 1 000 слів" else "з $totalWords"
                    SupportLanguage.Russian -> if (totalWords >= 1000) "из 1 000 слов" else "из $totalWords"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.56f)
            )
        }
    }
}

@Composable
private fun WelcomeNextStepCard(
    supportLanguage: SupportLanguage
) {
    val spacing = MaterialTheme.appSpacing
    val lessonProgress = WelcomeNextLessonMinutes.toFloat() / WelcomeNextLessonMinutesTotal.toFloat()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(spacing.xl),
            horizontalArrangement = Arrangement.spacedBy(spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) "Наступний крок" else "Следующий шаг",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                )
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) "Магазин" else "Магазин",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "3 з 5 уроків завершено"
                    } else {
                        "3 из 5 уроков завершено"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            }

            WelcomeTimeRing(
                minutes = WelcomeNextLessonMinutes,
                progress = lessonProgress,
                supportLanguage = supportLanguage
            )
        }
    }
}

@Composable
private fun WelcomeTimeRing(
    minutes: Int,
    progress: Float,
    supportLanguage: SupportLanguage
) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            drawArc(
                color = Color(0xFFE9DED3),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF19877C),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = minutes.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (supportLanguage == SupportLanguage.Ukrainian) "хв" else "мин",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
            )
        }
    }
}

@Composable
private fun WelcomeGlow(
    modifier: Modifier,
    brush: Brush
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(brush)
    )
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    PolishThousandTheme {
        WelcomeScreen()
    }
}
