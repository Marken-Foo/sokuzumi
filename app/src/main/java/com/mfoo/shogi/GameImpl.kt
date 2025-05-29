package com.mfoo.shogi

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.mfoo.shogi.kif.KifAst
import com.mfoo.shogi.kif.readKifFile
import com.mfoo.shogi.kif.validateKif
import com.mfoo.shogi.rgbranches.RGBranches


class GameImpl private constructor(
    private val gameData: RGBranches<Move>,
    private val initialPosition: PositionImpl,
    val currentPosition: PositionImpl = initialPosition,
) : Game {
    override fun toString(): String {
        return gameData.toString()
    }

    private fun updateAndApplyMove(
        newData: RGBranches<Move>,
        move: Move,
    ): GameImpl {
        return GameImpl(
            gameData = newData,
            initialPosition = initialPosition,
            currentPosition = currentPosition.doMove(move)
        )
    }

    override fun addMove(move: Move): Either<GameError.IllegalMove, Game> {
        return if (isLegal(move, currentPosition)) {
            gameData.add(move)
                ?.let { updateAndApplyMove(it, move) }
                ?.right()
                ?: GameError.IllegalMove.left() // Should be error in .add()
        } else {
            GameError.IllegalMove.left()
        }
    }

    override fun advanceMove(move: Move): Either<GameError.NoSuchMove, Game> {
        return gameData.advanceIfPresent(move)
            ?.let { updateAndApplyMove(it, move) }
            ?.right()
            ?: GameError.NoSuchMove.left()
    }

    override fun advance(): Either<GameError.EndOfVariation, Game> {
        val newData = gameData.advance()
            ?: return GameError.EndOfVariation.left()
        val move = newData.getCurrentItem()
            ?: return GameError.EndOfVariation.left()
        return updateAndApplyMove(newData, move).right()
    }

    override fun retract(): Either<GameError.StartOfGame, Game> {
        val newData = gameData.retract()
            ?: return GameError.StartOfGame.left()
        val move = gameData.getCurrentItem()
            ?: return GameError.StartOfGame.left()
        return GameImpl(
            newData,
            initialPosition = initialPosition,
            currentPosition = currentPosition.undoMove(move)
        ).right()
    }

    override fun goToStart(): Game {
        return GameImpl(gameData.goToStart(), initialPosition)
    }

    override fun goToVariationEnd(): Game {
        return GameImpl(
            gameData = gameData.goToEnd(),
            initialPosition = initialPosition,
            currentPosition = gameData.getItemsToEnd()
                .fold(currentPosition) { pos, move -> pos.doMove(move) })
    }

    override fun getPosition(): Position = currentPosition

    override fun isAtVariationEnd(): Boolean {
        return gameData.isAtEnd()
    }

    override fun getMainlineMove(): Move? {
        return gameData.getNextItem()
    }

    override fun getMainline(): List<Move> {
        return gameData.goToStart().getItemsToEnd()
    }

    companion object : GameFactory {
        override fun empty(): Game {
            return GameImpl(RGBranches.empty(), PositionImpl.empty())
        }

        override fun fromPos(pos: Position): Game {
            return GameImpl(RGBranches.empty(), pos as PositionImpl)
        }

        override fun fromKifAst(kifAst: KifAst.Game<KifAst.Move>): Game {
            return kifAst
                .let(::validateKif)
                .let { RGBranches.fromTree(it.rootNode) }
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
