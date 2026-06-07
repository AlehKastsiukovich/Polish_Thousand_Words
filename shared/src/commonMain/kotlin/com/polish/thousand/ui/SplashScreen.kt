package com.polish.thousand.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.polish.thousand.core.designsystem.PolishThousandTheme
import com.polish.thousand.core.designsystem.appColors

@Composable
internal fun SplashScreen() {
    val colors = MaterialTheme.appColors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GlowOrb(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(220.dp),
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.heroEnd.copy(alpha = 0.18f),
                    colors.heroEnd.copy(alpha = 0f)
                )
            )
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PolishThousandBrandMark(modifier = Modifier.size(184.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "PolishThousand",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Practical Polish",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.52f)
            )
        }
    }
}

@Composable
private fun GlowOrb(
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
private fun SplashScreenPreview() {
    PolishThousandTheme {
        SplashScreen()
    }
}
