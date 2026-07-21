package com.polish.thousand

import androidx.compose.runtime.Composable

@Composable
internal expect fun AppForegroundEffect(onForeground: () -> Unit)
