package com.mfoo.shogi.rgbranches

internal class GreenRoot<T> private constructor(private val children: GreenBranching<T>) {
    constructor(branches: List<GreenBranch<T>>) : this(
        children = GreenBranching(branches)
    )

    override fun toString(): String = children.toString()

    fun goToBranch(branchIdx: BranchIdx): GreenBranch<T>? {
        return children.getOrNull(branchIdx)
    }

    fun findBranchIdx(item: T): BranchIdx? {
        return children.findBranchIdx(item)
    }

    fun addItem(item: T, path: Path): Pair<GreenRoot<T>, Path.Full>? {
        when (path) {
            Path.Empty -> {
                val newRoot = GreenRoot(children.add(item))
                return newRoot.findBranchIdx(item)
                    ?.let { Pair(newRoot, Path.Full(rootIdx = it)) }
            }

            is Path.Full -> {
                val (newBranch, step, iIdx) = goToBranch(path.rootIdx)
                    ?.addItem(item, path.getPartialPath())
                    ?: return null
                val newRoot = newBranch
                    .let { children.replaceAt(it, path.rootIdx) }
                    ?.let { GreenRoot(it) }
                    ?: return null
                val newPath =
                    step?.let { path.addStep(it) } ?: path.copy(endIdx = iIdx)
                return Pair(newRoot, newPath)
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
            return if (branches.contains(item)) {
                this
            } else {
                Node(item, branches.add(branch))
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
        val itemsString = (items
            .mapIndexed { i, item -> "${(1 + i)}. ${item.toString()}" }
            .joinToString(separator = ", "))
        val branchesString = body
            .mapIndexed { i, node -> (2 + i) to node.branches }
            .filter { (_, branches) -> branches.size() > 0 }
            .joinToString("") { (idx, branches) ->
                "Index $idx alternative, $branches"
            }
        return "Items: ${itemsString}\n${branchesString}"
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

    fun getAll(): List<T> = listOf(firstItem) + body.map { it.item }

    fun findBranchIdx(item: T, iIdx: ItemIdx): BranchIdx? {
        return this.getNode(iIdx)?.findBranchIdx(item)
    }

    private fun getNode(iIdx: ItemIdx): Node<T>? {
        return this.body.getOrNull(iIdx.decrement().t)
    }

    fun addItem(
        item: T,
        path: PartialPath,
    ): Triple<GreenBranch<T>, Step?, ItemIdx>? {
        val (nextStep, restPath) = path.pop()
        if (nextStep == null) {
            return addItem(item, path.endIdx)
        } else {
            val oldNode = getNode(nextStep.iIdx) ?: return null
            val (newBranch, step, iIdx) = goToBranch(nextStep)
                ?.addItem(item, restPath)
                ?: return null
            return oldNode.replaceAt(newBranch, nextStep.bIdx)
                ?.let { this.replaceAt(it, nextStep.iIdx) }
                ?.let { Triple(it, step, iIdx) }
        }
    }

    private fun addItem(
        item: T,
        iIdx: ItemIdx,
    ): Triple<GreenBranch<T>, Step?, ItemIdx>? {
        val isAtBranchEnd = body.size == iIdx.t
        if (isAtBranchEnd) {
            val newBranch = copy(body = body + Node(item, GreenBranching()))
            return Triple(newBranch, null, iIdx.increment())
        }
        val node = getNode(iIdx) ?: return null
        val bIdx = node.findBranchIdx(item)
        return if (bIdx != null) {
            Triple(this, Step(iIdx, bIdx), ItemIdx(0))
        } else {
            val newBranch = node
                .add(GreenBranch(item))
                .let { this.replaceAt(it, iIdx) }
                ?: return null
            val newBIdx = newBranch.findBranchIdx(item, iIdx)
                ?: return null
            return Triple(newBranch, Step(iIdx, newBIdx), ItemIdx(0))
        }
    }

    fun addBranch(branch: GreenBranch<T>, iIdx: ItemIdx): GreenBranch<T>? {
        return getNode(iIdx)
            ?.add(branch)
            ?.let { this.replaceAt(it, iIdx) }
    }

    private fun goToBranch(step: Step): GreenBranch<T>? {
        return goToBranch(step.iIdx, step.bIdx)
    }

    fun goToBranch(itemIdx: ItemIdx, branchIdx: BranchIdx): GreenBranch<T>? {
        return getNode(itemIdx)?.branches?.getOrNull(branchIdx)
    }

    fun hasAsFirstItem(item: T): Boolean = firstItem == item

    fun size(): Int = 1 + body.size
}

// Invariant: No two branches have the same first item
internal class GreenBranching<T>(private val t: List<GreenBranch<T>> = emptyList()) {
    override fun toString(): String {
        return "-->\n${t.joinToString("") { it.toString() }}\n<--"
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
        return t.replaceAt(branch, bIdx.t)?.let(::GreenBranching)
    }

    fun contains(item: T): Boolean = t.any { it.hasAsFirstItem(item) }
    fun listBranches(): List<GreenBranch<T>> = t
    fun size(): Int = t.size
}
