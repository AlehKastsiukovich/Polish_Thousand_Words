package com.polish.thousand.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.polish.thousand.core.designsystem.PolishThousandTheme

private val BrandTeal = Color(0xFF2D6F67)
private val BrandMintTint = Color(0xFFEAF4F0)
private val BrandCream = Color(0xFFFFFDF8)

@Composable
internal fun SplashScreen() {
    val entrance = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandCream,
                        Color(0xFFF4FAF7),
                        BrandMintTint
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(360.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandTeal.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(percent = 50)
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = entrance.value
                    scaleX = 0.92f + entrance.value * 0.08f
                    scaleY = 0.92f + entrance.value * 0.08f
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PolishThousandBrandMark(
                modifier = Modifier
                    .size(184.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(percent = 24),
                        ambientColor = BrandTeal.copy(alpha = 0.08f),
                        spotColor = BrandTeal.copy(alpha = 0.14f)
                    )
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = buildAnnotatedString {
                    append("Mów ")
                    withStyle(SpanStyle(color = BrandTeal)) {
                        append("1000")
                    }
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = BrandTeal
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "1000 słów do B1",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = BrandTeal.copy(alpha = 0.68f)
            )
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    PolishThousandTheme {
        SplashScreen()
    }
}
