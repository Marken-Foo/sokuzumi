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

    val side = koma!!.side

    val move = Move.Regular(
        startSq = startSq,
        endSq = endSq,
        isPromotion = isPromotion,
        side = koma.side,
        komaType = koma.komaType,
        capturedKoma = komas[endSq]
    )
    val result = isValid(move, pos.let {
        if (side.isSente()) {
            it
        } else {
            it.toggleSideToMove()
        }
    })
    result shouldBe expected
}

private fun testRegularMoves(
    komas: Map<Square, Koma>,
    startSq: Square,
    endSqs: Iterable<Square>,
) {
    val pos = Pos.fromMap(komas)
    val koma = komas[startSq]
    koma shouldNotBe null

    val side = koma!!.side

    val moves = endSqs.map {
        Move.Regular(
            startSq = startSq,
            endSq = it,
            isPromotion = false,
            side = koma.side,
            komaType = koma.komaType,
            capturedKoma = komas[it]
        )
    }
    val results = moves.map { move ->
        isValid(move, pos.let {
            if (side.isSente()) {
                it
            } else {
                it.toggleSideToMove()
            }
        })
    }
    results.all { isValid -> isValid } shouldBe true
}

class ValidMoveTests : FunSpec({
    context("General tests") {
        test("Should not be able to drop koma if koma not in hand") {
            val komaType = KomaType.GI
            val side = Side.SENTE
            val pos = Pos.empty().incrementHandAmount(side.switch(), komaType)
            val move = Move.Drop(sq(5, 5), side, komaType)
            val result = isValid(move, pos)
            val expected = false
            result shouldBe expected
        }
    }

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

    context("KI moves") {
        test("Sente KI step") {
            val side = Side.SENTE
            val startSq = sq(7, 8)
            val endSqs = listOf(
                sq(8, 7),
                sq(7, 7),
                sq(6, 7),
                sq(8, 8),
                sq(6, 8),
                sq(7, 9)
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
                sq(3, 4),
                sq(4, 5),
                sq(2, 5),
                sq(4, 6),
                sq(3, 6),
                sq(2, 6)
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
                sq(8, 7),
                sq(7, 7),
                sq(6, 8),
                sq(7, 9)
            )
            val komas = mapOf(
                startSq to Koma(side, KomaType.KI),
                sq(8,8) to Koma(side, KomaType.KI),
                sq(6,7) to Koma(side, KomaType.GI),
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
                sq(8, 7),
                sq(7, 7),
                sq(6, 7),
                sq(8, 8),
                sq(6, 8),
                sq(7, 9)
            )
            val komas = mapOf(
                startSq to Koma(side, KomaType.KI),
                sq(8,8) to Koma(side.switch(), KomaType.KI),
                sq(6,7) to Koma(side.switch(), KomaType.GI),
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
    }
})