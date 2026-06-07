package com.polish.thousand.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
internal fun LessonHeader(
    overline: String,
    title: String,
    backText: String = "Back",
    onBackClick: () -> Unit
) {
    AppTopBar(
        title = title,
        overline = overline,
        onBackClick = onBackClick
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun AppTopBar(
    title: String,
    overline: String? = null,
    onBackClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            if (overline == null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = overline,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    BackArrowIcon()
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        windowInsets = TopAppBarDefaults.windowInsets
    )
}

@Composable
private fun BackArrowIcon() {
    val arrowColor = MaterialTheme.colorScheme.onBackground
    Canvas(modifier = Modifier.size(24.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        val midY = size.height / 2f
        val startX = size.width * 0.22f
        val endX = size.width * 0.82f

        drawLine(
            color = arrowColor,
            start = androidx.compose.ui.geometry.Offset(endX, midY),
            end = androidx.compose.ui.geometry.Offset(startX, midY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = arrowColor,
            start = androidx.compose.ui.geometry.Offset(startX, midY),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.46f, size.height * 0.24f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = arrowColor,
            start = androidx.compose.ui.geometry.Offset(startX, midY),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.46f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
internal fun LessonPill(text: String) {
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
internal fun LessonGlow(
    modifier: Modifier,
    brush: Brush
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(brush)
    )
}
