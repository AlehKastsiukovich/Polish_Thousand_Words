package com.polish.thousand.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.rememberTextMeasurer
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
    onContinueClick: () -> Unit,
    onHomeClick: () -> Unit = {}
) {
    if (milestoneWordCount >= LearningTargetWords) {
        FinalAchievementScreen(
            supportLanguage = supportLanguage,
            onHomeClick = onHomeClick
        )
        return
    }

    val milestone = LearningPath.milestones
        .firstOrNull { it.wordCount == milestoneWordCount }
        ?: LearningPath.milestones.last()
    val celebrationContent = milestone.celebrationContentFor(supportLanguage)
    val spacing = MaterialTheme.appSpacing
    val accent = milestone.accentColor()
    val semanticAccent = MaterialTheme.colorScheme.primary
    val glow = accent.copy(alpha = 0.16f)

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
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 10.dp)
                .size(180.dp),
            brush = Brush.radialGradient(
                colors = listOf(glow, Color.Transparent)
            )
        )

        CelebrationGlow(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 160.dp)
                .size(120.dp),
            brush = Brush.radialGradient(
                colors = listOf(
                    MaterialTheme.appColors.heroGlow.copy(alpha = 0.18f),
                    Color.Transparent
                )
            )
        )

        Column(
            modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 12.dp, bottom = 92.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            JourneyProgressRing(
                currentMilestone = milestone,
                supportLanguage = supportLanguage,
                modifier = Modifier.size(320.dp)
            )

            Text(
                text = celebrationContent.eyebrow.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    letterSpacing = 1.4.sp
                ),
                fontWeight = FontWeight.Bold,
                color = semanticAccent
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = celebrationContent.headline,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 36.sp,
                    lineHeight = 38.sp,
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
                    lineHeight = 27.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )

            Spacer(modifier = Modifier.weight(1f))
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
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun JourneyProgressRing(
    currentMilestone: LearningMilestone,
    supportLanguage: SupportLanguage,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val trackColor = Color(0xFFE8E4DE)
    val labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
    val centerLabelColor = MaterialTheme.colorScheme.onBackground
    val accent = currentMilestone.accentColor()
    val milestones = LearningPath.milestones
    val stageColors = milestones.map { it.accentColor() }
    val boundaries = listOf(0) + milestones.map { it.wordCount }
    val milestoneLabelStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.ExtraBold,
        color = labelColor
    )
    val milestoneAngles = mapOf(
        100 to -54f,
        250 to 0f,
        500 to 90f,
        750 to 180f,
        1000 to 270f
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val ringRadius = size.minDimension * 0.35f
            val labelRadius = ringRadius + size.minDimension * 0.09f
            val strokeWidth = size.minDimension * 0.047f

            drawCircle(
                color = trackColor,
                radius = ringRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            val gapDegrees = 2.4f
            stageColors.forEachIndexed { index, color ->
                val stageStart = boundaries[index]
                val stageEnd = boundaries[index + 1]
                val stageLength = stageEnd - stageStart
                val fullSweep = (360f * stageLength / LearningTargetWords - gapDegrees)
                    .coerceAtLeast(0f)
                val startAngle = -90f + 360f * stageStart / LearningTargetWords + gapDegrees / 2f

                drawArc(
                    color = color.copy(alpha = 0.20f),
                    startAngle = startAngle,
                    sweepAngle = fullSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                    size = androidx.compose.ui.geometry.Size(ringRadius * 2f, ringRadius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val completedInStage = (currentMilestone.wordCount - stageStart)
                    .coerceIn(0, stageLength)
                if (completedInStage > 0) {
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = fullSweep * completedInStage / stageLength,
                        useCenter = false,
                        topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                        size = androidx.compose.ui.geometry.Size(ringRadius * 2f, ringRadius * 2f),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            milestones.forEachIndexed { index, milestone ->
                val angle = milestoneAngles.getValue(milestone.wordCount)
                val radians = angle * (kotlin.math.PI / 180.0)
                val direction = Offset(
                    x = kotlin.math.cos(radians).toFloat(),
                    y = kotlin.math.sin(radians).toFloat()
                )
                val markerCenter = center + direction * ringRadius
                val isCurrent = milestone.wordCount == currentMilestone.wordCount
                val isCompleted = milestone.wordCount < currentMilestone.wordCount
                val markerColor = when {
                    isCurrent -> accent
                    isCompleted -> stageColors[index]
                    else -> stageColors[index].copy(alpha = 0.52f)
                }

                if (isCurrent) {
                    drawCircle(
                        color = accent.copy(alpha = 0.16f),
                        radius = size.minDimension * 0.046f,
                        center = markerCenter
                    )
                }
                drawCircle(
                    color = markerColor,
                    radius = if (isCurrent) size.minDimension * 0.027f else size.minDimension * 0.021f,
                    center = markerCenter
                )
                drawCircle(
                    color = Color(0xFFFFFDFA),
                    radius = if (isCurrent) size.minDimension * 0.018f else size.minDimension * 0.013f,
                    center = markerCenter
                )
                drawCircle(
                    color = markerColor,
                    radius = if (isCurrent) size.minDimension * 0.013f else size.minDimension * 0.009f,
                    center = markerCenter
                )

                val labelLayout = textMeasurer.measure(
                    text = AnnotatedString(milestone.wordCount.toString()),
                    style = milestoneLabelStyle.copy(
                        color = if (isCurrent) accent else labelColor
                    )
                )
                val labelCenter = center + direction * labelRadius
                drawText(
                    textLayoutResult = labelLayout,
                    topLeft = Offset(
                        x = labelCenter.x - labelLayout.size.width / 2f,
                        y = labelCenter.y - labelLayout.size.height / 2f
                    )
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentMilestone.wordCount.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 66.sp,
                    lineHeight = 64.sp,
                    letterSpacing = (-3.2).sp
                ),
                fontWeight = FontWeight.ExtraBold,
                color = centerLabelColor
            )
            Text(
                text = if (supportLanguage == SupportLanguage.Ukrainian) {
                    "із 1 000 слів"
                } else {
                    "из 1 000 слов"
                },
                style = MaterialTheme.typography.labelMedium,
                color = labelColor
            )
        }
    }
}

@Composable
private fun FinalAchievementScreen(
    supportLanguage: SupportLanguage,
    onHomeClick: () -> Unit
) {
    val spacing = MaterialTheme.appSpacing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.appColors.heroStart.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = spacing.screenHorizontal)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 98.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            JourneyProgressRing(
                currentMilestone = LearningPath.milestones.last(),
                supportLanguage = supportLanguage,
                modifier = Modifier.size(304.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (supportLanguage == SupportLanguage.Ukrainian) {
                    "ШЛЯХ ПРОЙДЕНО"
                } else {
                    "ПУТЬ ПРОЙДЕН"
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    letterSpacing = 1.4.sp
                ),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (supportLanguage == SupportLanguage.Ukrainian) {
                    "1 000 слів —\nваша B1 база."
                } else {
                    "1 000 слов —\nваша B1 база."
                },
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 34.sp,
                    lineHeight = 36.sp,
                    letterSpacing = (-1.8).sp
                ),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (supportLanguage == SupportLanguage.Ukrainian) {
                    "Тепер просто повертайтеся до слів, коли зручно."
                } else {
                    "Теперь просто возвращайтесь к словам, когда удобно."
                },
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.sp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.lg),
                text = if (supportLanguage == SupportLanguage.Ukrainian) {
                    "• Повторення з'являться тут, коли слова знову потребуватимуть вашої уваги."
                } else {
                    "• Повторения появятся здесь, когда словам снова понадобится ваше внимание."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 21.sp
                ),
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onHomeClick,
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
                    text = if (supportLanguage == SupportLanguage.Ukrainian) {
                        "На головний екран"
                    } else {
                        "На главный экран"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
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

@Composable
private fun LearningMilestone.accentColor(): Color {
    val colors = MaterialTheme.appColors
    return when (wordCount) {
        100 -> colors.milestoneWarmup
        250 -> colors.milestoneBase
        500 -> colors.milestoneConfidence
        750 -> colors.milestoneMomentum
        1000 -> colors.milestoneMastery
        else -> colors.milestoneWarmup
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
