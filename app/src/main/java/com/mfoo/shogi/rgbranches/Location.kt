package com.mfoo.shogi.rgbranches


internal sealed interface Location<T> {
    val current: Red<T>

    fun advance(): Location<T>?
    fun advanceIfPresent(item: T): Location<T>?
    fun getCurrentItem(): T?
    fun getNextItem(): T?
    fun isAtLeaf(): Boolean

    class Root<T>(override val current: RedRoot<T>) : Location<T> {
        override fun advance(): Location<T>? {
            return current.goToBranch(BranchIdx(0))
                ?.let {
                    NonRoot(
                        it,
                        Path(BranchIdx(0), emptyList(), ItemIdx(0))
                    )
                }
        }

        override fun advanceIfPresent(item: T): Location<T>? {
            val branchIdx =
                current.findBranchIdx { b -> b.hasAsFirstItem(item) }
                    ?: return null

            return current.goToBranch(branchIdx)
                ?.let { NonRoot(it, Path(branchIdx, emptyList(), ItemIdx(0))) }
        }

        override fun getCurrentItem(): T? {
            return null
        }

        override fun getNextItem(): T? {
            return current.goToBranch(BranchIdx(0))?.getAt(ItemIdx(0))
        }

        override fun isAtLeaf(): Boolean {
            return current.isEmpty()
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

        override fun getCurrentItem(): T? {
            return current.getAt(path.finalIdx)
        }

        override fun getNextItem(): T? {
            return current.getAt(path.finalIdx.increment())
        }

        override fun isAtLeaf(): Boolean {
            return !current.hasIndex(path.finalIdx.increment())
        }
    }
}
