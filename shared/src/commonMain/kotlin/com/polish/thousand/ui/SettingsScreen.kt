package com.polish.thousand.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.appText
import com.polish.thousand.content.rememberAppVersion
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

@Composable
internal fun SettingsScreen(
    supportLanguage: SupportLanguage,
    hasPremium: Boolean,
    isPaymentInProgress: Boolean = false,
    paymentMessage: String? = null,
    completedLessonIds: Set<String>,
    learnedWords: Int,
    onBackClick: () -> Unit = {},
    onRestorePurchasesClick: () -> Unit = {},
    onUnlockClick: () -> Unit = {},
    onPaymentMessageClick: () -> Unit = {},
    onLanguageChanged: (SupportLanguage) -> Unit = {},
    onResetProgressClick: () -> Unit = {}
) {
    var isLanguagePickerVisible by rememberSaveable { mutableStateOf(false) }
    var isResetConfirmationVisible by rememberSaveable { mutableStateOf(false) }
    val appVersion = rememberAppVersion()

    SettingsBackground {
        if (isLanguagePickerVisible) {
            SupportLanguagePicker(
                selectedLanguage = supportLanguage,
                onBackClick = { isLanguagePickerVisible = false },
                onLanguageSelected = { language ->
                    onLanguageChanged(language)
                    isLanguagePickerVisible = false
                }
            )
        } else {
            CompactSettingsContent(
                supportLanguage = supportLanguage,
                hasPremium = hasPremium,
                isPaymentInProgress = isPaymentInProgress,
                paymentMessage = paymentMessage,
                completedLessons = completedLessonIds.size,
                learnedWords = learnedWords,
                onBackClick = onBackClick,
                onLanguageClick = { isLanguagePickerVisible = true },
                onRestorePurchasesClick = onRestorePurchasesClick,
                onUnlockClick = onUnlockClick,
                onPaymentMessageClick = onPaymentMessageClick,
                onResetProgressClick = { isResetConfirmationVisible = true },
                version = appVersion.displayValue()
            )
        }

        if (isResetConfirmationVisible) {
            val text = supportLanguage.appText
            AlertDialog(
                onDismissRequest = { isResetConfirmationVisible = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = text.settingsResetProgressTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = text.settingsResetProgressMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            isResetConfirmationVisible = false
                            onResetProgressClick()
                        }
                    ) {
                        Text(
                            text = text.settingsResetProgressConfirm,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isResetConfirmationVisible = false }) {
                        Text(text = text.settingsResetProgressCancel)
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsBackground(content: @Composable () -> Unit) {
    val colors = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        colors.heroStart,
                        colors.heroEnd.copy(alpha = 0.68f)
                    )
                )
            )
    ) {
        LessonGlow(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-74).dp, y = 56.dp)
                .size(210.dp),
            brush = Brush.radialGradient(
                listOf(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0f)
                )
            )
        )
        content()
    }
}

