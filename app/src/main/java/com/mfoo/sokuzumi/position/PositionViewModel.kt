package com.mfoo.sokuzumi.position

import androidx.lifecycle.ViewModel
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.PositionImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


// For Android lifecycle persistence across configuration change
class PositionViewModel(initialPos: PositionImpl = PositionImpl.empty()) :
    ViewModel() {

    private val _vm = PositionVM(initialPos)
    private val _uiState: MutableStateFlow<PosUiState> =
        MutableStateFlow(_vm.toPositionUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    private fun refresh() {
        _uiState.update { _vm.toPositionUiState() }
    }

    fun cancelSelection() {
        _vm.cancelSelection()
        refresh()
    }

    fun onSquareClick(x: Int, y: Int) {
        _vm.onSquareClick(x, y)
        refresh()
    }

    fun onSenteHandClick(komaType: KomaType) {
        _vm.onSenteHandClick(komaType)
        refresh()
    }

    fun onGoteHandClick(komaType: KomaType) {
        _vm.onGoteHandClick(komaType)
        refresh()
    }

    fun onPromote() {
        _vm.onPromote()
        refresh()
    }

    fun onUnpromote() {
        _vm.onUnpromote()
        refresh()
    }
}
