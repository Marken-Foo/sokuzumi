package com.mfoo.shogi

import arrow.core.Either
import com.mfoo.shogi.kif.KifAst
import com.mfoo.shogi.kif.readKifFile
import com.mfoo.shogi.kif.validateKif


class GameImpl private constructor(
    private val gameData: RedGreenBranches<Move>,
    private val currentPosition: PositionImpl,
) : Game {
    override fun toString(): String {
        return gameData.toString()
    }

    override fun addMove(move: Move): Either<GameError.IllegalMove, Game> {
        TODO("Not yet implemented")
    }

    override fun advanceMove(move: Move): Either<GameError.NoSuchMove, Game> {
        val newData = gameData.advanceIfPresent(move)
            ?: return Either.Left(GameError.NoSuchMove)
        return Either.Right(GameImpl(newData, currentPosition.doMove(move)))
    }

    override fun advance(): Either<GameError.EndOfVariation, Game> {
        val newData = gameData.advance()
            ?: return Either.Left(GameError.EndOfVariation)
        val move = newData.getCurrentItem()
            ?: return Either.Left(GameError.EndOfVariation)
        return Either.Right(GameImpl(newData, currentPosition.doMove(move)))
    }

    override fun retract(): Either<GameError.StartOfGame, Game> {
        TODO("Not yet implemented")
    }

    override fun goToStart(): Game {
        TODO("Not yet implemented")
    }

    override fun goToVariationEnd(): Game {
        TODO("Not yet implemented")
    }

    override fun isAtVariationEnd(): Boolean {
        return gameData.isAtLeaf()
    }

    override fun getMainlineMove(): Move? {
        return gameData.getNextItem()
    }

    companion object : GameFactory {
        override fun empty(): Game {
            TODO("Not yet implemented")
        }

        override fun fromKifAst(kifAst: KifAst.Game<KifAst.Move>): Game {
            return kifAst
                .let(::validateKif)
                .let { RedGreenBranches.fromTree(it.rootNode) }
                .let { GameImpl(it, kifAst.startPos as PositionImpl) }
        }
    }
}

private fun main() {
    val game =
        GameImpl.fromKifAst(readKifFile("sample_problems/variations.kif")!!)
    println(game)
//    println(game.goToVariationEnd().isAtVariationEnd())
}
