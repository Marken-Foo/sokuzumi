package com.mfoo.shogi.rgbranches

import com.mfoo.shogi.Tree

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
class RedGreenBranches<T> private constructor(
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

    fun add(item: T): RedGreenBranches<T>? {
        return when (this.location) {
            is Location.NonRoot -> {
                val newRoot = this.greenRoot.addItem(item, this.location.path)
                return null
            }

            is Location.Root -> {
                return null
            }

            else -> {
                throw IllegalStateException("Location is neither NonRoot or Root")
            }
        }
    }

    fun advance(): RedGreenBranches<T>? {
        return location.advance()
            ?.let { this.copy(location = it) }
    }

    fun advanceIfPresent(item: T): RedGreenBranches<T>? {
        return location.advanceIfPresent(item)
            ?.let { this.copy(location = it) }
    }

    fun getCurrentItem(): T? {
        return location.getCurrentItem()
    }

    fun getNextItem(): T? {
        return location.getNextItem()
    }

    fun isAtLeaf(): Boolean {
        return location.isAtLeaf()
    }

    companion object {
        fun <T> fromTree(treeRoot: Tree.RootNode<T>): RedGreenBranches<T> {
            return GreenRoot(treeRoot.children.map { traverse(it) })
                .let { RedGreenBranches(it, Location.Root(RedRoot(it))) }
        }
    }
}

internal fun <T> traverse(
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
