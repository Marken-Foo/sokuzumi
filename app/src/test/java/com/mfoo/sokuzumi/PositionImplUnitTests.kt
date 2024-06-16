package com.mfoo.sokuzumi

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
        }
    }
}

class PositionImplUnitTests : FunSpec({
    context("Position tests") {
        test("Positions with same state should be equal") {
            val arbitrarySfen =
                Sfen("""lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b - 1""")
            val sut1 = PositionImpl.fromSfen(arbitrarySfen)
            val sut2 = PositionImpl.fromSfen(arbitrarySfen)
            sut1 shouldBe sut2
        }

        test("Empty position should be equal to a default SFEN") {
            val emptySfen = Sfen("""9/9/9/9/9/9/9/9/9 b - 1""")
            val result = PositionImpl.empty()
            result shouldBe PositionImpl.fromSfen(emptySfen)
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
                PositionImpl.empty(),
                listOf(
                    PositionOperation.SetHandAmount(
                        Side.SENTE,
                        KomaType.FU,
                        2
                    ),
                    PositionOperation.SetHandAmount(
                        Side.GOTE,
                        KomaType.HI,
                        1
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
            val sut = PositionImpl.empty().setHandAmount(Side.GOTE, KomaType.KE, 2)
            val result = sut.getHandAmount(Side.GOTE, KomaType.KE)
            result shouldBe 2
        }
    }
})