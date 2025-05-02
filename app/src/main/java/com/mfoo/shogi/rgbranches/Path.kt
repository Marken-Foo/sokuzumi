package com.mfoo.shogi.rgbranches


internal data class Path(
    val rootIdx: BranchIdx,
    val choices: List<Pair<ItemIdx, BranchIdx>>,
    val finalIdx: ItemIdx,
) {
    fun append(idx: ItemIdx, branchIdx: BranchIdx): Path {
        return this.copy(choices = choices + (idx to branchIdx))
    }
}

internal data class PartialPath(
    val choices: List<Pair<ItemIdx, BranchIdx>>,
    val finalIdx: ItemIdx,
) {
    fun append(itemIdx: ItemIdx, branchIdx: BranchIdx): PartialPath {
        return this.copy(choices = choices + (itemIdx to branchIdx))
    }
}
