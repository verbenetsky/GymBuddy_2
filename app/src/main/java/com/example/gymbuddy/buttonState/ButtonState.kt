package com.example.gymbuddy.buttonState

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ButtonStateManager {
    private val _buttonState = MutableStateFlow("Send Request")
    val buttonState: StateFlow<String> = _buttonState.asStateFlow()

    fun updateState(newState: String) {
        _buttonState.value = newState
    }
}
