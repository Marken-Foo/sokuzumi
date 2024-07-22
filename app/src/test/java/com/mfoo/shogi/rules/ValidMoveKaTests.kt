package com.mfoo.shogi.rules

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Side
import io.kotest.core.spec.style.FunSpec

class ValidMoveKaTests : FunSpec({
    test("KA moves diagonally") {
        val side = Side.GOTE
        val komaType = KomaType.KA
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
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("KA lines are blocked by allied komas") {
        val side = Side.GOTE
        val komaType = KomaType.KA
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
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(7, 8) to Koma(side, KomaType.KA),
            sq(8, 5) to Koma(side, KomaType.RY),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("KA lines are blocked by enemy komas but can capture them") {
        val side = Side.GOTE
        val komaType = KomaType.KA
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
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(7, 8) to Koma(side.switch(), KomaType.KA),
            sq(8, 5) to Koma(side.switch(), KomaType.RY),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("Sente KA can promote when reaching last 3 rows") {
        val side = Side.SENTE
        val komaType = KomaType.KA
        val startSq = sq(6, 4)
        val komas = mapOf(startSq to Koma(side, komaType))
        val endSqs = listOf(
            TestCase(sq(7, 3), true),
            TestCase(sq(8, 2), true),
            TestCase(sq(9, 1), true),
            TestCase(sq(5, 3), true),
            TestCase(sq(4, 2), true),
            TestCase(sq(3, 1), true),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("Gote KA can promote when reaching last 3 rows") {
        val side = Side.GOTE
        val komaType = KomaType.KA
        val startSq = sq(5, 6)
        val komas = mapOf(startSq to Koma(side, komaType))
        val endSqs = setOf(
            TestCase(sq(6, 7), true),
            TestCase(sq(7, 8), true),
            TestCase(sq(8, 9), true),
            TestCase(sq(4, 7), true),
            TestCase(sq(3, 8), true),
            TestCase(sq(2, 9), true),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("Sente KA can promote when moving out of last 3 rows") {
        val side = Side.SENTE
        val komaType = KomaType.KA
        val startSq = sq(5, 1)
        val komas = mapOf(startSq to Koma(side, komaType))
        val endSqs = listOf(
            TestCase(sq(6, 2), true),
            TestCase(sq(7, 3), true),
            TestCase(sq(8, 4), true),
            TestCase(sq(9, 5), true),
            TestCase(sq(4, 2), true),
            TestCase(sq(3, 3), true),
            TestCase(sq(2, 4), true),
            TestCase(sq(1, 5), true),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("Gote KA can promote when moving out of last 3 rows") {
        val side = Side.GOTE
        val komaType = KomaType.KA
        val startSq = sq(3, 8)
        val komas = mapOf(startSq to Koma(side, komaType))
        val endSqs = listOf(
            TestCase(sq(4, 9), true),
            TestCase(sq(4, 7), true),
            TestCase(sq(5, 6), true),
            TestCase(sq(6, 5), true),
            TestCase(sq(7, 4), true),
            TestCase(sq(8, 3), true),
            TestCase(sq(9, 2), true),
            TestCase(sq(2, 9), true),
            TestCase(sq(2, 7), true),
            TestCase(sq(1, 6), true),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("Sente KA cannot promote outside of last 3 rows") {
        val side = Side.SENTE
        val komaType = KomaType.KA
        val startSq = sq(3, 8)
        val komas = mapOf(startSq to Koma(side, komaType))
        val endSqs = listOf(
            TestCase(sq(4, 9), false),
            TestCase(sq(4, 7), false),
            TestCase(sq(5, 6), false),
            TestCase(sq(6, 5), false),
            TestCase(sq(7, 4), false),
            TestCase(sq(2, 9), false),
            TestCase(sq(2, 7), false),
            TestCase(sq(1, 6), false),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("Gote KA cannot promote outside of last 3 rows") {
        val side = Side.GOTE
        val komaType = KomaType.KA
        val startSq = sq(5, 1)
        val komas = mapOf(startSq to Koma(side, komaType))
        val endSqs = listOf(
            TestCase(sq(6, 2), false),
            TestCase(sq(7, 3), false),
            TestCase(sq(8, 4), false),
            TestCase(sq(9, 5), false),
            TestCase(sq(4, 2), false),
            TestCase(sq(3, 3), false),
            TestCase(sq(2, 4), false),
            TestCase(sq(1, 5), false),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }
})
