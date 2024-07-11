package com.mfoo.shogi.bod

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.sfen.SfenAst

sealed interface BodAst {
    data class Row(val komas: List<Koma?>) : BodAst {
        override fun toString(): String {
            return "Row[${komas.joinToString { koma -> koma?.toCsa() ?: " . " }}]"
        }
    }

    data class Board(val rows: List<Row>) : BodAst {
        override fun toString(): String {
            return "Board:\n${rows.joinToString(separator = "\n") { row -> row.toString() }}"
        }
    }

    data class Hand(val contents: Map<KomaType, Int>) : BodAst

    data class Position(
        val board: Board,
        val senteHand: Hand,
        val goteHand: Hand,
    ) : BodAst
}