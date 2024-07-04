package com.mfoo.shogi

enum class Side {
    SENTE {
        override fun isSente() = true
        override fun switch() = GOTE
    },
    GOTE {
        override fun isSente() = false
        override fun switch() = SENTE
    };

    abstract fun isSente(): Boolean
    abstract fun switch(): Side

    companion object {
        val SHITATE = SENTE
        val UWATE = GOTE
    }
}

/**
 * Represents a type of shogi piece.
 */
enum class KomaType {
    FU, KY, KE, GI, KI, KA, HI, OU, TO, NY, NK, NG, UM, RY
}

/**
 * Represents a shogi piece.
 */
data class Koma(val side: Side, val komaType: KomaType)

@JvmInline
value class Row(val int: Int)

@JvmInline
value class Col(val int: Int)

/**
 * Represents a square on a shogi board.
 *
 * The usual convention is that the first number is the column and second is the row;
 * the upper-right corner is 11 and the lower-left is 99.
 */
data class Square(val col: Col, val row: Row) {
    companion object {
        fun all(): Iterable<Square> = (1..9).flatMap { x ->
            (1..9).map { y -> Square(Col(x), Row(y)) }
        }
    }
}
