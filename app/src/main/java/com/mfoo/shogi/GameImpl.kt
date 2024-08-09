package com.mfoo.shogi

import arrow.core.Either
import com.mfoo.shogi.kif.KifAst


private sealed interface MoveTree {
    val variations: List<Node>

    class RootNode(override val variations: List<Node>) : MoveTree
    class Node(override val variations: List<Node>, val move: Move) : MoveTree
}

interface GameFactory {
    fun empty(): Game
    fun fromKifAst(kifAst: KifAst.Game): Game
}

class GameImpl private constructor(
    private val moveTree: MoveTree.RootNode,
    private val path: ArrayDeque<MoveTree.Node>,
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
                path = ArrayDeque(),
                pos = PositionImpl.empty()
            )
        }

        override fun fromKifAst(kifAst: KifAst.Game): Game {
            TODO("Not yet implemented")
//            // Serialize tree to list
//            kifAst.rootNode.fold(emptyList<KifAst.Tree>()) { acc, node ->
//                acc + node
//            }
//
//
//            fun convertMove(move: KifAst.Move, side: Side): Move {
//                return when (move) {
//                    is KifAst.Move.Drop -> Move.Drop(
//                        move.sq,
//                        side,
//                        move.komaType
//                    )
//
//                    is KifAst.Move.GameEnd -> Move.GameEnd(
//                        endType = when (move.endType) {
//                            KifAst.Move.GameEndType.ABORT -> Move.GameEndType.ABORT
//                            KifAst.Move.GameEndType.RESIGN -> Move.GameEndType.RESIGN
//                            KifAst.Move.GameEndType.JISHOGI -> Move.GameEndType.JISHOGI
//                            KifAst.Move.GameEndType.SENNICHITE -> Move.GameEndType.SENNICHITE
//                            KifAst.Move.GameEndType.FLAG -> Move.GameEndType.FLAG
//                            KifAst.Move.GameEndType.ILLEGAL_WIN -> Move.GameEndType.ILLEGAL_WIN
//                            KifAst.Move.GameEndType.ILLEGAL_LOSS -> Move.GameEndType.ILLEGAL_LOSS
//                            KifAst.Move.GameEndType.NYUUGYOKU -> Move.GameEndType.NYUUGYOKU
//                            KifAst.Move.GameEndType.NO_CONTEST_WIN -> Move.GameEndType.NO_CONTEST_WIN
//                            KifAst.Move.GameEndType.NO_CONTEST_LOSS -> Move.GameEndType.NO_CONTEST_LOSS
//                            KifAst.Move.GameEndType.MATE -> Move.GameEndType.MATE
//                            KifAst.Move.GameEndType.NO_MATE -> Move.GameEndType.NO_MATE
//                        }
//                    )
//
//                    is KifAst.Move.Regular -> Move.Regular(
//                        startSq = move.startSq,
//                        endSq = move.endSq,
//                        isPromotion = move.isPromotion,
//                        side = side,
//                        komaType = move.komaType,
//                        capturedKoma = null
//                    )
//                }
//            }
//
//            fun convert(
//                kifAstNode: (KifAst.Tree),
//                newChildren: Collection<MoveTree>,
//            ): MoveTree {
//                val children = newChildren.filterIsInstance<MoveTree.Node>()
//                return when (kifAstNode) {
//                    is KifAst.Tree.MoveNode -> MoveTree.Node(
//                        children,
//                        move = kifAstNode.move
//                    )
//
//                    is KifAst.Tree.RootNode -> MoveTree.RootNode(children)
//                }
//            }
//
//            val moveTree = kifAst.rootNode.cata(::convert)
//            assert(moveTree is MoveTree.RootNode)
//            return GameImpl(
//                moveTree = moveTree as MoveTree.RootNode,
//                path = ArrayDeque(),
//                pos = kifAst.startPos as PositionImpl
//            )
        }
    }
}