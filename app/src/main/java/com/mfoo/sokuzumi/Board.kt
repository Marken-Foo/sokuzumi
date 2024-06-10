package com.mfoo.sokuzumi

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

class MailboxBoard private constructor() : Board {
    override fun getKoma(sq: Square): Either<Unit, Koma?> {
        TODO("Not yet implemented")
    }

    override fun setKoma(sq: Square, koma: Koma): Board {
        TODO("Not yet implemented")
    }

    override fun removeKoma(sq: Square): Board {
        TODO("Not yet implemented")
    }

    companion object : BoardFactory {
        override fun empty(): Board {
            TODO("Not yet implemented")
        }
    }
}