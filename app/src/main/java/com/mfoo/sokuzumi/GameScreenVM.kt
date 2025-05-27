package com.mfoo.sokuzumi

import com.mfoo.shogi.Game
import com.mfoo.shogi.GameImpl
import com.mfoo.shogi.PositionImpl
import com.mfoo.sokuzumi.position.PositionVM

class GameScreenVM(problemRepository: List<GameImpl>) {
    private var game: Game = problemRepository.first()
    private var posVM: PositionVM =
        PositionVM(game.getPosition() as PositionImpl)

    fun toUiState(): GameScreenUiState {
        return GameScreenUiState(
            posUi = posVM.toPositionUiState(),
            moveList = listOf("P-76", "P-34", "R-68", "Bx88+", "Sx")
        )
    }

    fun goForward() {
        game = game.advance().getOrNull() ?: return
        posVM.updatePosition(game.getPosition() as PositionImpl)
    }

    fun goBackward() {
        game = game.retract().getOrNull() ?: return
        posVM.updatePosition(game.getPosition() as PositionImpl)
    }

    fun goToStart() {
        game = game.goToStart()
        posVM.updatePosition(game.getPosition() as PositionImpl)
    }

    fun goToEnd() {
        game = game.goToVariationEnd()
        posVM.updatePosition(game.getPosition() as PositionImpl)
    }
}