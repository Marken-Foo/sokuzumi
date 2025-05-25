package com.mfoo.shogi.rgbranches

internal sealed interface Path {
    fun advance(): T

    data object Root : Path {
        override fun advance(): T {
            return Path.T(BranchIdx(0), emptyList(), ItemIdx(0))
        }

        fun goTo(bIdx: BranchIdx): T {
            return Path.T(rootIdx = bIdx)
        }
    }

    data class T(
        val rootIdx: BranchIdx = BranchIdx(0),
        val choices: List<Pair<ItemIdx, BranchIdx>> = emptyList(),
        val finalIdx: ItemIdx = ItemIdx(0),
    ) : Path {
        fun goTo(idx: ItemIdx, branchIdx: BranchIdx): T {
            return this.copy(
                choices = choices + (idx to branchIdx),
                finalIdx = ItemIdx(0)
            )
        }

        fun getPartialPath(): PartialPath {
            return PartialPath(choices, finalIdx)
        }

        override fun advance(): T {
            return this.copy(finalIdx = finalIdx.increment())
        }

        fun retract(): Path {
            return if (finalIdx.t == 0) {
                choices.lastOrNull()
                    ?.let { (iIdx, _) -> T(rootIdx, choices.dropLast(1), iIdx) }
                    ?: Root
            } else {
                this.copy(finalIdx = finalIdx.decrement())
            }
        }

        fun goTo(iIdx: ItemIdx): T {
            return this.copy(finalIdx = iIdx)
        }
    }
}

internal data class PartialPath(
    val choices: List<Pair<ItemIdx, BranchIdx>>,
    val finalIdx: ItemIdx,
) {
    fun append(itemIdx: ItemIdx, branchIdx: BranchIdx): PartialPath {
        return this.copy(choices = choices + (itemIdx to branchIdx))
    }

    fun pop(): Pair<Pair<ItemIdx, BranchIdx>?, PartialPath> {
        return Pair(choices.firstOrNull(), copy(choices = choices.drop(1)))
    }
}
