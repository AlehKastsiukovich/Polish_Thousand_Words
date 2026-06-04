package com.polish.thousand.core.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class IosAppDispatchers : AppDispatchers {
    override val io: CoroutineDispatcher = Dispatchers.Default
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val mainImmediate: CoroutineDispatcher = Dispatchers.Main.immediate
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

actual fun provideAppDispatchers(): AppDispatchers = IosAppDispatchers()
