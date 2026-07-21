package com.polish.thousand

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification

@Composable
internal actual fun AppForegroundEffect(onForeground: () -> Unit) {
    val currentOnForeground = rememberUpdatedState(onForeground)

    DisposableEffect(Unit) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null
        ) {
            currentOnForeground.value()
        }
        onDispose { NSNotificationCenter.defaultCenter.removeObserver(observer) }
    }
}
