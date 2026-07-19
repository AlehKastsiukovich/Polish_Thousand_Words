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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.polish.thousand.content.SupportLanguage
import com.polish.thousand.content.appText
import com.polish.thousand.content.paywallTitle
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors
import com.polish.thousand.core.designsystem.appSpacing

@Composable
internal fun SoftPaywallScreen(
    supportLanguage: SupportLanguage = SupportLanguage.Ukrainian,
    completedLessons: Int,
    productPrice: String? = null,
    isStoreConfigured: Boolean = false,
    isPaymentInProgress: Boolean = false,
    paymentMessage: String? = null,
    onUnlockClick: () -> Unit = {},
    onRestoreClick: () -> Unit = {},
    onPaymentMessageClick: () -> Unit = {},
    onContinueFreeClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    val spacing = MaterialTheme.appSpacing
    val colors = MaterialTheme.appColors
    val text = supportLanguage.appText
    val unlockButtonText = productPrice
        ?.takeIf { it.isNotBlank() }
        ?.let { "${text.unlockFullCourse} · $it" }
        ?: text.unlockFullCourse

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        colors.heroStart,
                        colors.heroEnd.copy(alpha = 0.74f)
                    )
                )
            )
    ) {
        LessonGlow(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 64.dp, y = (-8).dp)
                .size(220.dp),
            brush = Brush.radialGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0f)
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
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onCloseClick,
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
                    ) {
                        Text(
                            text = text.keepMomentum,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                    ) {
                        Text(
                            text = text.later,
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = supportLanguage.paywallTitle(completedLessons),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = text.paywallDescription,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.76f)
                )

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
                ) {
                    Column(
                        modifier = Modifier.padding(spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        PaywallBenefit(text.paywallBenefitOne)
                        PaywallBenefit(text.paywallBenefitTwo)
                        PaywallBenefit(text.paywallBenefitThree)
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = if (isStoreConfigured) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(spacing.xl),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = if (isStoreConfigured) text.paywallSoftNote else text.storeSetupRequired,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                if (paymentMessage != null) {
                    Surface(
                        onClick = onPaymentMessageClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
                    ) {
                        Text(
                            text = paymentMessage,
                            modifier = Modifier.padding(spacing.md),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                    }
                }

                Button(
                    onClick = onUnlockClick,
                    enabled = !isPaymentInProgress,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = unlockButtonText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onRestoreClick,
                    enabled = !isPaymentInProgress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = text.restorePurchases,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Button(
                        onClick = onContinueFreeClick,
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = null
                    ) {
                        Text(
                            text = text.continueFree,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Text(
                    text = text.paywallFooter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f)
                )
            }
        }
    }
}

@Composable
private fun PaywallBenefit(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
private fun SoftPaywallScreenPreview() {
    PolishThousandTheme {
        SoftPaywallScreen(
            supportLanguage = SupportLanguage.Ukrainian,
            completedLessons = 2
        )
    }
}
