package com.mfoo.shogi.rules

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Move
import com.mfoo.shogi.Side
import com.mfoo.shogi.isValid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ValidMoveKyTests: FunSpec ({
    test("Sente KY moves north") {
        val side = Side.SENTE
        val komaType = KomaType.KY
        val startSq = sq(1, 7)
        val endSqs = listOf(
            TestCase(sq(1, 8), false),
            TestCase(sq(1, 6), true),
            TestCase(sq(1, 5), true),
            TestCase(sq(1, 4), true),
            TestCase(sq(1, 3), true),
            TestCase(sq(1, 2), true),
            // (1,1) cannot be a normal destination as it must promote
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Gote KY moves south") {
        val side = Side.GOTE
        val komaType = KomaType.KY
        val startSq = sq(9,3)
        val endSqs = listOf(
            TestCase(sq(9, 2), false),
            TestCase(sq(9, 4), true),
            TestCase(sq(9, 5), true),
            TestCase(sq(9, 6), true),
            TestCase(sq(9, 7), true),
            TestCase(sq(9, 8), true),
            // (9,9) cannot be a normal destination as it must promote
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Sente KY line is blocked by allied koma") {
        val side = Side.SENTE
        val komaType = KomaType.KY
        val startSq = sq(1, 7)
        val endSqs = listOf(
            TestCase(sq(1, 6), true),
            TestCase(sq(1, 5), true),
            TestCase(sq(1, 4), true),
            TestCase(sq(1, 3), false),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(1, 3) to Koma(side, KomaType.KY),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Gote KY line is blocked by allied koma") {
        val side = Side.GOTE
        val komaType = KomaType.KY
        val startSq = sq(9,3)
        val endSqs = listOf(
            TestCase(sq(9, 4), true),
            TestCase(sq(9, 5), true),
            TestCase(sq(9, 6), true),
            TestCase(sq(9, 7), false),
            TestCase(sq(9, 8), false),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(9, 7) to Koma(side, KomaType.KY),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Sente KY line is blocked by enemy komas but can capture them") {
        val side = Side.SENTE
        val komaType = KomaType.KY
        val startSq = sq(1, 7)
        val endSqs = listOf(
            TestCase(sq(1, 6), true),
            TestCase(sq(1, 5), true),
            TestCase(sq(1, 4), true),
            TestCase(sq(1, 3), true),
            TestCase(sq(1, 2), false),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(1, 3) to Koma(side.switch(), KomaType.KY),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Gote KY line is blocked by enemy komas but can capture them") {
        val side = Side.GOTE
        val komaType = KomaType.KY
        val startSq = sq(9,3)
        val endSqs = listOf(
            TestCase(sq(9, 4), true),
            TestCase(sq(9, 5), true),
            TestCase(sq(9, 6), true),
            TestCase(sq(9, 7), true),
            TestCase(sq(9, 8), false),
        )
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(9, 7) to Koma(side.switch(), KomaType.KY),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Sente KY can promote only when reaching last 3 rows") {
        val side = Side.SENTE
        val komaType = KomaType.KY
        val startSq = sq(6, 9)
        val komas = mapOf(startSq to Koma(side, komaType))
        val endSqs = setOf(
            TestCase(sq(6, 8), false),
            TestCase(sq(6, 7), false),
            TestCase(sq(6, 6), false),
            TestCase(sq(6, 5), false),
            TestCase(sq(6, 4), false),
            TestCase(sq(6, 3), true),
            TestCase(sq(6, 2), true),
            TestCase(sq(6, 1), true),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("Gote KY can promote only when reaching last 3 rows") {
        val side = Side.GOTE
        val komaType = KomaType.KY
        val startSq = sq(7, 1)
        val komas = mapOf(startSq to Koma(side, komaType))
        val endSqs = setOf(
            TestCase(sq(7, 2), false),
            TestCase(sq(7, 3), false),
            TestCase(sq(7, 4), false),
            TestCase(sq(7, 5), false),
            TestCase(sq(7, 6), false),
            TestCase(sq(7, 7), true),
            TestCase(sq(7, 8), true),
            TestCase(sq(7, 9), true),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("Sente KY must promote when moving to last row") {
        val side = Side.SENTE
        val komaType = KomaType.KY
        val startSq = sq(2, 2)
        val endSq = sq(2, 1)
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, listOf(TestCase(endSq, true)))
        testRegularMoves(komas, startSq, listOf(TestCase(endSq, false)))
    }

    test("Gote KY must promote when moving to last row") {
        val side = Side.GOTE
        val komaType = KomaType.KY
        val startSq = sq(4, 8)
        val endSq = sq(4, 9)
        val komas = mapOf(startSq to Koma(side, komaType))
        testPromotionMoves(komas, startSq, listOf(TestCase(endSq, true)))
        testRegularMoves(komas, startSq, listOf(TestCase(endSq, false)))
    }

    test("Sente KY cannot be dropped on last row") {
        val side = Side.SENTE
        val komaType = KomaType.KY
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

    test("Gote KY cannot be dropped on last row") {
        val side = Side.GOTE
        val komaType = KomaType.KY
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
