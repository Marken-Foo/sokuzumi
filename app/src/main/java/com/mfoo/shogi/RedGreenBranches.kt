package com.mfoo.shogi


/**
 * Recursive structure describing a path from a branch to one of its
 * descendants.
 */
private sealed interface Path {
    fun append(idx: ItemIdx, branchIdx: Int): Segment

    /**
     * Marks the end of a path.
     */
    data object Terminal : Path {
        override fun append(idx: ItemIdx, branchIdx: Int): Segment {
            return Segment(idx, branchIdx, Terminal)
        }
    }

    /**
     * An intermediate segment in the path, describing which child
     * to descend into, and the remaining path to follow after that.
     */
    data class Segment(
        val idx: ItemIdx,
        val branchIdx: Int,
        val next: Path,
    ) : Path {
        override fun append(idx: ItemIdx, branchIdx: Int): Segment {
            return this.copy(next = next.append(idx, branchIdx))
        }
    }
}

/**
 * Data structure containing the children of a branch (red or green).
 * At a given `ItemIdx`, there is an ordered list of children.
 */
private class Children<B>(val t: Map<ItemIdx, List<B>> = emptyMap()) {
    fun get(itemIdx: ItemIdx, branchIdx: Int): B? {
        return t[itemIdx]?.getOrNull(branchIdx)
    }

    fun addAt(itemIdx: ItemIdx, branch: B): Children<B> {
        return t[itemIdx]
            ?.let { Children(t + (itemIdx to it + branch)) }
            ?: Children(t + (itemIdx to listOf(branch)))
    }

    private fun <S> List<S>.replaceAt(item: S, idx: Int): List<S>? {
        return if (idx < 0 || this.size <= idx) {
            null
        } else {
            this.slice(0..<idx) + item + this.slice(idx + 1..<this.size)
        }
    }

    fun updateAt(itemIdx: ItemIdx, branchIdx: Int, branch: B): Children<B>? {
        return t[itemIdx]
            ?.replaceAt(branch, branchIdx)
            ?.let { Children(t + (itemIdx to it)) }
    }

    fun findAt(itemIdx: ItemIdx, predicate: (B) -> Boolean): Pair<Int, B>? {
        return t[itemIdx]
            ?.indexOfFirst(predicate)
            ?.let { if (it == -1) null else (it to t[itemIdx]!![it]) }
    }
}

/**
 * Represents the (0-indexed) position of an item within a single branch.
 */
@JvmInline
private value class ItemIdx(val t: Int) {
    fun increment(): ItemIdx {
        return ItemIdx(this.t + 1)
    }
}

/**
 * Immutable branch structure, unaware of global position.
 */
private data class GreenBranch<T>(
    val items: List<T>,
    val children: Children<GreenBranch<T>>,
) {
    override fun toString(): String {
        return (items
            .mapIndexed { i, item -> i.toString() + item.toString() }
            .joinToString(separator = ", ")
            + "\n"
            + children.t.map { (itemIdx, branches) ->
            "$itemIdx " + branches.mapIndexed { i, branch -> "variation $i: $branch" }
                .joinToString("\n")
        }
            )
    }

    private fun isItemIndexValid(idx: ItemIdx): Boolean {
        return 0 <= idx.t && idx.t < items.size
    }

    fun getAt(idx: ItemIdx): T? {
        return items[idx.t]
    }

    fun addBranch(branch: GreenBranch<T>, idx: ItemIdx): GreenBranch<T>? {
        if (branch.items.isEmpty()) {
            return null
        }
        if (!isItemIndexValid(idx)) {
            return null
        }
        return this.copy(children = children.addAt(idx, branch))
    }

    fun hasAsFirstItem(item: T): Boolean {
        return this.items.isNotEmpty() && this.items[0] == item
    }
}

/**
 * Branch structure wrapping the immutable green branches, providing
 * a parent reference and child references. The child references are
 * generated on demand (upon access) and cached.
 */
