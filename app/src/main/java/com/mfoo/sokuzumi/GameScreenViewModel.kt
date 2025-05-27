package com.mfoo.sokuzumi

import androidx.lifecycle.ViewModel
import com.mfoo.shogi.GameImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameScreenViewModel() : ViewModel() {
    private val _vm = GameScreenVM(listOf(GameImpl.empty() as GameImpl))
    private val _uiState: MutableStateFlow<GameScreenUiState> =
        MutableStateFlow(_vm.toUiState())
    val uiState: StateFlow<GameScreenUiState> = _uiState.asStateFlow()

    private fun refresh() {
        _uiState.update { _vm.toUiState() }
    }

    fun goToStart() {
        _vm.goToStart()
        refresh()
    }

    fun goBackward() {
        _vm.goBackward()
        refresh()
    }

    fun goForward() {
        _vm.goForward()
        refresh()
    }

    fun goToEnd() {
        _vm.goToEnd()
        refresh()
    }
}