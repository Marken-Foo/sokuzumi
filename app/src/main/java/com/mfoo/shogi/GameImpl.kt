package com.mfoo.shogi

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.mfoo.shogi.kif.KifAst
import com.mfoo.shogi.kif.readKifFile


private typealias MoveNum = Int
private typealias Idx = Int

private sealed interface Path {
    /**
     * One-indexed
     */
    val idx: ItemIdx

    data class Terminal(override val idx: ItemIdx) : Path
    data class Segment(
        override val idx: ItemIdx,
        val branchIdx: Int,
        val next: Path,
    ) : Path
}

/**
 * Represents the 1-indexed position of an item within a branch.
 */
@JvmInline
private value class ItemIdx(val t: Int) {
    fun increment(): ItemIdx {
        return ItemIdx(this.t + 1)
    }
}

/**
 * Local branch structure, unaware of global position.
 */
private data class GreenBranch<T>(
    val items: List<T>,
    val children: Map<ItemIdx, List<GreenBranch<T>>>,
) {
    private fun <S> List<S>.replaceAt(item: S, idx: Int): List<S>? {
        return if (idx < 0 || this.size <= idx) {
            null
        } else {
            this.slice(0..<idx) + item + this.slice(idx + 1..<this.size)
        }
    }

    private fun updateBranch(
        segment: Path.Segment,
        newBranch: GreenBranch<T>,
    ): GreenBranch<T>? {
        return children[segment.idx]
            ?.replaceAt(newBranch, segment.branchIdx)
            ?.let { this.copy(children = children + (segment.idx to it)) }
    }

    private fun isItemIndexValid(idx: ItemIdx): Boolean {
        return 1 <= idx.t && idx.t <= items.size
    }

    fun add(item: T, path: Path): GreenBranch<T>? {
        when (path) {
            is Path.Segment -> {
                return children[path.idx]
                    ?.getOrNull(path.branchIdx)
                    ?.add(item, path.next)
                    ?.let { updateBranch(path, it) }
            }

            is Path.Terminal -> {
                return if (path.idx.t == items.size + 1) {
                    this.copy(items = items + item)
                } else if (isItemIndexValid(path.idx)) {
                    val branchList =
                        children.getOrDefault(path.idx, emptyList())
                    val newBranch = GreenBranch(listOf(item), emptyMap())
                    this.copy(children = children + (path.idx to (branchList + newBranch)))
                } else {
                    null
                }
            }
        }
    }

    fun addBranch(branch: GreenBranch<T>, path: Path): GreenBranch<T>? {
        if (branch.items.isEmpty()) {
            return null
        }
        when (path) {
            is Path.Segment -> {
                return children[path.idx]
                    ?.getOrNull(path.branchIdx)
                    ?.addBranch(branch, path.next)
                    ?.let { updateBranch(path, it) }
            }

            is Path.Terminal -> {
                if (!isItemIndexValid(path.idx)) {
                    return null
                }
                val newBranchList = children[path.idx]
                    ?.let { it + branch }
                    ?: listOf(branch)
                return this.copy(children = children + (path.idx to newBranchList))
            }
        }
    }
}

private data class RedBranch<T>(
    private val value: GreenBranch<T>,
    private val parent: RedBranch<T>?,
    private val path: Path,
)

class GameImpl private constructor(
    private val gameData: GreenBranch<Move>,
    private val currentLocation: RedBranch<Move>,
    private val currentPosition: PositionImpl,
) : Game {
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
            TODO("Not yet implemented")
            // Convert KifAst to red-green branches.
            fun traverse(
                idxInBranch: ItemIdx,
                mainlineMoves: List<KifAst.Move>,
                node: KifAst.Tree.MoveNode<KifAst.Move>,
            ): GreenBranch<KifAst.Move> {
                if (node.children.isEmpty()) {
                    return GreenBranch(mainlineMoves + node.move, emptyMap())
                }
                val branchOfCurrentNode = traverse(
                    idxInBranch.increment(),
                    mainlineMoves + node.move,
                    node.children[0]
                )
                return node.children.subList(1, node.children.size)
                    .fold(branchOfCurrentNode) { b, n ->
                        traverse(
                            idxInBranch.increment(),
                            emptyList(),
                            n
                        ).let {
                            b.addBranch(
                                it,
                                Path.Terminal(idxInBranch.increment())
                            )
                        } ?: b
                    }
            }

            val greenKifAst = if (kifAst.rootNode.children.isEmpty()) {
                GreenBranch<KifAst.Move>(emptyList(), emptyMap())
            } else {
                val mainBranch = traverse(
                    ItemIdx(0),
                    emptyList(),
                    kifAst.rootNode.children[0]
                )
                kifAst.rootNode.children.let { it.subList(1, it.size) }
                    .fold(mainBranch) { b, n ->
                        b.addBranch(
                            traverse(
                                ItemIdx(0),
                                emptyList(),
                                n
                            ), Path.Terminal(ItemIdx(0))
                        ) ?: b
                    }
            }
            TODO("Not yet implemented")
            // Convert branch<KifAst> to branch<Move> with legality
        }
    }
}

