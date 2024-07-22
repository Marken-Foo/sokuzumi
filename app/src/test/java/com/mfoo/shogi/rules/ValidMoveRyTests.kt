package com.mfoo.shogi.rules

import com.mfoo.shogi.Col
import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Row
import com.mfoo.shogi.Side
import com.mfoo.shogi.Square
import io.kotest.core.spec.style.FunSpec

class ValidMoveRyTests : FunSpec({
    test("RY moves orthogonally and one step diagonally") {
        val side = Side.GOTE
        val komaType = KomaType.RY
        val startSq = sq(3, 4)
        val endSqs = (1..9)
            .fold(emptySet<Square>()) { acc, n ->
                acc
                    .plus(Square(startSq.col, Row(n)))
                    .plus(Square(Col(n), startSq.row))
            }
            .minus(startSq)
            .plus(listOf(sq(4, 3), sq(2, 3), sq(4, 5), sq(2, 5)))
            .map { TestCase(it, true) }
        val komas = mapOf(
            startSq to Koma(side, komaType),
        )

        testRegularMoves(komas, startSq, endSqs)
    }

    test("RY lines are blocked by allied komas") {
        val side = Side.GOTE
        val komaType = KomaType.RY
        val startSq = sq(3, 4)
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(3, 1) to Koma(side, KomaType.KE),
            sq(3, 7) to Koma(side, KomaType.TO),
            sq(2, 4) to Koma(side, KomaType.UM),
            sq(7, 4) to Koma(side, KomaType.NY),
            sq(2, 5) to Koma(side, KomaType.FU),
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

            TestCase(sq(4, 3), true),
            TestCase(sq(2, 3), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(2, 5), false),
        )
        testRegularMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("RY lines are blocked by enemy komas but can capture them") {
        val side = Side.GOTE
        val komaType = KomaType.RY
        val startSq = sq(3, 4)
        val komas = mapOf(
            startSq to Koma(side, komaType),
            sq(3, 1) to Koma(side.switch(), KomaType.KE),
            sq(3, 7) to Koma(side.switch(), KomaType.TO),
            sq(2, 4) to Koma(side.switch(), KomaType.UM),
            sq(7, 4) to Koma(side.switch(), KomaType.NY),
            sq(2, 5) to Koma(side.switch(), KomaType.FU),
        )
        val endSqs = setOf(
            TestCase(sq(3, 1), true),
            TestCase(sq(3, 2), true),
            TestCase(sq(3, 3), true),
            TestCase(sq(3, 5), true),
            TestCase(sq(3, 6), true),
            TestCase(sq(3, 7), true),
            TestCase(sq(3, 8), false),
            TestCase(sq(1, 4), false),
            TestCase(sq(2, 4), true),
            TestCase(sq(4, 4), true),
            TestCase(sq(5, 4), true),
            TestCase(sq(6, 4), true),
            TestCase(sq(7, 4), true),
            TestCase(sq(8, 4), false),

            TestCase(sq(4, 3), true),
            TestCase(sq(2, 3), true),
            TestCase(sq(4, 5), true),
            TestCase(sq(2, 5), true),
        )
        testRegularMoves(komas, startSq = startSq, endSqs = endSqs)
    }

    test("RY cannot promote") {
        val side = Side.GOTE
        val komaType = KomaType.RY
        val startSq = sq(2, 8)
        val komas = mapOf(startSq to Koma(side, komaType))
        val endSqs = (1..9)
            .fold(emptySet<Square>()) { acc, n ->
                acc
                    .plus(Square(startSq.col, Row(n)))
                    .plus(Square(Col(n), startSq.row))
            }
            .minus(startSq)
            .plus(listOf(sq(3, 7), sq(1, 7), sq(3, 9), sq(1, 9)))
            .map { TestCase(it, false) }
        testPromotionMoves(komas, startSq = startSq, endSqs = endSqs)
    }
})
