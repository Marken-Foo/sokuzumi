package com.mfoo.shogi.rgbranches


internal sealed interface Red<T>

internal class RedRoot<T>(private val value: GreenRoot<T>) : Red<T> {
    private var childrenCache: List<RedBranch<T>?> =
        List<RedBranch<T>?>(value.children.size) { null }

    private fun updateCache(branchIdx: BranchIdx, branch: RedBranch<T>) {
        childrenCache =
            childrenCache.replaceAt(branch, branchIdx.t) ?: childrenCache
    }

    fun findBranchIdx(predicate: (branch: GreenBranch<T>) -> Boolean): BranchIdx? {
        return value.children
            .indexOfFirst(predicate)
            .let { if (it == -1) null else BranchIdx(it) }
    }

    fun goToBranch(branchIdx: BranchIdx): RedBranch<T>? {
        val greenBranch = value.goToBranch(branchIdx)
            ?: return null

        return childrenCache.getOrNull(branchIdx.t)
            ?: RedBranch(greenBranch, this)
                .also { updateCache(branchIdx, it) }
    }

    fun isEmpty(): Boolean {
        return value.children.isEmpty()
    }
}

/**
 * Branch structure wrapping the immutable green branches, providing
 * a parent reference and child references. The child references are
 * generated on demand (upon access) and cached.
 */
internal class RedBranch<T>(
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
        branchIdx: BranchIdx,
        branch: RedBranch<T>,
    ) {
        childrenCache = childrenCache[itemIdx]
            ?.replaceAt(branch, branchIdx.t)
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
    ): BranchIdx? {
        return value.getBranches(itemIdx)
            ?.indexOfFirst(predicate)
            ?.let { if (it == -1) null else BranchIdx(it) }
    }

    fun goToBranch(itemIdx: ItemIdx, branchIdx: BranchIdx): RedBranch<T>? {
        val greenBranch = value.goToBranch(itemIdx, branchIdx)
            ?: return null

        return childrenCache[itemIdx]
            ?.getOrNull(branchIdx.t)
            ?: RedBranch(greenBranch, this)
                .also { updateAt(itemIdx, branchIdx, it) }
    }
}
