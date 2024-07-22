package com.mfoo.shogi.rules

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Side
import io.kotest.core.spec.style.FunSpec

class ValidMoveOuTests : FunSpec({
    test("OU moves") {
        val side = Side.GOTE
        val komaType = KomaType.OU
        val startSq = sq(4, 6)
        val endSqs = listOf(
            TestCase(sq(5, 5), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(3, 5), true),
            TestCase(sq(5, 6), true),
            TestCase(sq(3, 6), true),
            TestCase(sq(5, 7), true),
            TestCase(sq(4, 7), true),
            TestCase(sq(3, 7), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testRegularMoves(komas, startSq, endSqs)
    }

    test("OU cannot move onto allied koma") {
        val side = Side.SENTE
        val komaType = KomaType.OU
        val startSq = sq(4, 6)
        val endSqs = listOf(
            TestCase(sq(5, 5), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(3, 5), true),
            TestCase(sq(5, 6), true),
            TestCase(sq(3, 6), false),
            TestCase(sq(5, 7), false),
            TestCase(sq(4, 7), true),
            TestCase(sq(3, 7), true),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(3, 6) to Koma(side, KomaType.KE),
            sq(5, 7) to Koma(side, KomaType.FU),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("OU can capture enemy koma") {
        val side = Side.SENTE
        val komaType = KomaType.OU
        val startSq = sq(4, 6)
        val endSqs = listOf(
            TestCase(sq(5, 5), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(3, 5), true),
            TestCase(sq(5, 6), true),
            TestCase(sq(3, 6), true),
            TestCase(sq(5, 7), true),
            TestCase(sq(4, 7), true),
            TestCase(sq(3, 7), true),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(3, 6) to Koma(side.switch(), KomaType.KE),
            sq(5, 7) to Koma(side.switch(), KomaType.FU),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("OU cannot promote") {
        val side = Side.SENTE
        val komaType = KomaType.OU
        val startSq = sq(3, 2)
        val endSqs = listOf(
            TestCase(sq(4, 1), false),
            TestCase(sq(3, 1), false),
            TestCase(sq(2, 1), false),
            TestCase(sq(4, 2), false),
            TestCase(sq(2, 2), false),
            TestCase(sq(4, 3), false),
            TestCase(sq(3, 3), false),
            TestCase(sq(2, 3), false),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }
})
