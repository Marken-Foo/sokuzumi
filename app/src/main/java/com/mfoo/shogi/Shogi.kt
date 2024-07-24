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
data class Koma(val side: Side, val komaType: KomaType) {
    // Returns the CSA string representation of the koma. See the CSA spec.
    // Japanese: http://www2.computer-shogi.org/protocol/record_v22.html
    // English translation: https://github.com/Marken-Foo/shogi-translations/blob/main/CSA-standard.md
    fun toCsa(): String {
        val sideChar = when (side) {
            Side.SENTE -> '+'
            Side.GOTE -> '-'
        }
        val komaStr = when (komaType) {
            KomaType.FU -> "FU"
            KomaType.KY -> "KY"
            KomaType.KE -> "KE"
            KomaType.GI -> "GI"
            KomaType.KI -> "KI"
            KomaType.KA -> "KA"
            KomaType.HI -> "HI"
            KomaType.OU -> "OU"
            KomaType.TO -> "TO"
            KomaType.NY -> "NY"
            KomaType.NK -> "NK"
            KomaType.NG -> "NG"
            KomaType.UM -> "UM"
            KomaType.RY -> "RY"
        }
        return "${sideChar}${komaStr}"
    }
}

@JvmInline
value class Row(val t: Int)

@JvmInline
value class Col(val t: Int)

/**
 * Represents a square on a shogi board.
 *
 * The usual convention is that the first number is the column and second is the row;
 * the upper-right corner is 11 and the lower-left is 99.
 */
data class Square(val col: Col, val row: Row) {
    override fun toString(): String {
        return "Sq(${col.t}${row.t})"
    }

    companion object {
        fun all(): Iterable<Square> = (1..9).flatMap { x ->
            (1..9).map { y -> Square(Col(x), Row(y)) }
        }
    }
}
