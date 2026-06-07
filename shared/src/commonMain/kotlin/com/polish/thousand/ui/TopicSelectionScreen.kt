package com.polish.thousand.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.TopicContent
import com.polish.thousand.content.titleFor
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

@Composable
internal fun TopicSelectionScreen(
    supportLanguage: SupportLanguage = SupportLanguage.Ukrainian,
    completedLessonIds: Set<String> = emptySet(),
    onBackClick: () -> Unit = {},
    onStartTopicClick: (TopicContent) -> Unit = {}
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    var selectedTopicId by remember { mutableStateOf(recommendedTopic(completedLessonIds).id) }
    val selectedTopic = MvpSeedContent.topics.first { it.id == selectedTopicId }
    val featuredTopic = recommendedTopic(completedLessonIds)
    val otherTopics = MvpSeedContent.topics.filterNot { it.id == featuredTopic.id }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        colors.heroStart,
                        colors.heroEnd.copy(alpha = 0.82f)
                    )
                )
            )
    ) {
        TopicGlow(
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
                    .padding(bottom = 156.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.lg)
            ) {
                AppTopBar(
                    title = if (supportLanguage == SupportLanguage.Ukrainian) "Теми" else "Темы",
                    onBackClick = onBackClick
                )

                TopicBlockLabel(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Наступне"
                    } else {
                        "Дальше"
                    }
                )

                TopicCard(
                    topic = featuredTopic,
                    completedLessonIds = completedLessonIds,
                    supportLanguage = supportLanguage,
                    selected = selectedTopicId == featuredTopic.id,
                    compact = false,
                    onClick = { selectedTopicId = featuredTopic.id }
                )

                TopicBlockLabel(
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "Інші теми"
                    } else {
                        "Другие темы"
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    otherTopics.forEach { topic ->
                        TopicCard(
                            topic = topic,
                            completedLessonIds = completedLessonIds,
                            supportLanguage = supportLanguage,
                            selected = selectedTopicId == topic.id,
                            compact = true,
                            onClick = { selectedTopicId = topic.id }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Button(
                    onClick = { onStartTopicClick(selectedTopic) },
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
                        text = topicActionLabel(selectedTopic, completedLessonIds, supportLanguage),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicBlockLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )
}

@Composable
private fun TopicCard(
    topic: TopicContent,
    completedLessonIds: Set<String>,
    supportLanguage: SupportLanguage,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    val completedCount = topic.lessons.count { it.id in completedLessonIds }
    val lessonCount = topic.lessons.size
    val itemCount = topic.lessons.sumOf { it.items.size }
    val sampleWords = topic.lessons
        .flatMap { it.items }
        .take(3)
        .map { it.polish.lowercase() }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.74f)
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        shadowElevation = if (selected) 2.dp else 0.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = if (compact) 18.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = topic.titleFor(supportLanguage),
                        style = if (compact) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.displaySmall
                        },
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TopicMetaText("$itemCount ${wordUnit(supportLanguage)}")
                        TopicMetaDot()
                        TopicMetaText(
                            if (supportLanguage == SupportLanguage.Ukrainian) {
                                "$completedCount з $lessonCount уроків"
                            } else {
                                "$completedCount из $lessonCount уроков"
                            }
                        )
                    }
                }

                TopicProgressRing(
                    progress = completedCount.toFloat() / lessonCount.toFloat(),
                    label = "$completedCount/$lessonCount"
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sampleWords.forEach { word ->
                    TopicWordPill(word)
                }
            }
        }
    }
}

@Composable
private fun TopicMetaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}

@Composable
private fun TopicMetaDot() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    )
}

@Composable
private fun TopicWordPill(text: String) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
        )
    }
}

@Composable
private fun TopicProgressRing(
    progress: Float,
    label: String
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.size(58.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            drawArc(
                color = Color(0xFFE7DDD2),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun recommendedTopic(completedLessonIds: Set<String>): TopicContent {
    return MvpSeedContent.topics.firstOrNull { topic ->
        topic.lessons.any { it.id !in completedLessonIds }
    } ?: MvpSeedContent.topics.first()
}

private fun topicActionLabel(
    topic: TopicContent,
    completedLessonIds: Set<String>,
    supportLanguage: SupportLanguage
): String {
    val completedCount = topic.lessons.count { it.id in completedLessonIds }
    val title = topic.titleFor(supportLanguage)
    return when {
        completedCount == 0 -> if (supportLanguage == SupportLanguage.Ukrainian) {
            "Почати: $title"
        } else {
            "Начать: $title"
        }
        completedCount < topic.lessons.size -> if (supportLanguage == SupportLanguage.Ukrainian) {
            "Продовжити: $title"
        } else {
            "Продолжить: $title"
        }
        else -> if (supportLanguage == SupportLanguage.Ukrainian) {
            "Повторити: $title"
        } else {
            "Повторить: $title"
        }
    }
}

private fun wordUnit(language: SupportLanguage): String = when (language) {
    SupportLanguage.Ukrainian -> "слів"
    SupportLanguage.Russian -> "слов"
}

@Composable
private fun TopicGlow(
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
private fun TopicSelectionScreenPreview() {
    PolishThousandTheme {
        TopicSelectionScreen()
    }
}
