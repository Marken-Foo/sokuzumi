package com.mfoo.shogi

import arrow.core.Either

/**
 * Represents just the shogi board, without considering pieces in hand.
 */
interface Board {
    fun getKoma(sq: Square): Either<Unit, Koma?>
    fun setKoma(sq: Square, koma: Koma): Board
    fun removeKoma(sq: Square): Board
}

interface BoardFactory {
    fun empty(): Board
}
