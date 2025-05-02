package com.mfoo.shogi.rgbranches

import kotlin.math.max

/**
 * Represents the (0-indexed) position of an item within a single branch.
 */
@JvmInline
internal value class ItemIdx(val t: Int) {
    fun increment(): ItemIdx = ItemIdx(this.t + 1)

    fun decrement(): ItemIdx = ItemIdx(max(0, this.t - 1))
}

/**
 * Represents the index of a branch among the options at one item.
 */
@JvmInline
internal value class BranchIdx(val t: Int)

internal fun <S> List<S>.replaceAt(item: S, idx: Int): List<S>? {
    return if (idx < 0 || this.size <= idx) {
        null
    } else {
        this.slice(0..<idx) + item + this.slice(idx + 1..<this.size)
    }
}
