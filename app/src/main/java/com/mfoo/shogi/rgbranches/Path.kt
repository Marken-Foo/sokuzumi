package com.mfoo.shogi.rgbranches

internal sealed interface Path {
    fun advance(): Full

    data object Empty : Path {
        override fun advance(): Full {
            return Full(BranchIdx(0), emptyList(), ItemIdx(0))
        }

        fun goTo(bIdx: BranchIdx): Full {
            return Full(rootIdx = bIdx)
        }
    }

    data class Full(
        val rootIdx: BranchIdx = BranchIdx(0),
        val steps: List<Step> = emptyList(),
        val endIdx: ItemIdx = ItemIdx(0),
    ) : Path {

        fun goTo(iIdx: ItemIdx): Full = this.copy(endIdx = iIdx)
        fun goTo(idx: ItemIdx, branchIdx: BranchIdx): Full {
            return this.copy(
                steps = steps + Step(idx, branchIdx),
                endIdx = ItemIdx(0)
            )
        }

        fun getPartialPath(): PartialPath {
            return PartialPath(steps, endIdx)
        }

        override fun advance(): Full = this.copy(endIdx = endIdx.increment())

        fun retract(): Path {
            return if (endIdx.t == 0) {
                steps.lastOrNull()
                    ?.let { (iIdx, _) ->
                        Full(rootIdx, steps.dropLast(1), iIdx)
                    }
                    ?: Empty
            } else {
                this.copy(endIdx = endIdx.decrement())
            }
        }

        fun addStep(step: Step, iIdx: ItemIdx = ItemIdx(0)): Full {
            return this.copy(steps = steps + step, endIdx = iIdx)
        }
    }
}

internal data class Step(val iIdx: ItemIdx, val bIdx: BranchIdx)

internal data class PartialPath(
    val steps: List<Step>,
    val endIdx: ItemIdx,
) {
    fun pop(): Pair<Step?, PartialPath> {
        return Pair(steps.firstOrNull(), copy(steps = steps.drop(1)))
    }
}
