package com.mfoo.shogi.rgbranches

internal class GreenRoot<T> private constructor(private val children: GreenBranching<T>) {
    constructor(branches: List<GreenBranch<T>>) : this(
        children = GreenBranching(
            branches
        )
    )

    override fun toString(): String {
        return children.toString()
    }

    fun goToBranch(branchIdx: BranchIdx): GreenBranch<T>? {
        return children.getOrNull(branchIdx)
    }

    fun findBranchIdx(item: T): BranchIdx? {
        return children.findBranchIdx(item)
    }

    fun pathOfItem(item: T, path: Path): Path.T? {
        return when (path) {
            Path.Root -> {
                findBranchIdx(item)?.let { Path.T(it, emptyList(), ItemIdx(0)) }
            }

            is Path.T -> {
                goToBranch(path.rootIdx)
                    ?.pathOfItem(item, path.getPartialPath())
                    ?.let { Path.T(path.rootIdx, it.choices, it.finalIdx) }
            }
        }
    }

    fun addItem(item: T, path: Path): GreenRoot<T>? {
        return when (path) {
            Path.Root -> return GreenRoot(children.add(item))
            is Path.T -> {
                goToBranch(path.rootIdx)
                    ?.addItem(item, path.getPartialPath())
                    ?.let { children.replaceAt(it, path.rootIdx) }
                    ?.let { GreenRoot(it) }
            }
        }
    }

    fun listBranches(): List<GreenBranch<T>> {
        return children.listBranches()
    }
}

/**
 * Immutable branch structure, unaware of global position.
 */
internal class GreenBranch<T> private constructor(
    val firstItem: T,
    internal val body: List<Node<T>> = emptyList(),
) {
    // Extra parameter to avoid JVM constructor issues
    constructor(head: T, tail: List<T>, x: Any? = null) :
        this(head, tail.map { Node(it, GreenBranching()) })

    constructor(head: T) : this(firstItem = head)

    internal class Node<T> internal constructor(
        val item: T,
        val branches: GreenBranching<T>,
    ) {
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

        fun listBranches(): List<GreenBranch<T>> {
            return this.branches.listBranches()
        }

        fun findBranchIdx(item: T): BranchIdx? {
            return this.branches.findBranchIdx(item)
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

    private fun copy(
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

    fun findBranchIdx(item: T, iIdx: ItemIdx): BranchIdx? {
        return this.getNode(iIdx)?.findBranchIdx(item)
    }

    private fun getNode(iIdx: ItemIdx): Node<T>? {
        return this.body.getOrNull(iIdx.decrement().t)
    }

    fun addItem(item: T, path: PartialPath): GreenBranch<T>? {
        val (head, tail) = path.pop()
        if (head == null) {
            val isAtBranchEnd = this.body.size == path.finalIdx.t
            if (isAtBranchEnd) {
                return copy(body = this.body + Node(item, GreenBranching()))
            }
            val node = getNode(path.finalIdx)
                ?: return null
            if (node.branches.contains(item)) {
                return this
            }
            return node.add(GreenBranch(item))
                .let { this.replaceAt(it, path.finalIdx) }
        } else {
            val (iIdx, bIdx) = head
            val oldNode = getNode(iIdx)
                ?: return null
            return goToBranch(iIdx, bIdx)
                ?.addItem(item, tail)
                ?.let { oldNode.replaceAt(it, bIdx) }
                ?.let { this.replaceAt(it, iIdx) }
        }
    }

    fun addBranch(branch: GreenBranch<T>, iIdx: ItemIdx): GreenBranch<T>? {
        return getNode(iIdx)
            ?.add(branch)
            ?.let { this.replaceAt(it, iIdx) }
    }

    fun goToBranch(itemIdx: ItemIdx, branchIdx: BranchIdx): GreenBranch<T>? {
        return getNode(itemIdx)?.branches?.getOrNull(branchIdx)
    }

    fun pathOfItem(item: T, path: PartialPath): PartialPath? {
        val (head, tail) = path.pop()
        return if (head == null) {
            pathOfItem(item, path.finalIdx)
        } else {
            val (iIdx, bIdx) = head
            goToBranch(iIdx, bIdx)
                ?.pathOfItem(item, tail)
                ?.let { it.copy(choices = listOf(iIdx to bIdx) + it.choices) }
        }
    }

    private fun pathOfItem(item: T, iIdx: ItemIdx): PartialPath? {
        val isOnMainPath = item == getAt(iIdx.increment())
        return if (isOnMainPath) {
            PartialPath(emptyList(), iIdx.increment())
        } else {
            getNode(iIdx)
                ?.branches
                ?.findBranchIdx(item)
                ?.let {
                    PartialPath(listOf(iIdx to it), ItemIdx(0))
                }
        }
    }

    fun hasAsFirstItem(item: T): Boolean {
        return firstItem == item
    }

    fun hasAt(item: T, iIdx: ItemIdx): Boolean {
        return getNode(iIdx)
            ?.let { it.item == item || it.branches.contains(item) }
            ?: false
    }

    fun size(): Int {
        return body.size
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

    fun findBranchIdx(item: T): BranchIdx? {
        return t.indexOfFirst { it.hasAsFirstItem(item) }
            .let { if (it == -1) null else BranchIdx(it) }
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

    fun size(): Int {
        return t.size
    }
}
