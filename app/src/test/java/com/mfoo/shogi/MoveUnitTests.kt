package com.mfoo.shogi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val Pos: PositionFactory = PositionImpl

private fun testIsolatedMove(move: Move.Regular) {
    val sut = Pos.empty()
        .setKoma(move.startSq, Koma(move.side, move.komaType))
        .let { if (move.side == Side.SENTE) it else it.toggleSideToMove() }
    val result = sut.doMove(move)
    val endKomaType = if (move.isPromotion) {
        move.komaType.promote()
    } else {
        move.komaType
    }
    val expected = Pos.empty()
        .setKoma(move.endSq, Koma(move.side, endKomaType))
        .let { if (move.side == Side.SENTE) it.toggleSideToMove() else it }
    result shouldBe expected
}

private fun testCapture(move: Move.Regular, capturedKomaType: KomaType) {
    val capturedKoma = Koma(move.side.switch(), capturedKomaType)
    val sut = Pos.empty()
        .setKoma(move.startSq, Koma(move.side, move.komaType))
        .setKoma(move.endSq, capturedKoma)
        .let { if (move.side == Side.SENTE) it else it.toggleSideToMove() }
    val result = sut.doMove(move)
    val endKomaType = if (move.isPromotion) {
        move.komaType.promote()
    } else {
        move.komaType
    }
    val expected = Pos.empty()
        .setKoma(move.endSq, Koma(move.side, endKomaType))
        .incrementHandAmount(move.side, capturedKomaType.demote())
        .let { if (move.side == Side.SENTE) it.toggleSideToMove() else it }
    result shouldBe expected
}

private fun testDrop(move: Move.Drop, initialHandAmount: Int = 1) {
    val sut = Pos.empty()
        .setHandAmount(move.side, move.komaType, initialHandAmount)
        .let { if (move.side == Side.SENTE) it else it.toggleSideToMove() }
    val result = sut.doMove(move)
    val expected = Pos.empty()
        .setKoma(move.sq, Koma(move.side, move.komaType))
        .setHandAmount(move.side, move.komaType, initialHandAmount - 1)
        .let { if (move.side == Side.SENTE) it.toggleSideToMove() else it }
    result shouldBe expected
}

private fun testGameEnd(move: Move.GameEnd) {
    val sut = Pos.empty()
    val result = sut.doMove(move)
    val expected = Pos.empty()
    result shouldBe expected
}

class MoveUnitTests : FunSpec({
    context("Do move") {
        test("Apply regular sente move") {
            val move = Move.Regular(
                startSq = Square(Col(4), Row(4)),
                endSq = Square(Col(4), Row(3)),
                isPromotion = false,
                side = Side.SENTE,
                komaType = KomaType.FU,
                capturedKoma = null,
            )
            testIsolatedMove(move)
        }

        test("Apply regular gote move") {
            val move = Move.Regular(
                startSq = Square(Col(1), Row(7)),
                endSq = Square(Col(2), Row(8)),
                isPromotion = false,
                side = Side.GOTE,
                komaType = KomaType.GI,
                capturedKoma = null,
            )
            testIsolatedMove(move)
        }

        test("Apply sente promotion move") {
            val move = Move.Regular(
                startSq = Square(Col(4), Row(4)),
                endSq = Square(Col(4), Row(3)),
                isPromotion = true,
                side = Side.SENTE,
                komaType = KomaType.FU,
                capturedKoma = null,
            )
            testIsolatedMove(move)
        }

        test("Apply gote promotion move") {
            val move = Move.Regular(
                startSq = Square(Col(1), Row(7)),
                endSq = Square(Col(2), Row(8)),
                isPromotion = true,
                side = Side.GOTE,
                komaType = KomaType.GI,
                capturedKoma = null,
            )
            testIsolatedMove(move)
        }

        test("Sente capture") {
            val side = Side.SENTE
            val capturedKomaType = KomaType.FU
            val move = Move.Regular(
                startSq = Square(Col(2), Row(8)),
                endSq = Square(Col(2), Row(4)),
                isPromotion = false,
                side = side,
                komaType = KomaType.HI,
                capturedKoma = Koma(side.switch(), capturedKomaType)
            )
            testCapture(move, capturedKomaType)
        }

        test("Gote capture") {
            val side = Side.GOTE
            val capturedKomaType = KomaType.KE
            val move = Move.Regular(
                startSq = Square(Col(9), Row(8)),
                endSq = Square(Col(9), Row(7)),
                isPromotion = false,
                side = side,
                komaType = KomaType.NY,
                capturedKoma = Koma(side.switch(), capturedKomaType)
            )
            testCapture(move, capturedKomaType)
        }

        test("Sente capture of promoted koma") {
            val side = Side.SENTE
            val capturedKomaType = KomaType.TO
            val move = Move.Regular(
                startSq = Square(Col(2), Row(8)),
                endSq = Square(Col(2), Row(4)),
                isPromotion = false,
                side = side,
                komaType = KomaType.HI,
                capturedKoma = Koma(side.switch(), capturedKomaType)
            )
            testCapture(move, capturedKomaType)
        }

        test("Gote capture of promoted koma") {
            val side = Side.GOTE
            val capturedKomaType = KomaType.NK
            val move = Move.Regular(
                startSq = Square(Col(9), Row(8)),
                endSq = Square(Col(9), Row(7)),
                isPromotion = false,
                side = side,
                komaType = KomaType.NY,
                capturedKoma = Koma(side.switch(), capturedKomaType)
            )
            testCapture(move, capturedKomaType)
        }

        test("Sente drop") {
            val move = Move.Drop(
                sq = Square(Col(5), Row(4)),
                side = Side.SENTE,
                komaType = KomaType.KY
            )
            testDrop(move)
        }

        test("Gote drop") {
            val move = Move.Drop(
                sq = Square(Col(3), Row(3)),
                side = Side.GOTE,
                komaType = KomaType.KE
            )
            testDrop(move)
        }

        test("Game end") {
            val move = Move.GameEnd(Move.GameEndType.MATE)
            testGameEnd(move)
        }
    }
})