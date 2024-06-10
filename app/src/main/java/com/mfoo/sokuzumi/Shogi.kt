package com.mfoo.sokuzumi

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
