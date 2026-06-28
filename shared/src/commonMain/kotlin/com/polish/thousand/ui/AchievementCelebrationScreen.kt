package com.polish.thousand.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polish.thousand.content.LearningMilestone
import com.polish.thousand.content.LearningPath
import com.polish.thousand.content.LearningTargetWords
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.celebrationContentFor
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

@Composable
internal fun AchievementCelebrationScreen(
    milestoneWordCount: Int,
    supportLanguage: SupportLanguage,
    onContinueClick: () -> Unit
) {
    val milestone = LearningPath.milestones
        .firstOrNull { it.wordCount == milestoneWordCount }
        ?: LearningPath.milestones.last()
    val celebrationContent = milestone.celebrationContentFor(supportLanguage)
    val spacing = MaterialTheme.appSpacing
    val accent = milestone.accentColor()
    val glow = accent.copy(alpha = 0.16f)
    val progress = milestone.wordCount.toFloat() / LearningTargetWords

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.appColors.heroStart,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                    )
                )
            )
            .padding(horizontal = spacing.screenHorizontal)
    ) {
        CelebrationGlow(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
                .size(220.dp),
            brush = Brush.radialGradient(
                colors = listOf(glow, Color.Transparent)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 18.dp, bottom = 106.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = celebrationContent.eyebrow,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.56f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = milestone.wordCount.toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 92.sp,
                    lineHeight = 82.sp,
                    letterSpacing = (-5.2).sp
                ),
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = milestoneWordLabel(supportLanguage),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.46f)
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = celebrationContent.headline,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 34.sp,
                    lineHeight = 34.sp,
                    letterSpacing = (-1.8).sp
                ),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = celebrationContent.message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 28.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${milestone.wordCount} / $LearningTargetWords",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
                )
                CelebrationProgressLine(progress = progress, accent = accent)
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.46f)
                )
            }
        }

        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(58.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = if (supportLanguage == SupportLanguage.Ukrainian) "Продовжити" else "Продолжить",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CelebrationGlow(
    modifier: Modifier,
    brush: Brush
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(brush)
    )
}

private fun LearningMilestone.accentColor(): Color = when (wordCount) {
    100 -> Color(0xFF2D6F67)
    250 -> Color(0xFFC9785C)
    500 -> Color(0xFF3478C5)
    750 -> Color(0xFF7B63D3)
    1000 -> Color(0xFFE8A54F)
    else -> Color(0xFF2D6F67)
}

private fun milestoneWordLabel(supportLanguage: SupportLanguage): String =
    if (supportLanguage == SupportLanguage.Ukrainian) "слів" else "слов"

@Composable
private fun CelebrationProgressLine(
    progress: Float,
    accent: Color
) {
    val colors = MaterialTheme.appColors
    val height = 6.dp
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
                    color = accent,
                    shape = MaterialTheme.shapes.extraLarge
                )
        )
    }
}

@Preview
@Composable
private fun AchievementCelebrationScreenPreview() {
    PolishThousandTheme {
        AchievementCelebrationScreen(
            milestoneWordCount = 500,
            supportLanguage = SupportLanguage.Ukrainian,
            onContinueClick = {}
        )
    }
}
