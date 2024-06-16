package com.mfoo.sokuzumi

import arrow.core.Either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

fun MailboxBoard.Companion.fromMap(boardMap: Map<Square, Koma>): Board {
    var board = empty()
    for ((sq, koma) in boardMap) {
        board = board.setKoma(sq, koma)
    }
    return board
}

sealed interface BoardOperation {
    class SetKoma(val sq: Square, val koma: Koma) : BoardOperation
    class RemoveKoma(val sq: Square) : BoardOperation
}

fun testBoard(
    initialBoardData: Map<Square, Koma>,
    operations: List<BoardOperation>,
    expectedBoardData: Map<Square, Koma>
) {
    val initialBoard = MailboxBoard.fromMap(initialBoardData)
    val finalBoard = operations.fold(initialBoard) { acc, op ->
        when (op) {
            is BoardOperation.SetKoma -> acc.setKoma(op.sq, op.koma)
            is BoardOperation.RemoveKoma -> acc.removeKoma(op.sq)
        }
    }
    finalBoard shouldBe MailboxBoard.fromMap(expectedBoardData)
}

class MailboxBoardUnitTests : FunSpec({
    test("Boards with same structure should be equal") {
        val sq = Square(Col(4), Row(7))
        val koma = Koma(Side.SENTE, KomaType.OU)
        (MailboxBoard.empty().setKoma(sq, koma) == MailboxBoard.empty()
            .setKoma(sq, koma)).shouldBeTrue()
    }
    test("Different boards should be not equal") {
        val sq = Square(Col(4), Row(7))
        val koma = Koma(Side.SENTE, KomaType.OU)
        val koma2 = Koma(Side.GOTE, KomaType.OU)
        (MailboxBoard.empty().setKoma(sq, koma) == MailboxBoard.empty()
            .setKoma(sq, koma2)).shouldBeFalse()
    }
    test("Empty board should contain nothing") {
        val board = MailboxBoard.empty()
        val allSquares = Square.all()
        for (sq in allSquares) {
            board.getKoma(sq) shouldBe Either.Right(null)
        }
    }

    test("Setting Koma on empty Square") {
        val sq = Square(Col(4), Row(7))
        val koma = Koma(Side.SENTE, KomaType.OU)
        testBoard(
            mapOf(),
            listOf(BoardOperation.SetKoma(sq, koma)),
            mapOf(sq to koma)
        )
    }

    test("Setting Koma on occupied Square") {
        val sq = Square(Col(3), Row(6))
        val koma1 = Koma(Side.SENTE, KomaType.OU)
        val koma2 = Koma(Side.GOTE, KomaType.GI)
        testBoard(
            mapOf(sq to koma1),
            listOf(BoardOperation.SetKoma(sq, koma2)),
            mapOf(sq to koma2)
        )
    }

    test("Removing Koma from empty Square") {
        val sq = Square(Col(2), Row(1))
        testBoard(
            mapOf(),
            listOf(BoardOperation.RemoveKoma(sq)),
            mapOf()
        )
    }

    test("Removing Koma from occupied Square") {
        val sq = Square(Col(1), Row(2))
        val koma = Koma(Side.SENTE, KomaType.RY)
        testBoard(
            mapOf(sq to koma),
            listOf(BoardOperation.RemoveKoma(sq)),
            mapOf()
        )
    }
})