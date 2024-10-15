package com.mfoo.shogi


/**
 * Recursive structure describing a path from a branch to one of its
 * descendants.
 */
private sealed interface Path {
    fun append(idx: ItemIdx, branchIdx: Int): Path

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

    data class Start(val branchIdx: Int, val next: Path) : Path {
        override fun append(idx: ItemIdx, branchIdx: Int): Start {
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
private class GreenBranch<T> private constructor(
    val firstItem: T,
    val body: List<Node<T>>,
//    val children: Children<GreenBranch<T>>,
) {
    // Extra parameter to avoid JVM constructor issues
    constructor(head: T, tail: List<T>, x: Any? = null) :
        this(head, tail.map { Node(it, listOf()) })


    class Node<T>(val item: T, val branches: List<GreenBranch<T>>)

    override fun toString(): String {
        val items = listOf(firstItem) + body.map { it.item }
        return (items
            .mapIndexed { i, item -> i.toString() + item.toString() }
            .joinToString(separator = ", ")
            + "\n"
            + body
            .mapIndexed { idx, node -> idx + 1 to node.branches }
            .filter { (_, branches) -> branches.isNotEmpty() }
            .map { (itemIdx, branches) ->
                "Index $itemIdx alternative, " + branches.mapIndexed { i, branch -> "variation $i: $branch" }
                    .joinToString("\n")
            }
            )
    }

    fun copy(
        firstItem: T = this.firstItem,
        body: List<Node<T>> = this.body,
    ): GreenBranch<T> {
        return GreenBranch(firstItem = firstItem, body = body)
    }

    fun getAt(idx: ItemIdx): T? {
        return if (idx.t == 0) {
            firstItem
        } else {
            body.getOrNull(idx.t - 1)?.item
        }
    }

    fun getBranches(idx: ItemIdx): List<GreenBranch<T>>? {
        return if (idx.t == 0) {
            null
        } else {
            body.getOrNull(idx.t - 1)?.branches
        }
    }

    fun addBranch(branch: GreenBranch<T>, idx: ItemIdx): GreenBranch<T>? {
        return body.getOrNull(idx.t - 1)
            ?.let { Node(it.item, it.branches + branch) }
            ?.let { body.replaceAt(it, idx.t - 1) }
            ?.let { this.copy(body = it) }
    }

    fun goToBranch(itemIdx: ItemIdx, branchIdx: Int): GreenBranch<T>? {
        return getBranches(itemIdx)?.getOrNull(branchIdx)
    }

    fun hasAsFirstItem(item: T): Boolean {
        return firstItem == item
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
        value.body
            .mapIndexedNotNull { index, node ->
                if (node.branches.isEmpty()) {
                    null
                } else {
                    List<RedBranch<T>?>(node.branches.size) { null }
                        .let { ItemIdx(index + 1) to it }
                }
            }
            .let { Children(it.toMap()) }

    fun hasIndex(idx: ItemIdx): Boolean {
        return 0 <= idx.t && idx.t < value.body.size + 1
    }

    fun getAt(idx: ItemIdx): T? {
        return this.value.getAt(idx)
    }

    fun findBranchIdx(
        idx: ItemIdx,
        predicate: (branch: GreenBranch<T>) -> Boolean,
    ): Int? {
        return value.getBranches(idx)
            ?.indexOfFirst(predicate)
            ?.let { if (it == -1) null else it }
    }

    fun goToBranch(itemIdx: ItemIdx, branchIdx: Int): RedBranch<T>? {
        val greenBranch = value.goToBranch(itemIdx, branchIdx)
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
    private val greenRoot: List<GreenBranch<T>>,
    private val location: Location<T>,
) {
    private sealed interface Location<S> {
        class Root<S> : Location<S>
        data class T<S>(
            val currentBranch: RedBranch<S>,
            val pathFromRoot: Path,
            val idxOnBranch: ItemIdx = ItemIdx(0),
        ) : Location<S>
    }

    override fun toString(): String {
        return greenRoot.toString()
    }

    private fun copy(
        greenRoot: List<GreenBranch<T>> = this.greenRoot,
        location: Location<T> = this.location,
    ): RedGreenBranches<T> {
        return RedGreenBranches(
            greenRoot,
            location,
        )
    }

    fun advance(): RedGreenBranches<T>? {
        when (location) {
            is Location.Root -> {
                if (greenRoot.isEmpty()) {
                    return null
                } else {
                    val newLocation = Location.T(
                        RedBranch(greenRoot[0], null),
                        Path.Start(0, Path.Terminal),
                    )
                    return RedGreenBranches(greenRoot, newLocation)
                }
            }

            is Location.T<T> -> {
                val newIdx = location.idxOnBranch.increment()
                return if (location.currentBranch.hasIndex(newIdx)) {
                    this.copy(location = this.location.copy(idxOnBranch = newIdx))
                } else {
                    null
                }
            }
        }
    }

    fun advanceIfPresent(item: T): RedGreenBranches<T>? {
        when (location) {
            is Location.Root -> {
                val branchIdx = greenRoot
                    .indexOfFirst { b -> b.hasAsFirstItem(item) }
                    .let { if (it == -1) null else it }
                    ?: return null

                return RedGreenBranches(
                    greenRoot,
                    Location.T(
                        RedBranch(greenRoot[branchIdx], null),
                        Path.Start(branchIdx, Path.Terminal),
                    )
                )
            }

            is Location.T -> {
                val nextIdx = location.idxOnBranch.increment()

                if (location.currentBranch.getAt(nextIdx) == item) {
                    return this.advance()
                }

                val branchIdx = location.currentBranch
                    .findBranchIdx(nextIdx) { b -> b.hasAsFirstItem(item) }
                    ?: return null

                val newPath = location.pathFromRoot.append(nextIdx, branchIdx)

                return location.currentBranch.goToBranch(nextIdx, branchIdx)
                    ?.let {
                        Location.T(
                            currentBranch = it,
                            pathFromRoot = newPath,
                        )
                    }
                    ?.let { this.copy(location = it) }
            }
        }

    }

    companion object {
        fun <T> fromTree(treeRoot: Tree.RootNode<T>): RedGreenBranches<T> {
            return RedGreenBranches(
                greenBranchFromTree(treeRoot),
                Location.Root()
            )
        }
    }
}

private fun <T> greenBranchFromTree(treeRoot: Tree.RootNode<T>): List<GreenBranch<T>> {
    return if (treeRoot.children.isEmpty()) {
        emptyList()
    } else {
        treeRoot.children.map { traverse(it) }
    }
}

private fun <T> traverse(
    node: Tree.Node<T>,
    idxInBranch: ItemIdx = ItemIdx(0),
    mainlineMoves: List<T> = emptyList(),
): GreenBranch<T> {
    if (node.children.isEmpty()) {
        return (mainlineMoves + node.value).let {
            GreenBranch(it.first(), it.subList(1, it.size))
        }
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

private fun <S> List<S>.replaceAt(item: S, idx: Int): List<S>? {
    return if (idx < 0 || this.size <= idx) {
        null
    } else {
        this.slice(0..<idx) + item + this.slice(idx + 1..<this.size)
    }
}
