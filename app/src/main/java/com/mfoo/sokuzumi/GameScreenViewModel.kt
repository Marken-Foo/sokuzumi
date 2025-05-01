package com.mfoo.sokuzumi

import androidx.lifecycle.ViewModel
import com.mfoo.shogi.GameImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameScreenViewModel(): ViewModel() {
    private val _vm = GameScreenVM(GameImpl.empty() as GameImpl)
    private val _uiState: MutableStateFlow<GameScreenUiState> = MutableStateFlow(_vm.toUiState())
    val uiState: StateFlow<GameScreenUiState> = _uiState.asStateFlow()
}