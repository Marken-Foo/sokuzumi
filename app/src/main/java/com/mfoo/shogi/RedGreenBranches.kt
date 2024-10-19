package com.mfoo.shogi


private data class Path(
    val rootIdx: Int,
    val choices: List<Pair<ItemIdx, Int>>,
    val finalIdx: ItemIdx,
) {
    fun append(idx: ItemIdx, branchIdx: Int): Path {
        return this.copy(choices = choices + (idx to branchIdx))
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

private class GreenRoot<T>(val branches: List<GreenBranch<T>>) {
    override fun toString(): String {
        return branches.toString()
    }

    fun goToBranch(branchIdx: Int): GreenBranch<T>? {
        return branches.getOrNull(branchIdx)
    }
}

/**
 * Immutable branch structure, unaware of global position.
 */
private class GreenBranch<T> private constructor(
    val firstItem: T,
    val body: List<Node<T>>,
) {
    // Extra parameter to avoid JVM constructor issues
    constructor(head: T, tail: List<T>, x: Any? = null) :
        this(head, tail.map { Node(it, listOf()) })


    class Node<T>(val item: T, val branches: List<GreenBranch<T>>)

    override fun toString(): String {
        val items = listOf(firstItem) + body.map { it.item }
        val branches = body
            .mapIndexed { idx, node -> idx + 1 to node.branches }
            .filter { (_, branches) -> branches.isNotEmpty() }
        return (items
            .mapIndexed { i, item -> i.toString() + item.toString() }
            .joinToString(separator = ", ")
            + "\n"
            + branches.map { (itemIdx, branches) ->
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

private sealed interface Red<T>

private class RedRoot<T>(private val value: GreenRoot<T>) : Red<T> {
    private var childrenCache: List<RedBranch<T>?> =
        List<RedBranch<T>?>(value.branches.size) { null }

    private fun updateCache(branchIdx: Int, branch: RedBranch<T>) {
        childrenCache =
            childrenCache.replaceAt(branch, branchIdx) ?: childrenCache
    }

    fun findBranchIdx(predicate: (branch: GreenBranch<T>) -> Boolean): Int? {
        return value.branches
            .indexOfFirst(predicate)
            .let { if (it == -1) null else it }
    }

    fun goToBranch(branchIdx: Int): RedBranch<T>? {
        val greenBranch = value.goToBranch(branchIdx)
            ?: return null

        return childrenCache.getOrNull(branchIdx)
            ?: RedBranch(greenBranch, this)
                .also { updateCache(branchIdx, it) }
    }
}

/**
 * Branch structure wrapping the immutable green branches, providing
 * a parent reference and child references. The child references are
 * generated on demand (upon access) and cached.
 */
private class RedBranch<T>(
    private val value: GreenBranch<T>,
    val parent: Red<T>,
) : Red<T> {
    // Populate the children cache with the appropriate number of
    // nulls so the indices are correct. Mutable.
    private var childrenCache: Map<ItemIdx, List<RedBranch<T>?>> =
        value.body.mapIndexedNotNull { index, node ->
            if (node.branches.isEmpty()) {
                null
            } else {
                List<RedBranch<T>?>(node.branches.size) { null }
                    .let { ItemIdx(index + 1) to it }
            }
        }.toMap()

    private fun updateAt(
        itemIdx: ItemIdx,
        branchIdx: Int,
        branch: RedBranch<T>,
    ) {
        childrenCache = childrenCache[itemIdx]
            ?.replaceAt(branch, branchIdx)
            ?.let { childrenCache + (itemIdx to it) }
            ?: childrenCache
    }

    fun hasIndex(idx: ItemIdx): Boolean {
        return 0 <= idx.t && idx.t < value.body.size + 1
    }

    fun getAt(idx: ItemIdx): T? {
        return this.value.getAt(idx)
    }

    fun findBranchIdx(
        itemIdx: ItemIdx,
        predicate: (branch: GreenBranch<T>) -> Boolean,
    ): Int? {
        return value.getBranches(itemIdx)
            ?.indexOfFirst(predicate)
            ?.let { if (it == -1) null else it }
    }

    fun goToBranch(itemIdx: ItemIdx, branchIdx: Int): RedBranch<T>? {
        val greenBranch = value.goToBranch(itemIdx, branchIdx)
            ?: return null

        return childrenCache[itemIdx]
            ?.getOrNull(branchIdx)
            ?: RedBranch(greenBranch, this)
                .also { updateAt(itemIdx, branchIdx, it) }
    }
}

private sealed interface Location<T> {
    val current: Red<T>

    fun advance(): Location<T>?
    fun advanceIfPresent(item: T): Location<T>?

    class Root<T>(override val current: RedRoot<T>) : Location<T> {
        override fun advance(): Location<T>? {
            return current.goToBranch(0)
                ?.let { NonRoot(it, Path(0, emptyList(), ItemIdx(0))) }
        }

        override fun advanceIfPresent(item: T): Location<T>? {
            val branchIdx =
                current.findBranchIdx { b -> b.hasAsFirstItem(item) }
                    ?: return null

            return current.goToBranch(branchIdx)
                ?.let { NonRoot(it, Path(branchIdx, emptyList(), ItemIdx(0))) }
        }
    }

    class NonRoot<T>(
        override val current: RedBranch<T>,
        val path: Path,
    ) : Location<T> {
        fun copy(
            current: RedBranch<T> = this.current,
            path: Path = this.path,
        ): NonRoot<T> {
            return NonRoot(current, path)
        }

        override fun advance(): Location<T>? {
            val newIdx = path.finalIdx.increment()
            return if (current.hasIndex(newIdx)) {
                path.copy(finalIdx = newIdx).let { this.copy(path = it) }
            } else {
                null
            }
        }

        override fun advanceIfPresent(item: T): Location<T>? {
            val nextIdx = path.finalIdx.increment()

            if (current.getAt(nextIdx) == item) {
                return this.advance()
            }

            val branchIdx = current
                .findBranchIdx(nextIdx) { b -> b.hasAsFirstItem(item) }
                ?: return null

            val newPath = path.append(nextIdx, branchIdx)

            return current.goToBranch(nextIdx, branchIdx)
                ?.let { NonRoot(current = it, path = newPath) }
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
    private val greenRoot: GreenRoot<T>,
    private val location: Location<T>,
) {
    override fun toString(): String {
        return greenRoot.toString()
    }

    private fun copy(
        greenRoot: GreenRoot<T> = this.greenRoot,
        location: Location<T> = this.location,
    ): RedGreenBranches<T> {
        return RedGreenBranches(greenRoot, location)
    }

    fun advance(): RedGreenBranches<T>? {
        return location.advance()
            ?.let { this.copy(location = it) }
    }

    fun advanceIfPresent(item: T): RedGreenBranches<T>? {
        return location.advanceIfPresent(item)
            ?.let { this.copy(location = it) }
    }

    companion object {
        fun <T> fromTree(treeRoot: Tree.RootNode<T>): RedGreenBranches<T> {
            return GreenRoot(treeRoot.children.map { traverse(it) })
                .let { RedGreenBranches(it, Location.Root(RedRoot(it))) }
        }
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
