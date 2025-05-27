package com.mfoo.sokuzumi

import com.mfoo.shogi.Game
import com.mfoo.shogi.PositionImpl
import com.mfoo.sokuzumi.position.PositionVM

class GameScreenVM(problemRepository: List<Game>) {
    private var game: Game = problemRepository.first()
    val posVM: PositionVM =
        PositionVM(game.getPosition() as PositionImpl)

    fun toUiState(): GameScreenUiState {
        return GameScreenUiState(
            posUi = posVM.toPositionUiState(),
            moveList = listOf("P-76", "P-34", "R-68", "Bx88+", "Sx")
        )
    }

    private fun updatePosition() {
        posVM.updatePosition(game.getPosition() as PositionImpl)
    }

    fun goForward() {
        game = game.advance().getOrNull() ?: return
        updatePosition()
    }

    fun goBackward() {
        game = game.retract().getOrNull() ?: return
        updatePosition()
    }

    fun goToStart() {
        game = game.goToStart()
        updatePosition()
    }

    fun goToEnd() {
        game = game.goToVariationEnd()
        updatePosition()
    }
}