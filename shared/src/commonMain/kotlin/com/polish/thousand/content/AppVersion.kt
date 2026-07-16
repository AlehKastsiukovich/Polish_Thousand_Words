package com.polish.thousand.content

import androidx.compose.runtime.Composable

internal data class AppVersion(
    val name: String,
    val build: String?
) {
    fun displayValue(): String = build
        ?.takeIf { it.isNotBlank() && it != name }
        ?.let { "$name ($it)" }
        ?: name
}

@Composable
internal expect fun rememberAppVersion(): AppVersion
