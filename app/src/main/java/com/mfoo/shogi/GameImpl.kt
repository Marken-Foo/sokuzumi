package com.mfoo.shogi

import arrow.core.Either
import com.mfoo.shogi.kif.KifAst
import com.mfoo.shogi.kif.readKifFile
import com.mfoo.shogi.kif.validateKif


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
    override fun toString(): String {
        return (items
            .mapIndexed { i, item -> i.toString() + item.toString() }
            .joinToString(separator = ", ")
            + "\n"
            + children.map { (itemIdx, branches) ->
            itemIdx.toString() + " " + branches.mapIndexed { i, branch -> "variation " + i.toString() + ": " + branch.toString() }
                .joinToString("\n")
        }
            )
    }

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
