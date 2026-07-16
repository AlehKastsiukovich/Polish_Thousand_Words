package com.polish.thousand.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberAppVersion(): AppVersion {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        AppVersion(
            name = packageInfo.versionName ?: "1.0",
            build = packageInfo.longVersionCode.toString()
        )
    }
}
