package com.mfoo.shogi.rgbranches

internal class GreenRoot<T>(internal val children: GreenBranching<T>) {
    override fun toString(): String {
        return children.toString()
    }

    fun goToBranch(branchIdx: BranchIdx): GreenBranch<T>? {
        return children.getOrNull(branchIdx)
    }

    fun addItem(item: T, path: Path?): GreenRoot<T>? {
        return if (path == null) {
            GreenRoot(children.add(item))
        } else {
            goToBranch(path.rootIdx)
                ?.addItem(
                    item,
                    PartialPath(
                        choices = path.choices,
                        finalIdx = path.finalIdx
                    )
                )
                ?.let { children.replaceAt(it, path.rootIdx) }
                ?.let { GreenRoot(it) }
        }
    }
}

/**
 * Immutable branch structure, unaware of global position.
 */
internal class GreenBranch<T> constructor(
    val firstItem: T,
    val body: List<Node<T>> = emptyList(),
) {
    // Extra parameter to avoid JVM constructor issues
    constructor(head: T, tail: List<T>, x: Any? = null) :
        this(head, tail.map { Node(it, GreenBranching()) })


    class Node<T>(val item: T, val branches: GreenBranching<T>) {
        fun add(branch: GreenBranch<T>): Node<T> {
            return if (this.branches.contains(item)) {
                this
            } else {
                Node(this.item, this.branches.add(branch))
            }
        }

        fun replaceAt(branch: GreenBranch<T>, bIdx: BranchIdx): Node<T>? {
            return if (this.branches.contains(branch.firstItem)) {
                this
            } else {
                this.branches
                    .replaceAt(branch, bIdx)
                    ?.let { Node(this.item, it) }
            }
        }
    }

    override fun toString(): String {
        val items = listOf(firstItem) + body.map { it.item }
        val indexedBranches = body
            .mapIndexed { idx, node -> idx + 1 to node.branches.listBranches() }
            .filter { (_, branches) -> branches.isNotEmpty() }
        val itemsString = (items
            .mapIndexed { i, item -> i.toString() + item.toString() }
            .joinToString(separator = ", "))
        val branchesString = indexedBranches
            .map { (itemIdx, branches) ->
                "Index $itemIdx alternative, " +
                    branches
                        .mapIndexed { i, branch -> "variation $i: $branch" }
                        .joinToString("\n")
            }
        return itemsString + "\n" + branchesString
    }

    fun copy(
        firstItem: T = this.firstItem,
        body: List<Node<T>> = this.body,
    ): GreenBranch<T> {
        return GreenBranch(firstItem = firstItem, body = body)
    }

    private fun replaceAt(node: Node<T>, iIdx: ItemIdx): GreenBranch<T>? {
        val idx = iIdx.decrement().t
        return if (idx <= 0) {
            null
        } else {
            this.body
                .replaceAt(node, idx)
                ?.let { copy(body = it) }
        }
    }

    fun getAt(iIdx: ItemIdx): T? {
        return if (iIdx.t == 0) {
            firstItem
        } else {
            getNode(iIdx.decrement())?.item
        }
    }

    fun getBranches(iIdx: ItemIdx): GreenBranching<T>? {
        return getNode(iIdx)?.branches
    }

    private fun getNode(iIdx: ItemIdx): Node<T>? {
        return this.body.getOrNull(iIdx.decrement().t)
    }

    fun addItem(item: T, path: PartialPath): GreenBranch<T>? {
        if (path.choices.isEmpty()) {
            val isAtBranchEnd = this.body.size == path.finalIdx.t
            if (isAtBranchEnd) {
                return addToEnd(Node(item, GreenBranching()))
            }
            val node = getNode(path.finalIdx)
                ?: return null
            if (node.branches.contains(item)) {
                return this
            }
            return node.add(GreenBranch(item))
                .let { this.replaceAt(it, path.finalIdx) }
        } else {
            val (itemIdx, branchIdx) = path.choices.first()
            val oldNode = getNode(itemIdx)
                ?: return null
            val nextPath = path.copy(choices = path.choices.drop(1))
            return goToBranch(itemIdx, branchIdx)
                ?.addItem(item, nextPath)
                ?.let { oldNode.replaceAt(it, branchIdx) }
                ?.let { this.replaceAt(it, itemIdx) }
        }
    }

    fun addBranch(branch: GreenBranch<T>, iIdx: ItemIdx): GreenBranch<T>? {
        return getNode(iIdx)
            ?.add(branch)
            ?.let { this.replaceAt(it, iIdx) }
    }

    private fun addToEnd(node: Node<T>): GreenBranch<T> {
        return copy(body = this.body + node)
    }

    fun goToBranch(itemIdx: ItemIdx, branchIdx: BranchIdx): GreenBranch<T>? {
        return getNode(itemIdx)?.branches?.getOrNull(branchIdx)
    }

    fun hasAsFirstItem(item: T): Boolean {
        return firstItem == item
    }
}

// Invariant: No two branches have the same first item
internal class GreenBranching<T>(private val t: List<GreenBranch<T>> = emptyList()) {
    override fun toString(): String {
        return t.toString()
    }

    fun getOrNull(bIdx: BranchIdx): GreenBranch<T>? {
        return t.getOrNull(bIdx.t)
    }

    fun add(item: T): GreenBranching<T> {
        return if (contains(item)) {
            this
        } else {
            GreenBranching(t + GreenBranch(item))
        }
    }

    fun add(branch: GreenBranch<T>): GreenBranching<T> {
        return if (contains(branch.firstItem)) {
            this
        } else {
            GreenBranching(t + branch)
        }
    }

    fun replaceAt(branch: GreenBranch<T>, bIdx: BranchIdx): GreenBranching<T>? {
        return if (contains(branch.firstItem)) {
            this
        } else {
            t.replaceAt(branch, bIdx.t)
                ?.let(::GreenBranching)
        }
    }

    fun isEmpty(): Boolean {
        return t.isEmpty()
    }

    fun contains(item: T): Boolean {
        return t.any { it.hasAsFirstItem(item) }
    }

    fun listBranches(): List<GreenBranch<T>> {
        return t
    }
}