/**
 * Given a `KifAst.Move`, convert it to a `Move`. This requires extra
 * information: the board position and the side making the move.
 */
private fun moveFromKifAst(
    kifMove: KifAst.Move,
    side: Side,
    posBeforeMove: Position,
): Move {
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


/**
 * Represents a variation consisting of moves and any branching points.
 *
 * In normal shogi game use cases, the expected number of branches is
 * expected to be low compared to the length of the game, and the
 * majority of operations would be done on the mainline.
 *
 * This data structure was chosen over an immutable tree (one move per node)
 * for the following advantages:
 *
 * - recursive depth of a single game is reduced
 * - localises the moves of a single variation together
 * - addMove() may be cheaper here than for an immutable tree
 */
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

    fun getChoice(choice: Choice): Variation<T>? {
        return if (
            moveNum < choice.moveNum
            && choice.moveNum <= moveNum + moves.size + 1
        ) {
            branches[choice.moveNum - moveNum]?.get(choice.idx)
        } else {
            null
        }
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

private data class Choice(val moveNum: MoveNum, val idx: Idx)

/**
 * The location within a [Variation]-based data structure
 * is given by the choices made at every branching point from the root,
 * and the move number within the current Variation.
 *
 * Also includes the position at the given location.
 */
private data class LocationX(
    val path: ArrayDeque<Choice>,
    val moveNum: MoveNum,
    val pos: PositionImpl,
)

class GameImplX private constructor(
    private val gameMoves: Variation<Move>,
    private val currentLocation: LocationX,
    private val startPos: PositionImpl,
) : Game {
    override fun addMove(move: Move): Either<GameError.IllegalMove, Game> {
        TODO("Not yet implemented")
    }

    override fun advanceMove(move: Move): Either<GameError.NoSuchMove, Game> {
        TODO("Not yet implemented")
    }

    override fun advance(): Either<GameError.EndOfVariation, Game> {
        val currentVariation =
            findVariation(this.gameMoves, this.currentLocation)
        val nextMove = currentVariation.moves
            .getOrNull(this.currentLocation.moveNum + 1 - currentVariation.moveNum)
            ?: return GameError.EndOfVariation.left()
        val newPos = this.currentLocation.pos.doMove(nextMove)
        return GameImplX(
            this.gameMoves,
            this.currentLocation.copy(
                moveNum = this.currentLocation.moveNum + 1,
                pos = newPos,
            ),
            this.startPos
        ).right()
    }

    override fun retract(): Either<GameError.StartOfGame, Game> {
        val currentVariation =
            findVariation(this.gameMoves, this.currentLocation)
        var prevMove = currentVariation.moves
            .getOrNull(this.currentLocation.moveNum - 1 - currentVariation.moveNum)
        if (prevMove == null) {
            // Try to go one variation back
            if (this.currentLocation.path.isEmpty()) {
                return GameError.StartOfGame.left()
            }
            val newPath = ArrayDeque(this.currentLocation.path)
            newPath.removeLast()
            val newLocation = this.currentLocation.copy(
                path = newPath,
                moveNum = this.currentLocation.moveNum - 1,
                pos = this.startPos // TODO: correctly calculate new position
            )
            prevMove = findVariation(this.gameMoves, newLocation)
                .moves
                .getOrNull(newLocation.moveNum - currentVariation.moveNum)
            assert(prevMove != null)
        }
//        val newPos = this.currentLocation.pos.undoMove(prevMove)
        return GameImplX(
            this.gameMoves,
            this.currentLocation.copy(
                moveNum = this.currentLocation.moveNum + 1,
//                pos = newPos,
            ),
            this.startPos
        ).right()
    }

    override fun goToStart(): Game {
        return GameImplX(
            this.gameMoves,
            LocationX(
                ArrayDeque(),
                this.gameMoves.moveNum,
                this.startPos
            ),
            this.startPos
        )
    }

    override fun goToVariationEnd(): Game {
        val currentVariation =
            findVariation(this.gameMoves, this.currentLocation)
        val newPos = currentVariation
            .let {
                val currentIdx = this.currentLocation.moveNum - it.moveNum
                it.moves.subList(currentIdx, it.moves.size)
            }
            .fold(this.currentLocation.pos) { acc, move -> acc.doMove(move) }
        return GameImplX(
            this.gameMoves,
            this.currentLocation.copy(
                moveNum = currentVariation.let { it.moveNum + it.moves.size - 1 },
                pos = newPos,
            ),
            this.startPos
        )
    }

    private fun <T> findVariation(
        gameMoves: Variation<T>,
        location: LocationX,
    ): Variation<T> {
        return location.path.foldRight(gameMoves) { choice, acc ->
            val branchIdx = choice.moveNum - acc.moveNum
            acc.branches[branchIdx]!![choice.idx]
        }
    }

    override fun isAtVariationEnd(): Boolean {
        val currentMoveNum = this.currentLocation.moveNum
        val finalMoveNum = findVariation(this.gameMoves, this.currentLocation)
            .let { it.moveNum + it.moves.size - 1 }
        return currentMoveNum == finalMoveNum
    }

    override fun getMainlineMove(): Move? {
        if (!this.currentLocation.path.isEmpty()) {
            return null
        }
        return this.gameMoves.moves
            .getOrNull(this.currentLocation.moveNum + 1 - this.gameMoves.moveNum)
    }

    companion object : GameFactory {
        override fun empty(): Game {
            val emptyPos = PositionImpl.empty()
            return GameImplX(
                Variation(0, emptyList(), emptyMap()),
                LocationX(ArrayDeque(), 0, emptyPos),
                emptyPos,
            )
        }

        override fun fromKifAst(kifAst: KifAst.Game<KifAst.Move>): Game {
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
            return playOut(
                mainVariation,
                kifAst.startPos.getSideToMove(),
                kifAst.startPos as PositionImpl
            )
                .fold({ GameImplX.empty() }) {
                    GameImplX(
                        it,
                        LocationX(
                            ArrayDeque(),
                            firstMoveNum,
                            kifAst.startPos
                        ),
                        kifAst.startPos,
                    )
                }
        }

        private fun moveFromKifAst(
            kifMove: KifAst.Move,
            side: Side,
            posBeforeMove: Position,
        ): Move {
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
            startPos: PositionImpl,
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

        /**
         * Transforms a [Variation]<[KifAst.Move]> to a `List`<[Move]> of
         * the variation's mainline moves.
         */
        private fun legalMovesFromKifMoves(
            variation: Variation<KifAst.Move>,
            startingSide: Side,
            startPos: PositionImpl,
        ): Either<String, List<Move>> {
            var pos = startPos
            val moveList = mutableListOf<Move>()
            for (kifMove in variation.moves) {
                val side = getSideToMove(
                    variation.moveNum,
                    kifMove.moveNum,
                    startingSide
                )
                val move = moveFromKifAst(kifMove, side, pos)
                if (!isLegal(move, pos)) {
                    return "Illegal move: ${move}".left()
                }
                pos = pos.doMove(move)
                moveList.add(move)
            }
            return moveList.right()
        }

        /**
         * Transforms a [KifAst.Tree] (of MoveNodes) to
         * a [Variation]<[KifAst.Move]>.
         */
        private fun traverse(
            acc: Variation<KifAst.Move>,
            node: KifAst.Tree.MoveNode<KifAst.Move>,
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
        GameImplX.fromKifAst(readKifFile("sample_problems/variations.kif")!!)
    println(game.isAtVariationEnd())
    println(game.goToVariationEnd().isAtVariationEnd())
}
