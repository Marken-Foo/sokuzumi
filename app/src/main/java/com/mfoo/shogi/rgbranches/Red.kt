package com.mfoo.shogi.rgbranches


internal sealed interface RedParent<T> {
    val red: Red<T>

    class Root<T>(override val red: RedRoot<T>, branchIdx: BranchIdx) :
        RedParent<T>

    class Branch<T>(
        override val red: RedBranch<T>,
        val itemIdx: ItemIdx,
        branchIdx: BranchIdx,
    ) :
        RedParent<T>
}


internal sealed interface Red<T> {
    fun advance(): RedBranch<T>?
}

internal class RedRoot<T>(private val green: GreenRoot<T>) : Red<T> {
    private val children: RedBranching<T> = green
        .listBranches()
        .let { RedBranching.fromGreenBranches(it) }

    fun goToBranch(bIdx: BranchIdx): RedBranch<T>? {
        return children.get(bIdx)
            ?: green.goToBranch(bIdx)
                ?.let { RedBranch(it, RedParent.Root(this, bIdx)) }
                ?.also { children.update(bIdx, it) }
    }

    fun followPath(path: Path.Root): RedRoot<T> {
        return this
    }

    fun followPath(path: Path.T): RedBranch<T>? {
        return goToBranch(path.rootIdx)
            ?.followPath(path.getPartialPath())
    }

    override fun advance(): RedBranch<T>? {
        return goToBranch(BranchIdx(0))
    }

    fun findBranchIdx(item: T): BranchIdx? {
        return green.findBranchIdx(item)
    }
}

/**
 * Branch structure wrapping the immutable green branches, providing
 * a parent reference and child references. The child references are
 * generated on demand (upon access) and cached.
 */
internal class RedBranch<T>(
    private val green: GreenBranch<T>,
    private val parent: RedParent<T>,
) : Red<T> {
    private val children: RedCache<T> = RedCache.fromGreenBranch(green)

    fun goToBranch(iIdx: ItemIdx, bIdx: BranchIdx): RedBranch<T>? {
        return if (iIdx.t == 0) {
            when (parent) {
                is RedParent.Root -> parent.red.goToBranch(bIdx)
                is RedParent.Branch -> {
                    parent.red.goToBranch(parent.itemIdx, bIdx)
                }
            }
        } else {
            this.children.get(iIdx, bIdx)
                ?: green.goToBranch(iIdx, bIdx)
                    ?.let { RedBranch(it, RedParent.Branch(this, iIdx, bIdx)) }
                    ?.also { children.update(iIdx, bIdx, it) }
        }
    }

    fun followPath(path: PartialPath): RedBranch<T>? {
        val (head, tail) = path.pop()
        return if (head == null) {
            this
        } else {
            val (iIdx, bIdx) = head
            this.goToBranch(iIdx, bIdx)?.followPath(tail)
        }
    }

    fun findBranchIdx(item: T, iIdx: ItemIdx): BranchIdx? {
        return this.green.findBranchIdx(item, iIdx)
    }

    override fun advance(): RedBranch<T> {
        return this
    }

    fun parent(): Red<T> {
        return this.parent.red
    }

    fun getAt(iIdx: ItemIdx): T? {
        return this.green.getAt(iIdx)
    }

    fun size(): Int {
        return this.green.size()
    }
}

internal class RedCache<T>(private val t: MutableMap<ItemIdx, RedBranching<T>> = mutableMapOf()) {
    fun get(iIdx: ItemIdx, bIdx: BranchIdx): RedBranch<T>? {
        return this.t[iIdx]?.get(bIdx)
    }

    fun update(iIdx: ItemIdx, bIdx: BranchIdx, branch: RedBranch<T>) {
        this.t[iIdx]?.update(bIdx, branch)
    }

    companion object {
        // Populate the children cache with the appropriate number of
        // nulls so the indices are correct. Mutable.
        fun <T> fromGreenBranch(branch: GreenBranch<T>): RedCache<T> {
            return branch.body
                .mapIndexedNotNull { index, node ->
                    node.listBranches()
                        .ifEmpty { null }
                        ?.let { RedBranching.fromGreenBranches(it) }
                        ?.let { ItemIdx(index + 1) to it }
                }
                .toMap()
                .toMutableMap()
                .let { RedCache(it) }
        }
    }
}


internal class RedBranching<T>(private val t: MutableList<RedBranch<T>?> = mutableListOf()) {
    fun update(bIdx: BranchIdx, branch: RedBranch<T>) {
        if (0 <= bIdx.t && bIdx.t < this.t.size) this.t[bIdx.t] = branch
    }

    fun get(bIdx: BranchIdx): RedBranch<T>? {
        return t.getOrNull(bIdx.t)
    }

    companion object {
        fun <T> fromGreenBranches(branches: List<GreenBranch<T>>): RedBranching<T> {
            return RedBranching(MutableList(branches.size) { null })
        }
    }
}
