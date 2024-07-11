package com.mfoo.shogi.sfen

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Side

sealed interface SfenAst {
    data class Row(val komas: List<Koma?>) : SfenAst {
        override fun toString(): String {
            return "Row[${komas.joinToString { koma -> koma?.toCsa() ?: " . " }}]"
        }
    }

    data class Board(val rows: List<Row>) : SfenAst {
        override fun toString(): String {
            return "Board:\n${rows.joinToString(separator = "\n") { row -> row.toString() }}"
        }
    }

    data class Hand(val contents: Map<KomaType, Int>) : SfenAst
    data class Hands(val senteHand: Hand, val goteHand: Hand) : SfenAst

    // Represents a valid shogi position
    data class ShogiPosition(
        val board: Board,
        val senteHand: Hand,
        val goteHand: Hand,
        val sideToMove: Side,
        val moveNumber: Int,
    ) : SfenAst
}
