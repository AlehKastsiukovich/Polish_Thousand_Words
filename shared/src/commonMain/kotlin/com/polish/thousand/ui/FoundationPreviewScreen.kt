package com.polish.thousand.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

@Composable
internal fun FoundationPreviewScreen() {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenVertical)
    ) {
        HeaderRow()
        Spacer(modifier = Modifier.height(spacing.xl))
        HeroCard()
        Spacer(modifier = Modifier.height(spacing.lg))
        ProgressBlock()
        Spacer(modifier = Modifier.height(spacing.xl))
        TopicsRow()
        Spacer(modifier = Modifier.weight(1f, fill = true))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Почати зараз",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(spacing.md))
        Text(
            text = "Переглянути теми",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(spacing.sm))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(120.dp)
                .height(4.dp)
                .clip(MaterialTheme.shapes.small)
                .background(colors.bottomHandle)
        )
    }
}

@Composable
private fun HeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PolishThousand",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LanguagePill(text = "UA", selected = true)
                LanguagePill(text = "RU", selected = false)
            }
        }
    }
}

@Composable
private fun HeroCard() {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.appSpacing

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(colors.heroStart, colors.heroEnd, colors.heroGlow)
                )
            )
            .padding(spacing.xl)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = colors.badgeContainer
            ) {
                Text(
                    text = "Крок 1",
                    modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xs),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.badgeContent
                )
            }

            Text(
                text = "Твій впевнений старт у Польщі",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Практична польська для щоденних справ, роботи й адаптації.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.76f)
            )
        }
    }
}

@Composable
private fun ProgressBlock() {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialBubble("A", colors.socialProofA)
            SocialBubble("M", colors.socialProofB)
            SocialBubble("K", colors.socialProofC)

            Text(
                text = "18,600+ вже приєдналися",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Прогрес курсу",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
            )
            Text(
                text = "85% проходять далі",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
            )
        }

        LinearProgressIndicator(
            progress = { 0.72f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(MaterialTheme.shapes.small),
            color = colors.progressStart,
            trackColor = colors.progressTrack
        )
    }
}

@Composable
private fun TopicsRow() {
    val spacing = MaterialTheme.appSpacing
    val scrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Text(
            text = "Теми",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
        )

        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            TopicChip(text = "Магазин", selected = true)
            TopicChip(text = "Транспорт", selected = false)
            TopicChip(text = "Лікар", selected = false)
            TopicChip(text = "Робота", selected = false)
        }
    }
}

@Composable
private fun TopicChip(
    text: String,
    selected: Boolean
) {
    val colors = MaterialTheme.appColors
    val backgroundColor = if (selected) {
        colors.chipSelectedContainer
    } else {
        colors.chipContainer
    }
    val contentColor = if (selected) {
        colors.chipSelectedContent
    } else {
        colors.chipContent
    }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

@Composable
private fun LanguagePill(
    text: String,
    selected: Boolean
) {
    val colors = MaterialTheme.appColors
    val backgroundColor = if (selected) colors.languageSelectedContainer else colors.languageContainer
    val contentColor = if (selected) colors.languageSelectedContent else colors.languageContent

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

@Composable
private fun SocialBubble(
    label: String,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
