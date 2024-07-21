package com.mfoo.shogi.rules

import com.mfoo.shogi.Col
import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Row
import com.mfoo.shogi.Side
import com.mfoo.shogi.Square
import io.kotest.core.spec.style.FunSpec

class ValidMoveHiTests : FunSpec({
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
})
