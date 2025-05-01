package com.mfoo.shogi

import arrow.core.Either
import com.mfoo.shogi.kif.KifAst


sealed interface GameError {
    data object StartOfGame : GameError
    data object IllegalMove : GameError
    data object NoSuchMove: GameError
    data object EndOfVariation : GameError
}

interface Game {
    // Construction
    fun addMove(move: Move): Either<GameError.IllegalMove, Game>

    // Navigation
    fun advanceMove(move: Move): Either<GameError.NoSuchMove, Game>
    fun advance(): Either<GameError.EndOfVariation, Game>
    fun retract(): Either<GameError.StartOfGame, Game>
    fun goToStart(): Game
    fun goToVariationEnd(): Game

    // Queries
    fun isAtVariationEnd(): Boolean
    fun getMainlineMove(): Move?
}

interface GameFactory {
    fun empty(): Game
    fun fromKifAst(kifAst: KifAst.Game<KifAst.Move>): Game
}
