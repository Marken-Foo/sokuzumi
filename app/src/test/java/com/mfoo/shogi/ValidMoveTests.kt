package com.mfoo.shogi

import arrow.core.fold
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

private val Pos: PositionFactory = PositionImpl

private fun PositionFactory.fromMap(komas: Map<Square, Koma>): Position {
    return komas.fold(this.empty()) { pos, (sq, koma) ->
        pos.setKoma(sq, koma)
    }
}

private fun sq(col: Int, row: Int): Square {
    return Square(Col(col), Row(row))
}

private fun testRegularMove(
    komas: Map<Square, Koma>,
    startSq: Square,
    endSq: Square,
    isPromotion: Boolean = false,
    expected: Boolean,
) {
    val pos = Pos.fromMap(komas)
    val koma = komas[startSq]
    koma shouldNotBe null

    val move = Move.Regular(
        startSq = startSq,
        endSq = endSq,
        isPromotion = isPromotion,
        side = koma!!.side,
        komaType = koma.komaType,
        capturedKoma = komas[endSq]
    )
    val result = isValid(move, pos)
    result shouldBe expected
}

class ValidMoveTests : FunSpec({
    context("FU moves") {
        test("Sente FU step") {
            val startSq = sq(1, 8)
            val endSq = sq(1, 7)
            val komas = mapOf(
                startSq to Koma(Side.SENTE, KomaType.FU),
            )
            val expected = true

            testRegularMove(
                komas,
                startSq = startSq,
                endSq = endSq,
                isPromotion = false,
                expected = expected,
            )
        }

        test("Incorrect sente FU step") {
            val startSq = sq(2, 5)
            val endSq = sq(2, 6)
            val komas = mapOf(
                startSq to Koma(Side.SENTE, KomaType.FU),
            )
            val expected = false

            testRegularMove(
                komas,
                startSq = startSq,
                endSq = endSq,
                isPromotion = false,
                expected = expected,
            )
        }

        test("Gote FU step") {
            val startSq = sq(2, 5)
            val endSq = sq(2, 6)
            val komas = mapOf(
                startSq to Koma(Side.GOTE, KomaType.FU),
            )
            val expected = true

            testRegularMove(
                komas,
                startSq = startSq,
                endSq = endSq,
                isPromotion = false,
                expected = expected,
            )
        }

        test("Incorrect gote FU step") {
            val startSq = sq(1, 8)
            val endSq = sq(1, 7)
            val komas = mapOf(
                startSq to Koma(Side.GOTE, KomaType.FU),
            )
            val expected = false

            testRegularMove(
                komas,
                startSq = startSq,
                endSq = endSq,
                isPromotion = false,
                expected = expected,
            )
        }

        test("Blocked sente FU step") {
            val startSq = sq(1, 8)
            val endSq = sq(1, 7)
            val komas = mapOf(
                startSq to Koma(Side.SENTE, KomaType.FU),
                endSq to Koma(Side.SENTE, KomaType.GI),
            )
            val expected = false

            testRegularMove(
                komas,
                startSq = startSq,
                endSq = endSq,
                isPromotion = false,
                expected = expected,
            )
        }

        test("Capture sente FU step") {
            val startSq = sq(1, 8)
            val endSq = sq(1, 7)
            val komas = mapOf(
                startSq to Koma(Side.SENTE, KomaType.FU),
                endSq to Koma(Side.GOTE, KomaType.GI),
            )
            val expected = true

            testRegularMove(
                komas,
                startSq = startSq,
                endSq = endSq,
                isPromotion = false,
                expected = expected,
            )
        }

        test("Sente FU valid promote") {
            val startSq = sq(5, 4)
            val endSq = sq(5, 3)
            val komas = mapOf(
                startSq to Koma(Side.SENTE, KomaType.FU),
            )
            val expected = true
            testRegularMove(
                komas,
                startSq = startSq,
                endSq = endSq,
                isPromotion = true,
                expected = expected,
            )
        }

        test("Sente FU invalid promote") {
            val startSq = sq(5, 5)
            val endSq = sq(5, 4)
            val komas = mapOf(
                startSq to Koma(Side.SENTE, KomaType.FU),
            )
            val expected = false
            testRegularMove(
                komas,
                startSq = startSq,
                endSq = endSq,
                isPromotion = true,
                expected = expected,
            )
        }

        test("Sente FU nifu") {
            val side = Side.SENTE
            val komaType = KomaType.FU
            val sq = sq(5, 3)
            val expected = false

            val pos = Pos.empty()
                .setKoma(sq, Koma(side, komaType))
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

        test("Sente legal FU drop") {
            val side = Side.SENTE
            val komaType = KomaType.FU
            val sq = sq(5, 3)
            val expected = true

            val pos = Pos.empty()
                .setKoma(sq, Koma(side, komaType))
                .setKoma(sq(5, 7), Koma(side.switch(), komaType))
                .setKoma(sq(5, 6), Koma(side, KomaType.TO))
                .setKoma(sq(4, 3), Koma(side, komaType))
                .incrementHandAmount(side, komaType)
            val move = Move.Drop(
                sq = sq,
                side = side,
                komaType = komaType,
            )
            val result = isValid(move, pos)
            result shouldBe expected
        }
    }
})