@Composable
private fun CompactSettingsContent(
    supportLanguage: SupportLanguage,
    hasPremium: Boolean,
    isPaymentInProgress: Boolean,
    paymentMessage: String?,
    completedLessons: Int,
    learnedWords: Int,
    onBackClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onRestorePurchasesClick: () -> Unit,
    onUnlockClick: () -> Unit,
    onPaymentMessageClick: () -> Unit,
    onResetProgressClick: () -> Unit,
    version: String
) {
    val spacing = MaterialTheme.appSpacing
    val text = supportLanguage.appText

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = text.settings,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screenHorizontal)
        ) {
            SettingsSectionLabel(text = text.settingsLearningSection)
            Spacer(modifier = Modifier.height(spacing.sm))

            LanguageSettingRow(
                language = supportLanguage,
                subtitle = text.settingsLanguageSubtitle,
                changeLabel = text.settingsChangeLanguage,
                onClick = onLanguageClick
            )

            Spacer(modifier = Modifier.height(spacing.xl))
            SettingsSectionLabel(text = text.settingsAccessSection)
            Spacer(modifier = Modifier.height(spacing.sm))

            AccessAndDataCard(
                supportLanguage = supportLanguage,
                hasPremium = hasPremium,
                isPaymentInProgress = isPaymentInProgress,
                completedLessons = completedLessons,
                learnedWords = learnedWords,
                onUnlockClick = onUnlockClick,
                onRestorePurchasesClick = onRestorePurchasesClick,
                onResetProgressClick = onResetProgressClick,
                hasProgress = learnedWords > 0 || completedLessons > 0
            )

            if (paymentMessage != null) {
                Spacer(modifier = Modifier.height(spacing.md))
                Surface(
                    onClick = onPaymentMessageClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)
                ) {
                    Text(
                        text = paymentMessage,
                        modifier = Modifier.padding(spacing.md),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${text.settingsVersion} $version",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = spacing.lg),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun LanguageSettingRow(
    language: SupportLanguage,
    subtitle: String,
    changeLabel: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        // Keep this opaque: translucency showed the background glow as a visible band on the row.
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageCodeBadge(
                code = language.code,
                selected = true
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = changeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            ChevronIcon(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(20.dp)
            )
        }
    }
}

@Composable
private fun AccessAndDataCard(
    supportLanguage: SupportLanguage,
    hasPremium: Boolean,
    isPaymentInProgress: Boolean,
    completedLessons: Int,
    learnedWords: Int,
    onUnlockClick: () -> Unit,
    onRestorePurchasesClick: () -> Unit,
    onResetProgressClick: () -> Unit,
    hasProgress: Boolean
) {
    val text = supportLanguage.appText
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (hasPremium) text.premiumUnlocked else text.freeStarterPlan,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = openedWordsLabel(
                            wordCount = if (hasPremium) 1_000 else 100,
                            language = supportLanguage
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!hasPremium) {
                    TextButton(
                        onClick = onUnlockClick,
                        enabled = !isPaymentInProgress
                    ) {
                        Text(
                            text = unlockShortLabel(supportLanguage),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            SettingsDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsStat(value = learnedWords, label = text.wordsLearnedLabel.lowercase())
                SettingsStat(value = completedLessons, label = text.lessonsLabel.lowercase())
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = text.settingsProgressSaved,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!hasPremium) {
                SettingsDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !isPaymentInProgress,
                            role = Role.Button,
                            onClick = onRestorePurchasesClick
                        )
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = text.restorePurchases,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    ChevronIcon(modifier = Modifier.size(19.dp))
                }
            }

            if (hasProgress) {
                SettingsDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.Button,
                            onClick = onResetProgressClick
                        )
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = text.settingsResetProgress,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = text.settingsResetProgressDescription,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ChevronIcon(modifier = Modifier.size(19.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsStat(value: Int, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun SupportLanguagePicker(
    selectedLanguage: SupportLanguage,
    onBackClick: () -> Unit,
    onLanguageSelected: (SupportLanguage) -> Unit
) {
    val spacing = MaterialTheme.appSpacing
    val text = selectedLanguage.appText
    var query by rememberSaveable { mutableStateOf("") }
    val allLanguages = SupportLanguage.entries
    val filteredLanguages = allLanguages.filter { language ->
        query.isBlank() || language.matchesQuery(query, selectedLanguage)
    }
    val useGroups = query.isBlank() && allLanguages.size > RecommendedLanguageCount
    val recommendedLanguages = filteredLanguages.take(RecommendedLanguageCount)
    val otherLanguages = filteredLanguages.drop(RecommendedLanguageCount)

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = text.supportLanguageTitle,
            onBackClick = onBackClick
        )

        Text(
            text = text.settingsChangesImmediately,
            modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (allLanguages.size > SearchLanguageThreshold) {
            Spacer(modifier = Modifier.height(spacing.lg))
            LanguageSearchField(
                value = query,
                placeholder = text.settingsSearchLanguage,
                onValueChange = { query = it }
            )
        }

        Spacer(modifier = Modifier.height(spacing.lg))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                bottom = spacing.xl
            )
        ) {
            if (useGroups) {
                item {
                    SettingsSectionLabel(text = text.settingsRecommendedLanguages)
                    Spacer(modifier = Modifier.height(spacing.sm))
                }
            }

            items(
                items = if (useGroups) recommendedLanguages else filteredLanguages,
                key = { it.code }
            ) { language ->
                LanguagePickerRow(
                    language = language,
                    displayLanguage = selectedLanguage,
                    selected = language == selectedLanguage,
                    selectedLabel = text.settingsSelectedLanguage,
                    onClick = { onLanguageSelected(language) }
                )
            }

            if (useGroups && otherLanguages.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(spacing.xl))
                    SettingsSectionLabel(text = text.settingsAllLanguages)
                    Spacer(modifier = Modifier.height(spacing.sm))
                }
                items(otherLanguages, key = { it.code }) { language ->
                    LanguagePickerRow(
                        language = language,
                        displayLanguage = selectedLanguage,
                        selected = language == selectedLanguage,
                        selectedLabel = text.settingsSelectedLanguage,
                        onClick = { onLanguageSelected(language) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSearchField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.appSpacing.screenHorizontal),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchIcon(modifier = Modifier.size(19.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Composable
private fun LanguagePickerRow(
    language: SupportLanguage,
    displayLanguage: SupportLanguage,
    selected: Boolean,
    selectedLabel: String,
    onClick: () -> Unit
) {
    val selectedContainer = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) selectedContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageCodeBadge(code = language.code, selected = selected)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (selected) selectedLabel else language.localizedNameFor(displayLanguage),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    CheckIcon(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(MaterialTheme.appSpacing.sm))
}

@Composable
private fun LanguageCodeBadge(code: String, selected: Boolean) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = code,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun ChevronIcon(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.36f, size.height * 0.24f),
            end = Offset(size.width * 0.64f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.64f, size.height * 0.5f),
            end = Offset(size.width * 0.36f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SearchIcon(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        drawCircle(
            color = color,
            radius = size.minDimension * 0.3f,
            center = Offset(size.width * 0.43f, size.height * 0.43f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.64f, size.height * 0.64f),
            end = Offset(size.width * 0.84f, size.height * 0.84f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun CheckIcon(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onPrimary
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.54f),
            end = Offset(size.width * 0.42f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.42f, size.height * 0.76f),
            end = Offset(size.width * 0.84f, size.height * 0.26f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun SupportLanguage.matchesQuery(
    query: String,
    displayLanguage: SupportLanguage
): Boolean {
    val normalizedQuery = query.trim().lowercase()
    return nativeName.lowercase().contains(normalizedQuery) ||
        englishName.lowercase().contains(normalizedQuery) ||
        localizedNameFor(displayLanguage).lowercase().contains(normalizedQuery) ||
        code.lowercase().contains(normalizedQuery)
}

private fun SupportLanguage.localizedNameFor(displayLanguage: SupportLanguage): String =
    when (displayLanguage) {
        SupportLanguage.Ukrainian -> when (this) {
            SupportLanguage.Ukrainian -> "Українська"
            SupportLanguage.Russian -> "Російська"
        }
        SupportLanguage.Russian -> when (this) {
            SupportLanguage.Ukrainian -> "Украинский"
            SupportLanguage.Russian -> "Русский"
        }
    }

private fun openedWordsLabel(wordCount: Int, language: SupportLanguage): String =
    when (language) {
        SupportLanguage.Ukrainian -> "$wordCount слів відкрито"
        SupportLanguage.Russian -> "$wordCount слов открыто"
    }

private fun unlockShortLabel(language: SupportLanguage): String =
    when (language) {
        SupportLanguage.Ukrainian -> "Відкрити 1 000"
        SupportLanguage.Russian -> "Открыть 1 000"
    }

private const val RecommendedLanguageCount = 3
private const val SearchLanguageThreshold = 5

@Preview
@Composable
private fun SettingsScreenPreview() {
    PolishThousandTheme {
        SettingsScreen(
            supportLanguage = SupportLanguage.Russian,
            hasPremium = false,
            completedLessonIds = setOf("lesson-1", "lesson-2"),
            learnedWords = 18
        )
    }
}
