package com.mfoo.shogi.rules

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Move
import com.mfoo.shogi.Side
import com.mfoo.shogi.isValid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ValidMoveKiTests : FunSpec({
    test("Sente KI step") {
        val side = Side.SENTE
        val startSq = sq(7, 8)
        val endSqs = listOf(
            TestCase(sq(8, 7), true),
            TestCase(sq(7, 7), true),
            TestCase(sq(6, 7), true),
            TestCase(sq(8, 8), true),
            TestCase(sq(6, 8), true),
            TestCase(sq(7, 9), true),
        )
        val komas = mapOf(
            startSq to Koma(side, KomaType.KI),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Gote KI step") {
        val side = Side.GOTE
        val startSq = sq(3, 5)
        val endSqs = listOf(
            TestCase(sq(3, 4), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(2, 5), true),
            TestCase(sq(4, 6), true),
            TestCase(sq(3, 6), true),
            TestCase(sq(2, 6), true),
        )
        val komas = mapOf(
            startSq to Koma(side, KomaType.KI),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Blocked sente KI step") {
        val side = Side.SENTE
        val startSq = sq(7, 8)
        val endSqs = listOf(
            TestCase(sq(8, 7), true),
            TestCase(sq(7, 7), true),
            TestCase(sq(6, 7), false),
            TestCase(sq(8, 8), false),
            TestCase(sq(6, 8), true),
            TestCase(sq(7, 9), true),
        )
        val komas = mapOf(
            startSq to Koma(side, KomaType.KI),
            sq(8, 8) to Koma(side, KomaType.KI),
            sq(6, 7) to Koma(side, KomaType.GI),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Sente KI captures") {
        val side = Side.SENTE
        val startSq = sq(7, 8)
        val endSqs = listOf(
            TestCase(sq(8, 7), true),
            TestCase(sq(7, 7), true),
            TestCase(sq(6, 7), true),
            TestCase(sq(8, 8), true),
            TestCase(sq(6, 8), true),
            TestCase(sq(7, 9), true),
        )
        val komas = mapOf(
            startSq to Koma(side, KomaType.KI),
            sq(8, 8) to Koma(side.switch(), KomaType.KI),
            sq(6, 7) to Koma(side.switch(), KomaType.GI),
        )

        testRegularMoves(
            komas,
            startSq = startSq,
            endSqs = endSqs,
        )
    }

    test("Sente legal KI drop") {
        val side = Side.SENTE
        val komaType = KomaType.KI
        val sq = sq(5, 3)
        val expected = true

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
