package com.mfoo.shogi

import arrow.core.Either

/**
 * Represents a single shogi move, implicitly dependent on some position.
 */
// This has no explicit dependence on Position to let a Move be defined without
// explicitly stating a position, e.g. while parsing a kifu or interpreting
// user input as a move.
sealed interface Move {
    enum class GameEndType {
        ABORT, RESIGN, JISHOGI, SENNICHITE, FLAG, ILLEGAL_WIN, ILLEGAL_LOSS,
        NYUUGYOKU, NO_CONTEST_WIN, NO_CONTEST_LOSS, MATE, NO_MATE,
    }

    /**
     * Represents a single shogi move in some implicit position (not a drop;
     * includes promotions and captures).
     */
    // Information that could be deduced from a position (capturedKoma,
    // komaType) is given to allow straightforward undo of moves.
    data class Regular(
        val startSq: Square,
        val endSq: Square,
        val isPromotion: Boolean,
        val side: Side,
        val komaType: KomaType,
        val capturedKoma: Koma?,
    ) : Move {
        override fun toString(): String {
            val promote = if (isPromotion) "+" else ""
            val capture = capturedKoma?.let { "x${it.komaType.name}" } ?: ""
            return "${komaType.name}-${endSq}${promote} (${startSq}) (${capture})"
        }
    }

    data class Drop(
        val sq: Square,
        val side: Side,
        val komaType: KomaType,
    ) : Move {
        override fun toString(): String {
            return "${komaType.name}*${sq}"
        }
    }

    data class GameEnd(val endType: GameEndType) : Move {
        override fun toString(): String {
            return endType.name
        }
    }
}
