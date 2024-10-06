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
        TODO("Not yet implemented")
    }

    override fun advance(): Either<GameError.EndOfVariation, Game> {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun getMainlineMove(): Move? {
        TODO("Not yet implemented")
    }

    companion object : GameFactory {
        override fun empty(): Game {
            TODO("Not yet implemented")
        }

        override fun fromKifAst(kifAst: KifAst.Game<KifAst.Move>): Game {
            val redGreenBranches = kifAst
                .let(::validateKif)
                .let { RedGreenBranches.fromTree(it.rootNode) }
            return GameImpl(
                redGreenBranches,
                kifAst.startPos as PositionImpl,
            )
        }
    }
}

private fun main() {
    val game =
        GameImpl.fromKifAst(readKifFile("sample_problems/variations.kif")!!)
    println(game)
//    println(game.goToVariationEnd().isAtVariationEnd())
}
