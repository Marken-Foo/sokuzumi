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
sealed class RGBranches<T> private constructor(
    private val greenRoot: GreenRoot<T>,
    private val currentPath: Path,
    private val red: Red<T>,
) {
    fun add(item: T): RGBranches<T>? {
        val (newGreenRoot, newPath) = greenRoot.addItem(item, currentPath)
            ?: return null
        val newRed = RedRoot(newGreenRoot).followPath(newPath) ?: return null
        return NonRoot(newGreenRoot, newPath, newRed)
    }

    fun advance(): RGBranches<T>? {
        return this.red
            .advance()
            ?.let { NonRoot(greenRoot, currentPath.advance(), it) }
    }

    abstract fun advanceIfPresent(item: T): RGBranches<T>?
    abstract fun retract(): RGBranches<T>?
    abstract fun goToStart(): RGBranches<T>
    abstract fun goToEnd(): RGBranches<T>
    abstract fun getCurrentItem(): T?

    fun getNextItem(): T? {
        return this.advance()?.getCurrentItem()
    }

    abstract fun getItemsToEnd(): List<T>

    abstract fun isAtEnd(): Boolean

    private class Root<T>(
        private val greenRoot: GreenRoot<T>,
        private val currentPath: Path.Empty,
        private val red: RedRoot<T>,
    ) :
        RGBranches<T>(greenRoot, currentPath, red) {

        override fun advanceIfPresent(item: T): NonRoot<T>? {
            val bIdx = red.findBranchIdx(item) ?: return null
            val newRed = red.goToBranch(bIdx) ?: return null
            return NonRoot(greenRoot, currentPath.goTo(bIdx), newRed)
        }

        override fun retract(): RGBranches<T>? {
            return null
        }

        override fun goToStart(): RGBranches<T> {
            return this
        }

        override fun goToEnd(): RGBranches<T> {
            val newRed = red.advance() ?: return this
            val newPath = Path.Full(endIdx = ItemIdx(newRed.size()))
            return NonRoot(greenRoot, newPath, newRed)
        }

        override fun getCurrentItem(): T? {
            return null
        }

        override fun getItemsToEnd(): List<T> {
            return greenRoot
                .goToBranch(BranchIdx(0))
                ?.getAll()
                ?: emptyList()
        }

        override fun isAtEnd(): Boolean {
            return greenRoot.listBranches().isEmpty()
        }
    }

    private class NonRoot<T>(
        private val greenRoot: GreenRoot<T>,
        private val currentPath: Path.Full,
        private val red: RedBranch<T>,
    ) : RGBranches<T>(greenRoot, currentPath, red) {
        override fun advanceIfPresent(item: T): NonRoot<T>? {
            val iIdx = currentPath.endIdx.increment()
            val bIdx = red.findBranchIdx(item, iIdx) ?: return null
            return red.goToBranch(iIdx, bIdx)
                ?.let { NonRoot(greenRoot, currentPath.goTo(iIdx, bIdx), it) }
        }

        override fun retract(): RGBranches<T> {
            val newPath = currentPath.retract()
            val newRed = red.parent()
            return if (newPath is Path.Full && newRed is RedBranch) {
                NonRoot(greenRoot, newPath, newRed)
            } else if (newPath is Path.Empty && newRed is RedRoot) {
                Root(greenRoot, newPath, newRed)
            } else {
                throw IllegalStateException("Path and parent do not agree")
            }
        }

        override fun goToStart(): RGBranches<T> {
            var newRed: Red<T> = this.red
            while (newRed is RedBranch) {
                newRed = newRed.parent()
            }
            return Root(greenRoot, Path.Empty, newRed as RedRoot<T>)
        }

        override fun goToEnd(): NonRoot<T> {
            return NonRoot(
                greenRoot,
                currentPath.goTo(ItemIdx(red.size())),
                red
            )
        }

        override fun getCurrentItem(): T? = red.getAt(currentPath.endIdx)

        override fun getItemsToEnd(): List<T> {
            return red.getAll().drop(1 + currentPath.endIdx.t)
        }

        override fun isAtEnd(): Boolean = red.size() == currentPath.endIdx.t
    }

    override fun toString(): String {
        return greenRoot.toString()
    }

    companion object {
        fun <T> empty(): RGBranches<T> {
            return GreenRoot<T>(emptyList())
                .let { Root(it, Path.Empty, RedRoot(it)) }
        }

        fun <T> fromTree(treeRoot: Tree.RootNode<T>): RGBranches<T> {
            return treeRoot.children
                .map { traverse(it) }
                .let(::GreenRoot)
                .let { Root(it, Path.Empty, RedRoot(it)) }
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
