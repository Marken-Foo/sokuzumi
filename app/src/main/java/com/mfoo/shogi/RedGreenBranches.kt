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

private sealed interface Red<T> {
    fun findBranchIdx(
        itemIdx: ItemIdx,
        predicate: (branch: GreenBranch<T>) -> Boolean,
    ): Int?
}

private class RedRoot<T>(private val value: GreenRoot<T>) : Red<T> {
    private var childrenCache: Children<RedBranch<T>?> =
        List<RedBranch<T>?>(value.branches.size) { null }
            .let { ItemIdx(0) to it }
            .let { Children(mapOf(it)) }

    override fun findBranchIdx(
        itemIdx: ItemIdx,
        predicate: (branch: GreenBranch<T>) -> Boolean,
    ): Int? {
        return value.branches
            .indexOfFirst(predicate)
            .let { if (it == -1) null else it }
    }

    fun goToBranch(
        itemIdx: ItemIdx = ItemIdx(0),
        branchIdx: Int,
    ): RedBranch<T>? {
        val greenBranch = value.goToBranch(branchIdx)
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
 * Branch structure wrapping the immutable green branches, providing
 * a parent reference and child references. The child references are
 * generated on demand (upon access) and cached.
 */
private class RedBranch<T>(
    private val value: GreenBranch<T>,
    private val parent: Red<T>,
) : Red<T> {
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

    override fun findBranchIdx(
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
    private val greenRoot: GreenRoot<T>,
    private val location: Location<T>,
) {
    private sealed interface Location<S> {
        class Root<S>(val current: RedRoot<S>) : Location<S>
        data class NonRoot<S>(
            val current: RedBranch<S>,
            val path: Path,
        ) : Location<S>
    }

    override fun toString(): String {
        return greenRoot.toString()
    }

    private fun copy(
        greenRoot: GreenRoot<T> = this.greenRoot,
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
                if (greenRoot.branches.isEmpty()) {
                    return null
                } else {
                    val newLocation = Location.NonRoot(
                        RedBranch(greenRoot.branches[0], location.current),
                        Path(0, emptyList(), ItemIdx(0)),
                    )
                    return RedGreenBranches(greenRoot, newLocation)
                }
            }

            is Location.NonRoot<T> -> {
                val newIdx = location.path.finalIdx.increment()
                return if (location.current.hasIndex(newIdx)) {
                    location.path
                        .copy(finalIdx = newIdx)
                        .let { location.copy(path = it) }
                        .let { this.copy(location = it) }
                } else {
                    null
                }
            }
        }
    }

    fun advanceIfPresent(item: T): RedGreenBranches<T>? {
        when (location) {
            is Location.Root -> {
                val branchIdx = greenRoot.branches
                    .indexOfFirst { b -> b.hasAsFirstItem(item) }
                    .let { if (it == -1) null else it }
                    ?: return null

                return RedGreenBranches(
                    greenRoot,
                    Location.NonRoot(
                        RedBranch(
                            greenRoot.branches[branchIdx],
                            location.current
                        ),
                        Path(branchIdx, emptyList(), ItemIdx(0)),
                    )
                )
            }

            is Location.NonRoot -> {
                val nextIdx = location.path.finalIdx.increment()

                if (location.current.getAt(nextIdx) == item) {
                    return this.advance()
                }

                val branchIdx = location.current
                    .findBranchIdx(nextIdx) { b -> b.hasAsFirstItem(item) }
                    ?: return null

                val newPath = location.path.append(nextIdx, branchIdx)

                return location.current.goToBranch(nextIdx, branchIdx)
                    ?.let {
                        Location.NonRoot(
                            current = it,
                            path = newPath,
                        )
                    }
                    ?.let { this.copy(location = it) }
            }
        }

    }

    companion object {
        fun <T> fromTree(treeRoot: Tree.RootNode<T>): RedGreenBranches<T> {
            return greenBranchFromTree(treeRoot).let {
                RedGreenBranches(
                    it,
                    Location.Root(RedRoot(it)),
                )
            }
        }
    }
}

private fun <T> greenBranchFromTree(treeRoot: Tree.RootNode<T>): GreenRoot<T> {
    return GreenRoot(treeRoot.children.map { traverse(it) })
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
