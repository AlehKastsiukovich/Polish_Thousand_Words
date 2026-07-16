package com.polish.thousand.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import polishthousand.shared.generated.resources.Res
import polishthousand.shared.generated.resources.mow_1000_brand_mark

@Composable
internal fun PolishThousandBrandMark(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 24))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF214A82),
                        Color(0xFF173B70),
                        Color(0xFF102E59)
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(Res.drawable.mow_1000_brand_mark),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
