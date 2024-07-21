package com.mfoo.shogi.rules

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Move
import com.mfoo.shogi.Side
import com.mfoo.shogi.isValid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ValidMoveFuTests : FunSpec({
    test("Sente FU moves north") {
        val side = Side.SENTE
        val startSq = sq(1, 8)
        val endSqs = listOf(
            TestCase(sq(1, 7), true),
            TestCase(sq(1, 9), false),
        )
        val komas = mapOf(
            startSq to Koma(side, KomaType.FU),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Gote FU moves south") {
        val side = Side.GOTE
        val startSq = sq(2, 5)
        val endSqs = listOf(
            TestCase(sq(2, 6), true),
            TestCase(sq(2, 4), false),
        )
        val komas = mapOf(
            startSq to Koma(side, KomaType.FU),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Sente FU cannot move onto allied koma") {
        val side = Side.SENTE
        val startSq = sq(1, 8)
        val endSqs = listOf(
            TestCase(sq(1, 7), false),
            TestCase(sq(1, 9), false),
        )
        val komas = mapOf(
            startSq to Koma(side, KomaType.FU),
            sq(1, 7) to Koma(side, KomaType.GI),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Sente FU can capture enemy koma") {
        val side = Side.SENTE
        val startSq = sq(1, 8)
        val endSqs = listOf(
            TestCase(sq(1, 7), true),
            TestCase(sq(1, 9), false),
        )
        val komas = mapOf(
            startSq to Koma(side, KomaType.FU),
            sq(1, 7) to Koma(side.switch(), KomaType.GI),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Sente FU can promote when reaching last 3 rows") {
        val side = Side.SENTE
        val startSq = sq(5, 4)
        val komas = mapOf(startSq to Koma(side, KomaType.FU))
        val endSqs = setOf(
            TestCase(sq(5, 3), true),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("Sente FU cannot promote when not reaching last 3 rows") {
        val side = Side.SENTE
        val startSq = sq(5, 5)
        val komas = mapOf(startSq to Koma(side, KomaType.FU))
        val endSqs = setOf(
            TestCase(sq(5, 4), false),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("Gote FU can promote when reaching last 3 rows") {
        val side = Side.GOTE
        val startSq = sq(9, 8)
        val komas = mapOf(startSq to Koma(side, KomaType.FU))
        val endSqs = setOf(
            TestCase(sq(9, 9), true),
        )
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("Sente FU must promote when moving to last row") {
        val side = Side.SENTE
        val startSq = sq(2, 2)
        val endSq = sq(2, 1)
        val komas = mapOf(startSq to Koma(side, KomaType.FU))
        testPromotionMoves(komas, startSq, listOf(TestCase(endSq, true)))
        testRegularMoves(komas, startSq, listOf(TestCase(endSq, false)))
    }

    test("Gote FU must promote when moving to last row") {
        val side = Side.GOTE
        val startSq = sq(4, 8)
        val endSq = sq(4, 9)
        val komas = mapOf(startSq to Koma(side, KomaType.FU))
        testPromotionMoves(komas, startSq, listOf(TestCase(endSq, true)))
        testRegularMoves(komas, startSq, listOf(TestCase(endSq, false)))
    }

    test("Sente FU nifu is invalid") {
        val side = Side.SENTE
        val komaType = KomaType.FU
        val sq = sq(5, 3)
        val expected = false

        val pos = Pos.empty()
            .setKoma(sq(5, 7), Koma(side, komaType))
            .incrementHandAmount(side, komaType)
        val move = Move.Drop(
            sq = sq,
            side = side,
            komaType = komaType,
        )
        val result = isValid(move, pos)
        result shouldBe expected
    }

    test("Sente FU can be dropped if not nifu") {
        val side = Side.SENTE
        val komaType = KomaType.FU
        val sq = sq(5, 3)
        val expected = true

        val pos = Pos.empty()
            .setKoma(sq(5, 7), Koma(side.switch(), KomaType.FU))
            .setKoma(sq(5, 6), Koma(side, KomaType.TO))
            .setKoma(sq(4, 3), Koma(side, KomaType.FU))
            .incrementHandAmount(side, komaType)
        val move = Move.Drop(
            sq = sq,
            side = side,
            komaType = komaType,
        )
        val result = isValid(move, pos)
        result shouldBe expected
    }

    test("Sente FU cannot be dropped on last row") {
        val side = Side.SENTE
        val komaType = KomaType.FU
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

    test("Gote FU cannot be dropped on last row") {
        val side = Side.GOTE
        val komaType = KomaType.FU
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
