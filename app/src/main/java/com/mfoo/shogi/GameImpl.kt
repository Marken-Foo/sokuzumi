package com.mfoo.shogi

import arrow.core.Either
import com.mfoo.shogi.kif.KifAst
import com.mfoo.shogi.kif.readKifFile
import com.mfoo.shogi.kif.validateKif


class GameImpl private constructor(
    private val gameData: GreenBranch<Move>,
    private val currentLocation: RedBranch<Move>,
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
            val greenBranchRoot = kifAst
                .let(::validateKif)
                .let(::greenBranchFromTree)
            val emptyPath = Path.Terminal(ItemIdx(0))
            return GameImpl(
                greenBranchRoot,
                RedBranch(greenBranchRoot, null, emptyPath),
                kifAst.startPos as PositionImpl
            )
        }

        private fun <T> greenBranchFromTree(kifGame: KifAst.Game<T>): GreenBranch<T> {
            return if (kifGame.rootNode.children.isEmpty()) {
                GreenBranch(emptyList(), emptyMap())
            } else {
                val mainBranch = traverse(kifGame.rootNode.children[0])
                kifGame.rootNode.children.let { it.subList(1, it.size) }
                    .fold(mainBranch) { b, n ->
                        b.addBranch(traverse(n), Path.Terminal(ItemIdx(0))) ?: b
                    }
            }
        }

        private fun <T> traverse(
            node: KifAst.Tree.MoveNode<T>,
            idxInBranch: ItemIdx = ItemIdx(0),
            mainlineMoves: List<T> = emptyList(),
        ): GreenBranch<T> {
            if (node.children.isEmpty()) {
                return GreenBranch(mainlineMoves + node.move, emptyMap())
            }
            val branchOfCurrentNode = traverse(
                node.children[0],
                idxInBranch.increment(),
                mainlineMoves + node.move,
            )
            return node.children.subList(1, node.children.size)
                .fold(branchOfCurrentNode) { b, n ->
                    b.addBranch(
                        traverse(n),
                        Path.Terminal(idxInBranch.increment())
                    ) ?: b
                }
        }
    }
}

private fun main() {
    val game =
        GameImpl.fromKifAst(readKifFile("sample_problems/variations.kif")!!)
    println(game)
//    println(game.goToVariationEnd().isAtVariationEnd())
}
