package com.mfoo.shogi

import com.mfoo.shogi.kif.KifAst


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

    companion object {
        fun empty(): Terminal {
            return Terminal(ItemIdx(0))
        }
    }
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
)

internal class RedGreenBranches<T> private constructor(
    private val greenRoot: GreenBranch<T>,
    private val redBranch: RedBranch<T>,
    private val location: Path,
) {
    override fun toString(): String {
        return greenRoot.toString()
    }

    companion object {
        fun <T> fromTree(treeRoot: KifAst.Tree.RootNode<T>): RedGreenBranches<T> {
            return greenBranchFromTree(treeRoot).let {
                RedGreenBranches(it, RedBranch(it, null), Path.empty())
            }
        }
    }
}

private fun <T> greenBranchFromTree(treeRoot: KifAst.Tree.RootNode<T>): GreenBranch<T> {
    return if (treeRoot.children.isEmpty()) {
        GreenBranch(emptyList(), emptyMap())
    } else {
        val mainBranch = traverse(treeRoot.children[0])
        treeRoot.children.let { it.subList(1, it.size) }
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
