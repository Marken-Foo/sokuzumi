package com.mfoo.sokuzumi

import com.mfoo.shogi.GameImpl
import com.mfoo.sokuzumi.position.PositionVM

class GameScreenVM(private var game: GameImpl) {
    private var posVM: PositionVM = PositionVM(game.currentPosition)

    fun toUiState(): GameScreenUiState {
        return GameScreenUiState(
            posUi = posVM.toPositionUiState(),
            moveList = listOf("P-76", "P-34", "R-68", "Bx88+", "Sx")
        )
    }

    fun goForward() {}
    fun goBackward() {}
    fun goToStart() {}
    fun goToEnd() {}
}