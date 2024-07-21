package com.mfoo.shogi.rules

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Move
import com.mfoo.shogi.Side
import com.mfoo.shogi.isValid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ValidMoveKeTests : FunSpec({
    test("Sente KE faces north") {
        val side = Side.SENTE
        val komaType = KomaType.KE
        val startSq = sq(4, 5)
        val endSqs = listOf(
            TestCase(sq(5, 3), true),
            TestCase(sq(3, 3), true),
            TestCase(sq(5, 7), false),
            TestCase(sq(3, 7), false),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testRegularMoves(komas, startSq, endSqs)
    }

    test("Gote KE faces north") {
        val side = Side.GOTE
        val komaType = KomaType.KE
        val startSq = sq(4, 5)
        val endSqs = listOf(
            TestCase(sq(5, 3), false),
            TestCase(sq(3, 3), false),
            TestCase(sq(5, 7), true),
            TestCase(sq(3, 7), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testRegularMoves(komas, startSq, endSqs)
    }

    test("Sente KE cannot move onto allied koma") {
        val side = Side.SENTE
        val komaType = KomaType.KE
        val startSq = sq(4, 5)
        val endSqs = listOf(
            TestCase(sq(5, 3), true),
            TestCase(sq(3, 3), false),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(3, 3) to Koma(side, KomaType.KE),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("Sente KE can capture enemy koma") {
        val side = Side.SENTE
        val komaType = KomaType.KE
        val startSq = sq(4, 5)
        val endSqs = listOf(
            TestCase(sq(5, 3), true),
            TestCase(sq(3, 3), true),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(3, 3) to Koma(side.switch(), KomaType.KE),
        )
        testRegularMoves(komas, startSq, endSqs)
    }

    test("Sente KE can promote when reaching last 3 rows") {
        val side = Side.SENTE
        val komaType = KomaType.KE
        val startSq = sq(4, 5)
        val endSqs = listOf(
            TestCase(sq(5, 3), true),
            TestCase(sq(3, 3), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Gote KE can promote when reaching last 3 rows") {
        val side = Side.GOTE
        val komaType = KomaType.KE
        val startSq = sq(4, 5)
        val endSqs = listOf(
            TestCase(sq(5, 7), true),
            TestCase(sq(3, 7), true),
        )
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, endSqs)
    }

    test("Sente KE must promote when reaching 2nd last row") {
        val side = Side.SENTE
        val komaType = KomaType.KE
        val startSq = sq(1, 4)
        val endSq = sq(2, 2)
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, listOf(TestCase(endSq, true)))
        testRegularMoves(komas, startSq, listOf(TestCase(endSq, false)))
    }

    test("Gote KE must promote when reaching 2nd last row") {
        val side = Side.GOTE
        val komaType = KomaType.KE
        val startSq = sq(9, 6)
        val endSq = sq(8, 8)
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, listOf(TestCase(endSq, true)))
        testRegularMoves(komas, startSq, listOf(TestCase(endSq, false)))
    }

    test("Sente KE must promote when reaching last row") {
        val side = Side.SENTE
        val komaType = KomaType.KE
        val startSq = sq(1, 3)
        val endSq = sq(2, 1)
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, listOf(TestCase(endSq, true)))
        testRegularMoves(komas, startSq, listOf(TestCase(endSq, false)))
    }

    test("Gote KE must promote when reaching last row") {
        val side = Side.GOTE
        val komaType = KomaType.KE
        val startSq = sq(9, 7)
        val endSq = sq(8, 9)
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, listOf(TestCase(endSq, true)))
        testRegularMoves(komas, startSq, listOf(TestCase(endSq, false)))
    }

    test("Sente KE cannot be dropped on 2nd last row") {
        val side = Side.SENTE
        val komaType = KomaType.KE
        val sq = sq(4, 2)
        val expected = false

        val pos = Pos.empty().incrementHandAmount(side, komaType)
        val move = Move.Drop(
            sq = sq,
            side = side,
            komaType = komaType,
        )
        val result = isValid(move, pos)
        result shouldBe expected
    }

    test("Gote KE cannot be dropped on 2nd last row") {
        val side = Side.GOTE
        val komaType = KomaType.KE
        val sq = sq(6, 8)
        val expected = false

        val pos = Pos.empty().incrementHandAmount(side, komaType)
        val move = Move.Drop(
            sq = sq,
            side = side,
            komaType = komaType,
        )
        val result = isValid(move, pos)
        result shouldBe expected
    }

    test("Sente KE cannot be dropped on last row") {
        val side = Side.SENTE
        val komaType = KomaType.KE
        val sq = sq(4, 1)
        val expected = false

        val pos = Pos.empty().incrementHandAmount(side, komaType)
        val move = Move.Drop(
            sq = sq,
            side = side,
            komaType = komaType,
        )
        val result = isValid(move, pos)
        result shouldBe expected
    }

    test("Gote KE cannot be dropped on last row") {
        val side = Side.GOTE
        val komaType = KomaType.KE
        val sq = sq(6, 9)
        val expected = false

        val pos = Pos.empty().incrementHandAmount(side, komaType)
        val move = Move.Drop(
            sq = sq,
            side = side,
            komaType = komaType,
        )
        val result = isValid(move, pos)
        result shouldBe expected
    }
})
