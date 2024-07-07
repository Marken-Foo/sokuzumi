package com.mfoo.shogi

import com.mfoo.shogi.Col
import com.mfoo.shogi.HandImpl
import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Position
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.Row
import com.mfoo.shogi.Side
import com.mfoo.shogi.Square
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

sealed interface PositionOperation {
    class SetHandAmount(
        val side: Side, val komaType: KomaType, val amount: Int
    ) : PositionOperation

    class IncrementHand(val side: Side, val komaType: KomaType) :
        PositionOperation

    class DecrementHand(val side: Side, val komaType: KomaType) :
        PositionOperation

    class SetKoma(val sq: Square, val koma: Koma) : PositionOperation
    class RemoveKoma(val sq: Square) : PositionOperation
}

fun operate(
    initialPosition: Position, operations: List<PositionOperation>
): Position {
    return operations.fold(initialPosition) { acc, op ->
        when (op) {
            is PositionOperation.SetHandAmount -> acc.setHandAmount(
                op.side, op.komaType, op.amount
            )

            is PositionOperation.IncrementHand -> acc.incrementHandAmount(
                op.side, op.komaType,
            )

            is PositionOperation.DecrementHand -> acc.decrementHandAmount(
                op.side, op.komaType,
            )

            is PositionOperation.SetKoma -> acc.setKoma(op.sq, op.koma)
            is PositionOperation.RemoveKoma -> acc.removeKoma(op.sq)
        }
    }
}

