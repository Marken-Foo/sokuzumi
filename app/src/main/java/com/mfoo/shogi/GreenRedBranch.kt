package com.mfoo.shogi


private sealed interface Path {
    data object Terminal : Path
    data class Segment(
        val idx: ItemIdx,
        val branchIdx: Int,
        val next: Path,
    ) : Path
}

/**
 * Represents the 0-indexed position of an item within a branch.
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
        return 0 <= idx.t && idx.t < items.size
    }

    fun addBranch(branch: GreenBranch<T>, finalIdx: ItemIdx): GreenBranch<T>? {
        if (branch.items.isEmpty()) {
            return null
        }
        if (!isItemIndexValid(finalIdx)) {
            return null
        }
        val newBranchList = children[finalIdx]
            ?.let { it + branch }
            ?: listOf(branch)
        return this.copy(children = children + (finalIdx to newBranchList))
    }
}

private data class RedBranch<T>(
    val value: GreenBranch<T>,
    val parent: RedBranch<T>?,
)

private data class Location<T>(
    val currentBranch: RedBranch<T>,
    val pathFromRoot: Path,
    val idxOnCurrentBranch: ItemIdx,
)

internal class RedGreenBranches<T> private constructor(
    private val greenRoot: GreenBranch<T>,
    private val location: Location<T>,
) {
    override fun toString(): String {
        return greenRoot.toString()
    }

    companion object {
        fun <T> fromTree(treeRoot: Tree.RootNode<T>): RedGreenBranches<T> {
            return greenBranchFromTree(treeRoot).let {
                RedGreenBranches(
                    it,
                    Location(RedBranch(it, null), Path.Terminal, ItemIdx(0))
                )
            }
        }
    }
}

private fun <T> greenBranchFromTree(treeRoot: Tree.RootNode<T>): GreenBranch<T> {
    return if (treeRoot.children.isEmpty()) {
        GreenBranch(emptyList(), emptyMap())
    } else {
        val mainBranch = traverse(treeRoot.children[0])
        treeRoot.children.let { it.subList(1, it.size) }
            .fold(mainBranch) { b, n ->
                b.addBranch(traverse(n), ItemIdx(0)) ?: b
            }
    }
}

private fun <T> traverse(
    node: Tree.Node<T>,
    idxInBranch: ItemIdx = ItemIdx(0),
    mainlineMoves: List<T> = emptyList(),
): GreenBranch<T> {
    if (node.children.isEmpty()) {
        return GreenBranch(mainlineMoves + node.value, emptyMap())
    }
    val branchOfCurrentNode = traverse(
        node.children[0],
        idxInBranch.increment(),
        mainlineMoves + node.value,
    )
    return node.children.subList(1, node.children.size)
        .fold(branchOfCurrentNode) { b, n ->
            b.addBranch(traverse(n), idxInBranch.increment()) ?: b
        }
}
