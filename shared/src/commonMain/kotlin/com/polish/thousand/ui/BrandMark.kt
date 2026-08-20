package com.polish.thousand.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import polishthousand.shared.generated.resources.Res
import polishthousand.shared.generated.resources.mow_1000_brand_mark

@Composable
internal fun PolishThousandBrandMark(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(Res.drawable.mow_1000_brand_mark),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier.fillMaxSize()
    )
}
