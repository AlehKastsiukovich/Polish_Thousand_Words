package com.polish.thousand

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

fun MainViewController() = IOSViewModelStoreOwner().let { owner ->
    ComposeUIViewController {
        IOSApp(owner)
    }
}

@Composable
private fun IOSApp(owner: IOSViewModelStoreOwner) {
    DisposableEffect(owner) {
        onDispose(owner.viewModelStore::clear)
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        App()
    }
}

private class IOSViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}