private class RedBranch<T>(
    private val value: GreenBranch<T>,
    private val parent: RedBranch<T>?,
) {
    // Populate the children cache with the appropriate number of
    // nulls so the indices are correct. Mutable.
    private var childrenCache: Children<RedBranch<T>?> =
        Children(value.children.t.mapValues { (_, list) ->
            List<RedBranch<T>?>(list.size) { null }
        })

    fun hasIndex(idx: ItemIdx): Boolean {
        return 0 <= idx.t && idx.t < value.items.size
    }

    fun getAt(idx: ItemIdx): T? {
        return this.value.getAt(idx)
    }

    fun findBranchIdx(
        idx: ItemIdx,
        predicate: (branch: GreenBranch<T>) -> Boolean,
    ): Int? {
        return value.children.findAt(idx, predicate)?.first
    }

    fun goToBranch(itemIdx: ItemIdx, branchIdx: Int): RedBranch<T>? {
        val greenBranch = value.children.get(itemIdx, branchIdx)
            ?: return null

        return childrenCache.get(itemIdx, branchIdx)
            ?: RedBranch(greenBranch, this)
                .also {
                    childrenCache =
                        childrenCache.updateAt(itemIdx, branchIdx, it)
                            ?: childrenCache
                }
    }
}

/**
 * A tree-like data structure, inspired by Roslyn's red-green trees.
 *
 * This data structure contains a reference to the root of the
 * immutable green branch system, and its location in that structure as:
 *
 * - a reference to the "current" branch (a red branch)
 * - the index of the "current" item on that branch
 * - a path from the root of the branch system to the current location.
 *
 * This is an experimental structure for the needs of a shogi game tree, which
 *
 * - is deep (a game typically lasts 100-200 moves)
 * - is not wide (low branching factor)
 * - has long non-branching chains of nodes (branches do not occur often)
 * - has a frequent operation of appending a move to the end of a line
 *
 * By condensing long chains of moves into "branches", we reduce the
 * recursive depth and simplify the operation of appending a move to a branch,
 * at the cost of needing a data structure to handle each branch's children
 * and more complexity in general than a simple tree.
 */
internal class RedGreenBranches<T> private constructor(
    private val greenRoot: GreenBranch<T>,
    private val currentBranch: RedBranch<T>,
    private val pathFromRoot: Path,
    private val idxOnBranch: ItemIdx,
) {
    override fun toString(): String {
        return greenRoot.toString()
    }

    private fun copy(
        greenRoot: GreenBranch<T> = this.greenRoot,
        currentBranch: RedBranch<T> = this.currentBranch,
        pathFromRoot: Path = this.pathFromRoot,
        idxOnBranch: ItemIdx = this.idxOnBranch,
    ): RedGreenBranches<T> {
        return RedGreenBranches(
            greenRoot,
            currentBranch,
            pathFromRoot,
            idxOnBranch
        )
    }

    fun advance(): RedGreenBranches<T>? {
        val newIdx = idxOnBranch.increment()
        return if (currentBranch.hasIndex(newIdx)) {
            this.copy(idxOnBranch = newIdx)
        } else {
            null
        }
    }

    fun advanceIfPresent(item: T): RedGreenBranches<T>? {
        val nextIdx = idxOnBranch.increment()

        if (currentBranch.getAt(nextIdx) == item) {
            return this.advance()
        }

        val branchIdx = currentBranch
            .findBranchIdx(nextIdx) { b -> b.hasAsFirstItem(item) }
            ?: return null

        return currentBranch.goToBranch(nextIdx, branchIdx)?.let {
            this.copy(
                currentBranch = it,
                pathFromRoot = pathFromRoot.append(nextIdx, branchIdx),
                idxOnBranch = ItemIdx(0),
            )
        }
    }

    companion object {
        fun <T> fromTree(treeRoot: Tree.RootNode<T>): RedGreenBranches<T> {
            return greenBranchFromTree(treeRoot).let {
                RedGreenBranches(
                    it,
                    RedBranch(it, null),
                    Path.Terminal,
                    ItemIdx(0)
                )
            }
        }
    }
}

private fun <T> greenBranchFromTree(treeRoot: Tree.RootNode<T>): GreenBranch<T> {
    return if (treeRoot.children.isEmpty()) {
        GreenBranch(emptyList(), Children())
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
        return GreenBranch(mainlineMoves + node.value, Children())
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
