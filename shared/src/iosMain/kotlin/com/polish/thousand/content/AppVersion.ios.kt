package com.polish.thousand.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle

@Composable
internal actual fun rememberAppVersion(): AppVersion = remember {
    val bundle = NSBundle.mainBundle
    AppVersion(
        name = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0",
        build = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
    )
}
