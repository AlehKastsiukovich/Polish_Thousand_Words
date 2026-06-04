package com.polish.thousand.core.mvi

interface Middleware<S : UiState, I : UiIntent, E : UiEffect> {
    suspend fun processIntent(intent: I, stateAccessor: () -> S): I
}
