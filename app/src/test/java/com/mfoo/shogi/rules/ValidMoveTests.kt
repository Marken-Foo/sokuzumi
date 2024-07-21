package com.mfoo.shogi.rules

import arrow.core.fold
import com.mfoo.shogi.Col
import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Move
import com.mfoo.shogi.Position
import com.mfoo.shogi.PositionFactory
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.Row
import com.mfoo.shogi.Side
import com.mfoo.shogi.Square
import com.mfoo.shogi.isValid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

internal val Pos: PositionFactory = PositionImpl

private fun PositionFactory.fromMap(komas: Map<Square, Koma>): Position {
    return komas.fold(this.empty()) { pos, (sq, koma) ->
        pos.setKoma(sq, koma)
    }
}

internal fun sq(col: Int, row: Int): Square {
    return Square(Col(col), Row(row))
}

internal data class TestCase(val sq: Square, val shouldBeValid: Boolean)

internal fun testRegularMoves(
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

internal fun testPromotionMoves(
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
})
