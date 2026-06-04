package com.polish.thousand.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class StoreViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialUiState: S,
    private val middlewares: List<Middleware<S, I, E>> = emptyList(),
    appDispatchers: AppDispatchers
) : ViewModel(), Store<S, I, E> {

    protected val storeScope: CoroutineScope =
        CoroutineScope(viewModelScope.coroutineContext + appDispatchers.mainImmediate)

    private val _uiState = MutableStateFlow(initialUiState)
    override val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEffect = Channel<E>(Channel.BUFFERED)
    override val uiEffect: Flow<E> = _uiEffect.receiveAsFlow()

    private val intentsToProcessFlow = MutableSharedFlow<I>(extraBufferCapacity = 64)

    init {
        storeScope.launch {
            intentsToProcessFlow
                .onEach { intent ->
                    var currentIntent = intent
                    for (middleware in middlewares) {
                        currentIntent = middleware.processIntent(currentIntent) { _uiState.value }
                    }
                    handleIntentAndReduce(currentIntent)
                }
                .collect()
        }
    }

    override fun dispatchIntent(intent: I): Boolean = intentsToProcessFlow.tryEmit(intent)

    protected abstract fun handleIntentAndReduce(intent: I)

    protected fun reduceState(reducer: (currentState: S) -> S) {
        _uiState.update(reducer)
    }

    protected fun sendEffect(effect: E) {
        storeScope.launch {
            _uiEffect.send(effect)
        }
    }

    protected fun reduceSync(processor: S.() -> S) = reduceState(processor)

    protected fun <R> reduceAsync(
        onLoading: ((currentState: S) -> S)? = null,
        operation: suspend () -> R,
        onSuccess: (currentState: S, result: R) -> S,
        onError: ((currentState: S, error: Throwable) -> S)? = null
    ) {
        storeScope.launch {
            onLoading?.let { reduceState(it) }
            try {
                val result = operation()
                reduceState { currentState -> onSuccess(currentState, result) }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                onError?.let { reduceState { currentState -> it(currentState, throwable) } }
                    ?: reduceState { currentState -> currentState }
            }
        }
    }
}