class PositionImplUnitTests : FunSpec({
    context("Position tests") {
        test("Positions with same state should be equal") {
            val arbitrarySfen =
                """lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b - 1"""
            val sut1 = PositionImpl.fromSfen(arbitrarySfen)
            val sut2 = PositionImpl.fromSfen(arbitrarySfen)
            sut1 shouldBe sut2
        }

        test("Empty position should be equal to a default SFEN") {
            val emptySfen = """9/9/9/9/9/9/9/9/9 b - 1"""
            val result = PositionImpl.empty()
            result shouldBe PositionImpl.fromSfen(emptySfen)
        }

        test("Side to move of an empty Position should be sente") {
            val sut = PositionImpl.empty()
            val result = sut.getSideToMove()
            result shouldBe Side.SENTE
        }

        test("Toggle side to move should change the side to move") {
            val arbitraryPosition = PositionImpl.empty()
            val sut = arbitraryPosition.toggleSideToMove()
            val result = sut.getSideToMove()
            val expectedSide = arbitraryPosition.getSideToMove().switch()
            result shouldBe expectedSide
        }
    }

    context("Position hand tests") {
        data class HandKomaData(
            val side: Side, val komaType: KomaType, val amount: Int
        )

        val sides = listOf(Side.SENTE, Side.GOTE)
        val komaTypes = listOf(KomaType.FU, KomaType.GI, KomaType.HI)
        val amounts = listOf(0, 1, 4)
        val handKomaData = sides.map { side ->
            komaTypes.map { komaType ->
                amounts.map { amount ->
                    HandKomaData(
                        side, komaType, amount
                    )
                }
            }
        }.flatten().flatten()

        context("Hands in default position should be empty") {
            withData(listOf(Side.SENTE, Side.GOTE)) { side ->
                val arbitraryPosition = PositionImpl.empty()
                val result = arbitraryPosition.getHandOfSide(side)
                result shouldBe HandImpl.empty()
            }
        }

        context("Getting hands of either player") {
            val sut = operate(
                PositionImpl.empty(), listOf(
                    PositionOperation.SetHandAmount(
                        Side.SENTE, KomaType.FU, 2
                    ), PositionOperation.SetHandAmount(
                        Side.GOTE, KomaType.HI, 1
                    )
                )
            )
            val senteHand = HandImpl.empty().setAmount(KomaType.FU, 2)
            val goteHand = HandImpl.empty().setAmount(KomaType.HI, 1)
            val resultSente = sut.getHandOfSide(Side.SENTE)
            val resultGote = sut.getHandOfSide(Side.GOTE)

            resultSente shouldBe senteHand
            resultGote shouldBe goteHand
        }

        context("Setting hand amounts") {
            withData(
                nameFn = { "${it.side}, ${it.komaType}, ${it.amount}" },
                handKomaData
            ) { (side, komaType, amount) ->
                val sut = operate(
                    PositionImpl.empty(), listOf(
                        PositionOperation.SetHandAmount(
                            side, komaType, amount
                        )
                    )
                )
                val expectedHand =
                    sut.getHandOfSide(side).setAmount(komaType, amount)
                val result = sut.getHandOfSide(side)
                result shouldBe expectedHand
            }
        }

        context("Incrementing hand amounts") {
            withData(
                nameFn = { "${it.side}, ${it.komaType}, ${it.amount}" },
                handKomaData
            ) { (side, komaType, amount) ->
                val sut = operate(
                    PositionImpl.empty(), listOf(
                        PositionOperation.SetHandAmount(
                            side, komaType, amount
                        )
                    )
                )
                val expectedHand =
                    sut.getHandOfSide(side).setAmount(komaType, amount + 1)
                val result =
                    sut.incrementHandAmount(side, komaType).getHandOfSide(side)
                result shouldBe expectedHand
            }
        }

        context("Decrementing hand amounts") {
            withData(
                nameFn = { "${it.side}, ${it.komaType}, ${it.amount}" },
                handKomaData
            ) { (side, komaType, amount) ->
                val sut =
                    PositionImpl.empty().setHandAmount(side, komaType, amount)
                val expectedHand = sut.getHandOfSide(side)
                    .setAmount(komaType, maxOf(amount - 1, 0))
                val result =
                    sut.decrementHandAmount(side, komaType).getHandOfSide(side)
                result shouldBe expectedHand
            }
        }

        test("Get hand amount") {
            val sut =
                PositionImpl.empty().setHandAmount(Side.GOTE, KomaType.KE, 2)
            val result = sut.getHandAmount(Side.GOTE, KomaType.KE)
            result shouldBe 2
        }
    }

    context("Position board tests") {
        fun PositionImpl.Companion.fromMap(boardMap: Map<Square, Koma>): Position {
            var position = PositionImpl.empty()
            for ((sq, koma) in boardMap) {
                position = position.setKoma(sq, koma)
            }
            return position
        }

        fun testPositionBoard(
            initialBoardData: Map<Square, Koma>,
            operations: List<PositionOperation>,
            expectedBoardData: Map<Square, Koma>
        ) {
            val initialPosition = PositionImpl.fromMap(initialBoardData)
            val finalPosition = operate(initialPosition, operations)
            finalPosition shouldBe PositionImpl.fromMap(expectedBoardData)
        }

        test("Setting Koma on empty Square") {
            val sq = Square(Col(4), Row(7))
            val koma = Koma(Side.SENTE, KomaType.OU)
            testPositionBoard(
                mapOf(),
                listOf(PositionOperation.SetKoma(sq, koma)),
                mapOf(sq to koma)
            )
        }

        test("Setting Koma on occupied Square") {
            val sq = Square(Col(3), Row(6))
            val koma1 = Koma(Side.SENTE, KomaType.OU)
            val koma2 = Koma(Side.GOTE, KomaType.GI)
            testPositionBoard(
                mapOf(sq to koma1),
                listOf(PositionOperation.SetKoma(sq, koma2)),
                mapOf(sq to koma2)
            )
        }

        test("Removing Koma from empty Square") {
            val sq = Square(Col(2), Row(1))
            testPositionBoard(
                mapOf(), listOf(PositionOperation.RemoveKoma(sq)), mapOf()
            )
        }

        test("Removing Koma from occupied Square") {
            val sq = Square(Col(1), Row(2))
            val koma = Koma(Side.SENTE, KomaType.RY)
            testPositionBoard(
                mapOf(sq to koma),
                listOf(PositionOperation.RemoveKoma(sq)),
                mapOf()
            )
        }

        test("Get all Koma from board") {
            val data = mapOf(
                Square(Col(1), Row(4)) to Koma(Side.SENTE, KomaType.GI),
                Square(Col(9), Row(7)) to Koma(Side.GOTE, KomaType.OU),
                Square(Col(7), Row(6)) to Koma(Side.GOTE, KomaType.TO),
            )
            val sut = operate(
                PositionImpl.empty(),
                data.map { (sq, koma) -> PositionOperation.SetKoma(sq, koma) }
            )
            val result = sut.getAllKoma()
            result shouldBe data
        }
    }
})