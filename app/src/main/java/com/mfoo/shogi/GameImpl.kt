package com.mfoo.shogi

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.mfoo.shogi.kif.KifAst
import com.mfoo.shogi.kif.readKifFile


private typealias MoveNum = Int
private typealias Idx = Int

// Represents a variation consisting of moves and any branching points.
private data class Variation<T>(
    val moveNum: MoveNum, // move number of first move
    val moves: List<T>,
    val branches: Map<Idx, List<Variation<T>>>,
) {
    fun add(item: T): Variation<T> {
        return this.copy(moves = moves + item)
    }

    fun addBranch(branch: Variation<T>): Variation<T> {
        val idx = branch.moveNum - this.moveNum
        assert(idx >= 0)
        assert(idx < moves.size)
        val branchList = branches.getOrDefault(idx, emptyList())
        return this.copy(branches = branches + (idx to (branchList + branch)))
    }

    override fun toString(): String {
        val numberedMoves = moves.mapIndexed { idx, move ->
            "${idx + this.moveNum}: ${move}"
        }
        val variations = branches.map { (idx, variation) ->
            "\nBranch at ${idx + this.moveNum}: ${variation}"
        }
        return "${numberedMoves}${variations}"
    }
}

private data class Choice(val moveNumber: MoveNum, val choice: Int)

private data class CurrentLocation(
    val choices: ArrayDeque<Choice>,
    val moveNumber: MoveNum,
)

data class GameImpl private constructor(
    private val variations: Variation<Move>,
    private val path: CurrentLocation,
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
        override fun empty(): Game {
            return GameImpl(
                Variation(0, emptyList(), emptyMap()),
                CurrentLocation(ArrayDeque(), 0),
                PositionImpl.empty()
            )
        }

        override fun fromKifAst(kifAst: KifAst.Game): Game {
            val firstMoveNum =
                (kifAst.rootNode.children.firstOrNull()?.move?.moveNum ?: 1)
            val rootVariations = kifAst.rootNode.children.map {
                traverse(Variation(firstMoveNum, emptyList(), emptyMap()), it)
            }
            val mainVariation = if (rootVariations.isEmpty()) {
                Variation(firstMoveNum, emptyList(), emptyMap())
            } else {
                rootVariations
                    .subList(1, rootVariations.size)
                    .fold(rootVariations[0]) { a, v -> a.addBranch(v) }
            }
            val internal = playOut(
                mainVariation,
                kifAst.startPos.getSideToMove(),
                kifAst.startPos
            )
            return internal.fold({ println(it); GameImpl.empty() }) {
                GameImpl(
                    it,
                    CurrentLocation(ArrayDeque(), firstMoveNum),
                    kifAst.startPos as PositionImpl,
                )
            }
        }

        private fun moveFromKifAst(
            kifMove: KifAst.Move,
            startingMoveNum: Int,
            startingSide: Side,
            posBeforeMove: Position,
        ): Move {
            val side =
                getSideToMove(startingMoveNum, kifMove.moveNum, startingSide)
            return when (kifMove) {
                is KifAst.Move.Drop -> {
                    Move.Drop(kifMove.sq, side, kifMove.komaType)
                }

                is KifAst.Move.GameEnd -> {
                    val gameEndType = when (kifMove.endType) {
                        KifAst.Move.GameEndType.ABORT -> Move.GameEndType.ABORT
                        KifAst.Move.GameEndType.RESIGN -> Move.GameEndType.RESIGN
                        KifAst.Move.GameEndType.JISHOGI -> Move.GameEndType.JISHOGI
                        KifAst.Move.GameEndType.SENNICHITE -> Move.GameEndType.SENNICHITE
                        KifAst.Move.GameEndType.FLAG -> Move.GameEndType.FLAG
                        KifAst.Move.GameEndType.ILLEGAL_WIN -> Move.GameEndType.ILLEGAL_WIN
                        KifAst.Move.GameEndType.ILLEGAL_LOSS -> Move.GameEndType.ILLEGAL_LOSS
                        KifAst.Move.GameEndType.NYUUGYOKU -> Move.GameEndType.NYUUGYOKU
                        KifAst.Move.GameEndType.NO_CONTEST_WIN -> Move.GameEndType.NO_CONTEST_WIN
                        KifAst.Move.GameEndType.NO_CONTEST_LOSS -> Move.GameEndType.NO_CONTEST_LOSS
                        KifAst.Move.GameEndType.MATE -> Move.GameEndType.MATE
                        KifAst.Move.GameEndType.NO_MATE -> Move.GameEndType.NO_MATE
                    }
                    Move.GameEnd(gameEndType)
                }

                is KifAst.Move.Regular -> {
                    val capturedKoma =
                        posBeforeMove.getKoma(kifMove.endSq).getOrNull()
                    Move.Regular(
                        startSq = kifMove.startSq,
                        endSq = kifMove.endSq,
                        isPromotion = kifMove.isPromotion,
                        side = side,
                        komaType = kifMove.komaType,
                        capturedKoma = capturedKoma
                    )
                }
            }
        }

        private fun getSideToMove(
            startingMoveNum: Int,
            currentMoveNum: Int,
            startingSide: Side,
        ): Side {
            // Assumes that moves alternate between sente and gote with no deviation
            // (no passing moves, no game end, etc.)
            return if ((startingMoveNum - currentMoveNum) % 2 == 0) {
                startingSide
            } else {
                startingSide.switch()
            }
        }

        /**
         * Convert a Variation<KifAst.Move> to a Variation<Move>
         * by playing out and verifying the moves as legal.
         */
        private fun playOut(
            variation: Variation<KifAst.Move>,
            startingSide: Side,
            startPos: Position,
        ): Either<String, Variation<Move>> {
            return either {
                val moves =
                    legalMovesFromKifMoves(variation, startingSide, startPos)
                        .bind()
                val children = variation.branches.mapValues { (idx, children) ->
                    val sideToMove = getSideToMove(
                        variation.moveNum,
                        variation.moveNum + idx,
                        startingSide
                    )
                    val pos = moves
                        .take(idx)
                        .fold(startPos) { pos, move -> pos.doMove(move) }
                    children
                        .map { playOut(it, sideToMove, pos) }
                        .bindAll()
                }
                Variation(variation.moveNum, moves, children)
            }
        }

        private fun legalMovesFromKifMoves(
            variation: Variation<KifAst.Move>,
            startingSide: Side,
            startPos: Position,
        ): Either<String, List<Move>> {
            var pos = startPos
            val moveList = mutableListOf<Move>()
            for (kifMove in variation.moves) {
                val move = moveFromKifAst(
                    kifMove,
                    variation.moveNum,
                    startingSide,
                    pos
                )
                if (!isLegal(move, pos as PositionImpl)) {
                    return "Illegal move: ${move}".left()
                }
                pos = pos.doMove(move)
                moveList.add(move)
            }
            return moveList.right()
        }

        private fun traverse(
            acc: Variation<KifAst.Move>,
            node: KifAst.Tree.MoveNode,
        ): Variation<KifAst.Move> {
            if (node.children.isEmpty()) {
                return acc.add(node.move)
            }
            val mainVariation = traverse(
                acc.add(node.move), node.children[0]
            )
            return node.children.subList(1, node.children.size)
                .fold(mainVariation) { a, n ->
                    a.addBranch(
                        traverse(
                            Variation(
                                1 + node.move.moveNum,
                                emptyList(),
                                emptyMap()
                            ), n
                        )
                    )
                }
        }
    }
}

private fun main() {
    val game =
        GameImpl.fromKifAst(readKifFile("sample_problems/variations.kif")!!)
    println(game)
}
