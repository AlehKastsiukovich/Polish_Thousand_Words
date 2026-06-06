package com.polish.thousand.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class PolishThousandSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val screenHorizontal: Dp = 24.dp,
    val screenVertical: Dp = 20.dp
)

internal val LocalPolishThousandSpacing = staticCompositionLocalOf { PolishThousandSpacing() }

@Suppress("UnusedReceiverParameter")
val MaterialTheme.appSpacing: PolishThousandSpacing
    @Composable
    get() = LocalPolishThousandSpacing.current
