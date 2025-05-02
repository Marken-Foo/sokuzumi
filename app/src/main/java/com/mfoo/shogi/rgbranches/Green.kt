package com.mfoo.shogi.rgbranches

internal class GreenRoot<T>(val branches: List<GreenBranch<T>>) {
    override fun toString(): String {
        return branches.toString()
    }

    fun goToBranch(branchIdx: BranchIdx): GreenBranch<T>? {
        return branches.getOrNull(branchIdx.t)
    }

    fun addItem(item: T, path: Path?): GreenRoot<T>? {
        return if (path == null) {
            GreenRoot(this.branches + GreenBranch(item, emptyList()))
        } else {
            goToBranch(path.rootIdx)
                ?.addItem(
                    item,
                    PartialPath(
                        choices = path.choices,
                        finalIdx = path.finalIdx
                    )
                )
                ?.let { this.branches.replaceAt(it, path.rootIdx.t) }
                ?.let { GreenRoot(it) }
        }
    }
}

/**
 * Immutable branch structure, unaware of global position.
 */
internal class GreenBranch<T> private constructor(
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

    fun addItem(item: T, path: PartialPath): GreenBranch<T>? {
        if (path.choices.isEmpty()) {
            if (this.body.size == path.finalIdx.t) {
                return copy(body = this.body + Node(item, emptyList()))
            }
            val node = this.body.getOrNull(path.finalIdx.t)?: return null
            if (node.branches.any { it.hasAsFirstItem(item) }) {
                return this
            }
            return GreenBranch(item, emptyList())
                .let { node.branches + it }
                .let { Node(node.item, it) }
                .let { this.body.replaceAt(it, path.finalIdx.t) }
                ?.let { copy(body = it) }
                ?: return null
        } else {
            val (itemIdx, branchIdx) = path.choices.first()
            val oldNode = this.body.getOrNull(itemIdx.t) ?: return null
            val nextPath = path.copy(choices = path.choices.drop(1))
            return goToBranch(itemIdx, branchIdx)
                ?.addItem(item, nextPath)
                ?.let { oldNode.branches.replaceAt(it, branchIdx.t) }
                ?.let { Node(oldNode.item, it) }
                ?.let { this.body.replaceAt(it, itemIdx.t) }
                ?.let { copy(body = it) }
                ?: return null
        }
    }

    fun addBranch(branch: GreenBranch<T>, idx: ItemIdx): GreenBranch<T>? {
        return body.getOrNull(idx.t - 1)
            ?.let { Node(it.item, it.branches + branch) }
            ?.let { body.replaceAt(it, idx.t - 1) }
            ?.let { this.copy(body = it) }
    }

    fun goToBranch(itemIdx: ItemIdx, branchIdx: BranchIdx): GreenBranch<T>? {
        return getBranches(itemIdx)?.getOrNull(branchIdx.t)
    }

    fun hasAsFirstItem(item: T): Boolean {
        return firstItem == item
    }
}
