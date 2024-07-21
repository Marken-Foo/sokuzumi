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
            isPromotion = true,
            side = side,
            komaType = koma.komaType,
            capturedKoma = komas[sq]
        )
        isValid(move, pos) == shouldBeValid
    } shouldBe true
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
                TestCase(sq(3, 1), true),
                TestCase(sq(3, 2), true),
                TestCase(sq(3, 3), true),
            )
            testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
        }

        test("Gote promotions into promotion zone") {
            val side = Side.GOTE
            val startSq = sq(3, 4)
            val komas = mapOf(startSq to Koma(side, KomaType.HI))
            val endSqs = setOf(
                TestCase(sq(3, 7), true),
                TestCase(sq(3, 8), true),
                TestCase(sq(3, 9), true),
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
                .map { TestCase(it, true) }
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
                .map { TestCase(it, true) }
            testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
        }
    }
})