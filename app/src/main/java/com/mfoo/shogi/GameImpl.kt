package com.mfoo.shogi

import arrow.core.Either
import com.mfoo.shogi.kif.KifAst


private sealed interface MoveTree {
    class RootNode(val variations: List<Node>) : MoveTree
    class Node(val variations: List<Node>, val move: Move) : MoveTree
}

interface GameFactory {
    fun empty(): Game
    fun fromKifAst(kifAst: KifAst.Game): Game
}

class GameImpl private constructor (
    private val moveTree: MoveTree.RootNode,
    private val currNode: MoveTree,
    private val pos: PositionImpl,
) : Game {

    override fun addMove(move: Move): Game {
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
        override fun empty(): GameImpl {
            val moveTree = MoveTree.RootNode(emptyList())
            return GameImpl(
                moveTree = moveTree,
                currNode = moveTree,
                pos = PositionImpl.empty()
            )
        }

        override fun fromKifAst(kifAst: KifAst.Game): Game {
            val moveTree = MoveTree.RootNode(emptyList())
            return GameImpl(
                moveTree = moveTree,
                currNode = moveTree,
                pos = kifAst.startPos as PositionImpl
            )
        }
    }
}