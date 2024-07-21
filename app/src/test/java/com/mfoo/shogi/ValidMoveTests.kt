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
    val koma = komas[startSq]
    koma shouldNotBe null

    val side = koma!!.side
    val pos = Pos.fromMap(komas).setSideToMove(side)

    val move = Move.Regular(
        startSq = startSq,
        endSq = endSq,
        isPromotion = isPromotion,
        side = side,
        komaType = koma.komaType,
        capturedKoma = komas[endSq]
    )
    val result = isValid(move, pos)
    result shouldBe expected
}

private data class TestCase(val sq: Square, val shouldBeValid: Boolean)

private fun testRegularMoves(
    komas: Map<Square, Koma>,
    startSq: Square,
    endSqs: Iterable<TestCase>,
) {
    val koma = komas[startSq]
    koma shouldNotBe null

    val side = koma!!.side
    val pos = Pos.fromMap(komas).setSideToMove(side)

    endSqs.all { (sq, shouldBeValid) ->
        val move = Move.Regular(
            startSq = startSq,
            endSq = sq,
            isPromotion = false,
            side = side,
            komaType = koma.komaType,
            capturedKoma = komas[sq]
        )
        isValid(move, pos) == shouldBeValid
    } shouldBe true
}

private fun testPromotionMoves(
    komas: Map<Square, Koma>,
    startSq: Square,
    endSqs: Iterable<Square>,
) {
    val koma = komas[startSq]
    koma shouldNotBe null

    val side = koma!!.side
    val pos = Pos.fromMap(komas).setSideToMove(side)

    val moves = endSqs.map {
        Move.Regular(
            startSq = startSq,
            endSq = it,
            isPromotion = true,
            side = side,
            komaType = koma.komaType,
            capturedKoma = komas[it]
        )
    }
    moves.all { move -> isValid(move, pos) } shouldBe true
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

        test("Should not be able to drop koma if ally on square") {
            val komaType = KomaType.GI
            val side = Side.SENTE
            val sq = sq(5, 5)
            val pos = Pos.empty()
                .setKoma(sq, Koma(side, KomaType.NG))
                .incrementHandAmount(side, komaType)
            val move = Move.Drop(sq, side, komaType)
            val result = isValid(move, pos)
            val expected = false
            result shouldBe expected
        }

        test("Should not be able to drop koma if enemy on square") {
            val komaType = KomaType.GI
            val side = Side.SENTE
            val sq = sq(5, 5)
            val pos = Pos.empty()
                .setKoma(sq, Koma(side.switch(), KomaType.NG))
                .incrementHandAmount(side, komaType)
            val move = Move.Drop(sq, side, komaType)
            val result = isValid(move, pos)
            val expected = false
            result shouldBe expected
        }
    }

    context("FU moves") {
        test("Sente FU step") {
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

        test("Gote FU step") {
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

        test("Blocked sente FU step") {
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

        test("Capture sente FU step") {
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
    }

    context("HI moves") {
        test("Regular step") {
            val side = Side.GOTE
            val startSq = sq(3, 4)
            val endSqs = (1..9)
                .fold(emptySet<Square>()) { acc, n ->
                    acc
                        .plus(Square(startSq.col, Row(n)))
                        .plus(Square(Col(n), startSq.row))
                }
                .minus(startSq)
                .map { TestCase(it, true) }
            val komas = mapOf(
                startSq to Koma(side, KomaType.HI),
            )

            testRegularMoves(
                komas,
                startSq = startSq,
                endSqs = endSqs,
            )
        }

        test("Blocked") {
            val side = Side.GOTE
            val startSq = sq(3, 4)
            val komas = mapOf(
                startSq to Koma(side, KomaType.HI),
                sq(3, 1) to Koma(side, KomaType.KE),
                sq(3, 7) to Koma(side, KomaType.TO),
                sq(2, 4) to Koma(side, KomaType.UM),
                sq(7, 4) to Koma(side, KomaType.NY),
            )
            val endSqs = setOf(
                TestCase(sq(3, 1), false),
                TestCase(sq(3, 2), true),
                TestCase(sq(3, 3), true),
                TestCase(sq(3, 5), true),
                TestCase(sq(3, 6), true),
                TestCase(sq(3, 7), false),
                TestCase(sq(2, 4), false),
                TestCase(sq(4, 4), true),
                TestCase(sq(5, 4), true),
                TestCase(sq(6, 4), true),
                TestCase(sq(7, 4), false),
            )
            testRegularMoves(komas, startSq = startSq, endSqs = endSqs)
        }

        test("Captures") {
            val side = Side.GOTE
            val startSq = sq(3, 4)
            val komas = mapOf(
                startSq to Koma(side, KomaType.HI),
                sq(3, 1) to Koma(side.switch(), KomaType.KE),
                sq(3, 7) to Koma(side.switch(), KomaType.TO),
                sq(2, 4) to Koma(side.switch(), KomaType.UM),
                sq(7, 4) to Koma(side.switch(), KomaType.NY),
            )
            val endSqs = setOf(
                TestCase(sq(3, 1), true),
                TestCase(sq(3, 2), true),
                TestCase(sq(3, 3), true),
                TestCase(sq(3, 5), true),
                TestCase(sq(3, 6), true),
                TestCase(sq(3, 7), true),
                TestCase(sq(2, 4), true),
                TestCase(sq(4, 4), true),
                TestCase(sq(5, 4), true),
                TestCase(sq(6, 4), true),
                TestCase(sq(7, 4), true),
            )
            testRegularMoves(komas, startSq = startSq, endSqs = endSqs)
        }

        test("Sente promotions into promotion zone") {
            val side = Side.SENTE
            val startSq = sq(3, 4)
            val komas = mapOf(startSq to Koma(side, KomaType.HI))
            val endSqs = setOf(
                sq(3, 1), sq(3, 2), sq(3, 3)
            )
            testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
        }

        test("Gote promotions into promotion zone") {
            val side = Side.GOTE
            val startSq = sq(3, 4)
            val komas = mapOf(startSq to Koma(side, KomaType.HI))
            val endSqs = setOf(
                sq(3, 7), sq(3, 8), sq(3, 9)
            )
            testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
        }

        test("Sente promotions from promotion zone") {
            val side = Side.SENTE
            val startSq = sq(5, 1)
            val komas = mapOf(startSq to Koma(side, KomaType.HI))
            val endSqs = (1..9)
                .fold(emptySet<Square>()) { acc, n ->
                    acc
                        .plus(Square(startSq.col, Row(n)))
                        .plus(Square(Col(n), startSq.row))
                }
                .minus(startSq)
            testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
        }

        test("Gote promotions from promotion zone") {
            val side = Side.GOTE
            val startSq = sq(2, 8)
            val komas = mapOf(startSq to Koma(side, KomaType.HI))
            val endSqs = (1..9)
                .fold(emptySet<Square>()) { acc, n ->
                    acc
                        .plus(Square(startSq.col, Row(n)))
                        .plus(Square(Col(n), startSq.row))
                }
                .minus(startSq)
            testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
        }
    }
})