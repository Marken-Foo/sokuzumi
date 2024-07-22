package com.mfoo.shogi.rules

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Side
import io.kotest.core.spec.style.FunSpec

class ValidMoveUmTests : FunSpec({
    test("UM moves diagonally and one step orthogonally") {
        val side = Side.GOTE
        val komaType = KomaType.UM
        val startSq = sq(6, 7)
        val endSqs = listOf(
            TestCase(sq(7, 6), true),
            TestCase(sq(8, 5), true),
            TestCase(sq(9, 4), true),
            TestCase(sq(7, 8), true),
            TestCase(sq(8, 9), true),
            TestCase(sq(5, 6), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(3, 4), true),
            TestCase(sq(2, 3), true),
            TestCase(sq(1, 2), true),
            TestCase(sq(5, 8), true),
            TestCase(sq(4, 9), true),

            TestCase(sq(6, 6), true),
            TestCase(sq(6, 8), true),
            TestCase(sq(7, 7), true),
            TestCase(sq(5, 7), true),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("UM lines are blocked by allied komas") {
        val side = Side.GOTE
        val komaType = KomaType.UM
        val startSq = sq(6, 7)
        val endSqs = listOf(
            TestCase(sq(7, 6), true),
            TestCase(sq(8, 5), false),
            TestCase(sq(9, 4), false),
            TestCase(sq(7, 8), false),
            TestCase(sq(8, 9), false),
            TestCase(sq(5, 6), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(3, 4), true),
            TestCase(sq(2, 3), true),
            TestCase(sq(1, 2), true),
            TestCase(sq(5, 8), true),
            TestCase(sq(4, 9), true),

            TestCase(sq(6, 6), true),
            TestCase(sq(6, 8), false),
            TestCase(sq(7, 7), true),
            TestCase(sq(5, 7), true),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(7, 8) to Koma(side, KomaType.KA),
            sq(8, 5) to Koma(side, KomaType.RY),
            sq(6, 8) to Koma(side, KomaType.HI),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("UM lines are blocked by enemy komas but can capture them") {
        val side = Side.GOTE
        val komaType = KomaType.UM
        val startSq = sq(6, 7)
        val endSqs = listOf(
            TestCase(sq(7, 6), true),
            TestCase(sq(8, 5), true),
            TestCase(sq(9, 4), false),
            TestCase(sq(7, 8), true),
            TestCase(sq(8, 9), false),
            TestCase(sq(5, 6), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(3, 4), true),
            TestCase(sq(2, 3), true),
            TestCase(sq(1, 2), true),
            TestCase(sq(5, 8), true),
            TestCase(sq(4, 9), true),

            TestCase(sq(6, 6), true),
            TestCase(sq(6, 8), true),
            TestCase(sq(7, 7), true),
            TestCase(sq(5, 7), true),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(7, 8) to Koma(side.switch(), KomaType.KA),
            sq(8, 5) to Koma(side.switch(), KomaType.RY),
            sq(6, 8) to Koma(side.switch(), KomaType.HI),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("UM cannot promote") {
        val side = Side.SENTE
        val komaType = KomaType.UM
        val startSq = sq(4, 2)
        val komas = mapOf(startSq to Koma(side, komaType))
        val endSqs = listOf(
            TestCase(sq(5, 1), false),
            TestCase(sq(4, 1), false),
            TestCase(sq(3, 1), false),
            TestCase(sq(5, 2), false),
            TestCase(sq(3, 2), false),
            TestCase(sq(5, 3), false),
            TestCase(sq(4, 3), false),
            TestCase(sq(3, 3), false),
            TestCase(sq(6, 4), false),
            TestCase(sq(7, 5), false),
            TestCase(sq(8, 6), false),
            TestCase(sq(9, 7), false),
            TestCase(sq(2, 4), false),
            TestCase(sq(1, 5), false),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }
})
