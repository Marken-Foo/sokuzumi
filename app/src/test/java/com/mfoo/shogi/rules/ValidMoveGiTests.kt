package com.mfoo.shogi.rules

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Side
import io.kotest.core.spec.style.FunSpec

class ValidMoveGiTests : FunSpec({
    test("Sente GI faces north") {
        val side = Side.SENTE
        val komaType = KomaType.GI
        val startSq = sq(4, 6)
        val endSqs = listOf(
            TestCase(sq(5, 5), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(3, 5), true),
            TestCase(sq(5, 7), true),
            TestCase(sq(4, 7), false),
            TestCase(sq(3, 7), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testRegularMoves(komas, startSq, endSqs)
    }

    test("Gote GI faces south") {
        val side = Side.GOTE
        val komaType = KomaType.GI
        val startSq = sq(4, 6)
        val endSqs = listOf(
            TestCase(sq(5, 5), true),
            TestCase(sq(4, 5), false),
            TestCase(sq(3, 5), true),
            TestCase(sq(5, 7), true),
            TestCase(sq(4, 7), true),
            TestCase(sq(3, 7), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testRegularMoves(komas, startSq, endSqs)
    }

    test("Sente GI cannot move onto allied koma") {
        val side = Side.SENTE
        val komaType = KomaType.GI
        val startSq = sq(4, 6)
        val endSqs = listOf(
            TestCase(sq(5, 5), true),
            TestCase(sq(4, 5), false),
            TestCase(sq(3, 5), true),
            TestCase(sq(5, 7), false),
            TestCase(sq(3, 7), true),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(4, 5) to Koma(side, KomaType.NG),
            sq(5, 7) to Koma(side, KomaType.NG),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("Sente GI can capture enemy koma") {
        val side = Side.SENTE
        val komaType = KomaType.GI
        val startSq = sq(4, 6)
        val endSqs = listOf(
            TestCase(sq(5, 5), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(3, 5), true),
            TestCase(sq(5, 7), true),
            TestCase(sq(3, 7), true),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(4, 5) to Koma(side.switch(), KomaType.NG),
            sq(5, 7) to Koma(side.switch(), KomaType.NG),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("Sente GI cannot promote outside promotion zone") {
        val side = Side.SENTE
        val komaType = KomaType.GI
        val startSq = sq(3, 7)
        val endSqs = listOf(
            TestCase(sq(4, 6), false),
            TestCase(sq(3, 6), false),
            TestCase(sq(2, 6), false),
            TestCase(sq(4, 8), false),
            TestCase(sq(2, 8), false),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Gote GI cannot promote outside promotion zone") {
        val side = Side.GOTE
        val komaType = KomaType.GI
        val startSq = sq(5, 3)
        val endSqs = listOf(
            TestCase(sq(6, 4), false),
            TestCase(sq(5, 4), false),
            TestCase(sq(4, 4), false),
            TestCase(sq(6, 2), false),
            TestCase(sq(5, 2), false),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Sente GI can promote when reaching 3rd last row") {
        val side = Side.SENTE
        val komaType = KomaType.GI
        val startSq = sq(3, 4)
        val endSqs = listOf(
            TestCase(sq(4, 3), true),
            TestCase(sq(3, 3), true),
            TestCase(sq(2, 3), true),
            TestCase(sq(4, 5), false),
            TestCase(sq(2, 5), false),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Gote GI can promote when reaching 3rd last row") {
        val side = Side.GOTE
        val komaType = KomaType.GI
        val startSq = sq(5, 6)
        val endSqs = listOf(
            TestCase(sq(6, 7), true),
            TestCase(sq(5, 7), true),
            TestCase(sq(4, 7), true),
            TestCase(sq(6, 5), false),
            TestCase(sq(5, 5), false),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Sente GI can promote when reaching 2nd last row") {
        val side = Side.SENTE
        val komaType = KomaType.GI
        val startSq = sq(4, 1)
        val endSqs = listOf(
            TestCase(sq(5, 2), true),
            TestCase(sq(3, 2), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Gote GI can promote when reaching 2nd last row") {
        val side = Side.GOTE
        val komaType = KomaType.GI
        val startSq = sq(4, 9)
        val endSqs = listOf(
            TestCase(sq(5, 8), true),
            TestCase(sq(3, 8), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Sente GI can promote when reaching last row") {
        val side = Side.SENTE
        val komaType = KomaType.GI
        val startSq = sq(4, 2)
        val endSqs = listOf(
            TestCase(sq(5, 1), true),
            TestCase(sq(4, 1), true),
            TestCase(sq(3, 1), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Gote GI can promote when reaching last row") {
        val side = Side.GOTE
        val komaType = KomaType.GI
        val startSq = sq(4, 8)
        val endSqs = listOf(
            TestCase(sq(5, 9), true),
            TestCase(sq(4, 9), true),
            TestCase(sq(3, 9), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Sente GI can promote when moving out of promotion zone") {
        val side = Side.SENTE
        val komaType = KomaType.GI
        val startSq = sq(4, 3)
        val endSqs = listOf(
            TestCase(sq(5, 4), true),
            TestCase(sq(3, 4), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Gote GI can promote when moving out of promotion zone") {
        val side = Side.GOTE
        val komaType = KomaType.GI
        val startSq = sq(4, 7)
        val endSqs = listOf(
            TestCase(sq(5, 6), true),
            TestCase(sq(3, 6), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }
})
