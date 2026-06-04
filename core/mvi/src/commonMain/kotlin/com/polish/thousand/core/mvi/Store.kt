package com.polish.thousand.core.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Store<S : UiState, I : UiIntent, E : UiEffect> {
    val uiState: StateFlow<S>
    val uiEffect: Flow<E>

    fun dispatchIntent(intent: I): Boolean
}
