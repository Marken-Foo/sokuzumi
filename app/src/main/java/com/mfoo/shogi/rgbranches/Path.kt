package com.mfoo.shogi.rgbranches

internal sealed interface Path {
    data object Root : Path
    data class T(
        val rootIdx: BranchIdx,
        val choices: List<Pair<ItemIdx, BranchIdx>>,
        val finalIdx: ItemIdx,
    ) : Path {
        fun append(idx: ItemIdx, branchIdx: BranchIdx): T {
            return this.copy(choices = choices + (idx to branchIdx))
        }

        fun getPartialPath(): PartialPath {
            return PartialPath(choices, finalIdx)
        }
    }
}

//
//internal data class Path(
//    val rootIdx: BranchIdx,
//    val choices: List<Pair<ItemIdx, BranchIdx>>,
//    val finalIdx: ItemIdx,
//) {
//    fun append(idx: ItemIdx, branchIdx: BranchIdx): Path {
//        return this.copy(choices = choices + (idx to branchIdx))
//    }
//
//    fun getPartialPath(): PartialPath {
//        return PartialPath(choices, finalIdx)
//    }
//}

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
