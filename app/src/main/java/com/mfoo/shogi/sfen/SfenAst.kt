package com.mfoo.shogi.sfen

sealed interface SfenAst {
    enum class KomaType : SfenAst {
        FU, KY, KE, GI, KI, KA, HI, OU, TO, NY, NK, NG, UM, RY
    }

    enum class Side : SfenAst { SENTE, GOTE }

    data class Koma(val side: Side, val komaType: KomaType) {
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
