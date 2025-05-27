package com.mfoo.sokuzumi

import androidx.lifecycle.ViewModel
import com.mfoo.shogi.Game
import com.mfoo.shogi.GameImpl
import com.mfoo.shogi.KomaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameScreenViewModel(problems: List<Game>) : ViewModel() {
    private val gameScreen = GameScreenVM(problems)
    private val _uiState: MutableStateFlow<GameScreenUiState> =
        MutableStateFlow(gameScreen.toUiState())
    val uiState: StateFlow<GameScreenUiState> = _uiState.asStateFlow()

    private fun refresh() {
        _uiState.update { gameScreen.toUiState() }
    }

    fun goToStart() {
        gameScreen.goToStart()
        refresh()
    }

    fun goBackward() {
        gameScreen.goBackward()
        refresh()
    }

    fun goForward() {
        gameScreen.goForward()
        refresh()
    }

    fun goToEnd() {
        gameScreen.goToEnd()
        refresh()
    }


    fun cancelSelection() {
        gameScreen.posVM.cancelSelection()
        refresh()
    }

    fun onSquareClick(x: Int, y: Int) {
        gameScreen.posVM.onSquareClick(x, y)
        refresh()
    }

    fun onSenteHandClick(komaType: KomaType) {
        gameScreen.posVM.onSenteHandClick(komaType)
        refresh()
    }

    fun onGoteHandClick(komaType: KomaType) {
        gameScreen.posVM.onGoteHandClick(komaType)
        refresh()
    }

    fun onPromote() {
        gameScreen.posVM.onPromote()
        refresh()
    }

    fun onUnpromote() {
        gameScreen.posVM.onUnpromote()
        refresh()
    }
}