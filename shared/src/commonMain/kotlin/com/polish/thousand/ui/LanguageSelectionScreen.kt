package com.polish.thousand.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.polish.thousand.content.BootstrapLanguage
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

@Composable
internal fun LanguageSelectionScreen(
    selectedLanguage: SupportLanguage? = null,
    interfaceLanguage: BootstrapLanguage = BootstrapLanguage.English,
    onLanguageSelected: (SupportLanguage) -> Unit = {}
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    val text = interfaceLanguage.selectionText
    var localSelection by remember(selectedLanguage) { mutableStateOf(selectedLanguage) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        colors.heroStart.copy(alpha = 0.48f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        LessonGlow(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(220.dp),
            brush = Brush.radialGradient(
                listOf(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0f)
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(
                    start = spacing.screenHorizontal,
                    end = spacing.screenHorizontal,
                    top = spacing.xxl,
                    bottom = spacing.lg
                )
        ) {
            LanguageHero(text = text)
            Spacer(modifier = Modifier.height(spacing.xxl))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .selectableGroup(),
                contentPadding = PaddingValues(bottom = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                items(
                    items = SupportLanguage.entries,
                    key = { it.name }
                ) { language ->
                    LanguageChoiceCard(
                        language = language,
                        selected = language == localSelection,
                        onClick = { localSelection = language }
                    )
                }
            }

            Button(
                onClick = { localSelection?.let(onLanguageSelected) },
                enabled = localSelection != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f)
                )
            ) {
                Text(
                    text = if (localSelection == null) text.selectToContinue else text.startLearning,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = text.footer,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.56f)
            )
        }
    }
}

@Composable
private fun LanguageHero(text: LanguageSelectionText) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Aa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = text.title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = text.description,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
        )
    }
}

@Composable
private fun LanguageChoiceCard(
    language: SupportLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.large,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        shadowElevation = if (selected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = language.code,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = language.explanationSample,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}

private val SupportLanguage.explanationSample: String
    get() = when (this) {
        SupportLanguage.Ukrainian -> "Пояснення українською"
        SupportLanguage.Russian -> "Объяснения на русском"
    }

private data class LanguageSelectionText(
    val title: String,
    val description: String,
    val selectToContinue: String,
    val startLearning: String,
    val footer: String
)

private val BootstrapLanguage.selectionText: LanguageSelectionText
    get() = when (this) {
        BootstrapLanguage.English -> LanguageSelectionText(
            title = "What language feels natural to you?",
            description = "We’ll use it for every translation and explanation.",
            selectToContinue = "Choose one to continue",
            startLearning = "Start learning",
            footer = "No account needed · Change anytime"
        )
        BootstrapLanguage.Ukrainian -> LanguageSelectionText(
            title = "Яка мова для вас природна?",
            description = "Ми використовуватимемо її для перекладів і пояснень.",
            selectToContinue = "Оберіть мову, щоб продовжити",
            startLearning = "Почати навчання",
            footer = "Без облікового запису · Можна змінити будь-коли"
        )
        BootstrapLanguage.Russian -> LanguageSelectionText(
            title = "Какой язык для вас привычнее?",
            description = "Мы будем использовать его для переводов и объяснений.",
            selectToContinue = "Выберите язык, чтобы продолжить",
            startLearning = "Начать обучение",
            footer = "Без аккаунта · Можно изменить в любое время"
        )
    }

@Preview
@Composable
private fun LanguageSelectionScreenPreview() {
    PolishThousandTheme {
        LanguageSelectionScreen(
            selectedLanguage = null,
            interfaceLanguage = BootstrapLanguage.English
        )
    }
}
