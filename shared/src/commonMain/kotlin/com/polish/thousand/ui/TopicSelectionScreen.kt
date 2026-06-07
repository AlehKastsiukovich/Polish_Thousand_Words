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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.polish.thousand.content.MvpSeedContent
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.TopicContent
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

@Composable
internal fun TopicSelectionScreen(
    supportLanguage: SupportLanguage = SupportLanguage.Ukrainian,
    completedLessonIds: Set<String> = emptySet(),
    onStartTopicClick: (TopicContent) -> Unit = {}
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    var selectedTopicId by remember { mutableStateOf(MvpSeedContent.topics.first().id) }
    val selectedTopic = MvpSeedContent.topics.first { it.id == selectedTopicId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        colors.heroStart,
                        colors.heroEnd.copy(alpha = 0.85f)
                    )
                )
            )
    ) {
        TopicGlow(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-48).dp, y = (-12).dp)
                .size(180.dp),
            brush = Brush.radialGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0f)
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
            TopicHeader(supportLanguage = supportLanguage)
            Spacer(modifier = Modifier.height(spacing.xl))
            TopicHeroSummary(
                selectedTopic = selectedTopic,
                completedLessonIds = completedLessonIds
            )
            Spacer(modifier = Modifier.height(spacing.xl))

            Text(
                text = "Choose a starting topic",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(spacing.sm))
            Text(
                text = "Start with the situations you will meet first. You can unlock the rest step by step.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )
            Spacer(modifier = Modifier.height(spacing.lg))

            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                MvpSeedContent.topics.forEach { topic ->
                    TopicCard(
                        topic = topic,
                        completedLessonIds = completedLessonIds,
                        selected = topic.id == selectedTopicId,
                        onClick = { selectedTopicId = topic.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))
            SupportiveNote()
            Spacer(modifier = Modifier.height(spacing.xl))

            Button(
                onClick = { onStartTopicClick(selectedTopic) },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = topicActionLabel(selectedTopic, completedLessonIds),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TopicHeader(
    supportLanguage: SupportLanguage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "${supportLanguage.englishName} support",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Your first Polish steps",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
        ) {
            Text(
                text = "${MvpSeedContent.totalLessons} lessons",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
            )
        }
    }
}

@Composable
private fun TopicHeroSummary(
    selectedTopic: TopicContent,
    completedLessonIds: Set<String>
) {
    val spacing = MaterialTheme.appSpacing
    val completedCount = selectedTopic.lessons.count { it.id in completedLessonIds }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    ) {
        Column(
            modifier = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopicSummaryPill(text = "${MvpSeedContent.topics.size} topics")
                TopicSummaryPill(text = "${MvpSeedContent.totalItems} phrases")
            }

            Text(
                text = selectedTopic.title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = selectedTopic.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
            )
            Text(
                text = "$completedCount of ${selectedTopic.lessons.size} lessons completed",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun TopicSummaryPill(text: String) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun TopicCard(
    topic: TopicContent,
    completedLessonIds: Set<String>,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val lessonCount = topic.lessons.size
    val itemCount = topic.lessons.sumOf { it.items.size }
    val completedCount = topic.lessons.count { it.id in completedLessonIds }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        text = if (completedCount > 0) "$completedCount / $lessonCount done" else "Ready to start",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = topic.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary else colors.progressTrack
                        )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TopicMetaPill(text = "$completedCount / $lessonCount lessons", selected = selected)
                TopicMetaPill(text = "$itemCount phrases", selected = selected)
            }
        }
    }
}

private fun topicActionLabel(
    topic: TopicContent,
    completedLessonIds: Set<String>
): String {
    val completedCount = topic.lessons.count { it.id in completedLessonIds }
    return if (completedCount == 0) {
        "Start with ${topic.title}"
    } else if (completedCount < topic.lessons.size) {
        "Continue ${topic.title}"
    } else {
        "Review ${topic.title}"
    }
}

@Composable
private fun TopicMetaPill(
    text: String,
    selected: Boolean
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun SupportiveNote() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Text(
                text = "You do not need to learn everything now. Pick one useful topic and finish one calm lesson first.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
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
