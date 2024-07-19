package com.mfoo.shogi

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

fun HandImpl.Companion.fromList(komaAmounts: List<Pair<KomaType, Int>>): Hand {
    var hand = empty()
    for ((komaType, amount) in komaAmounts) {
        hand = hand.setAmount(komaType, amount)
    }
    return hand
}

val ALLOWED_HAND_PIECES = listOf(
    KomaType.FU,
    KomaType.KY,
    KomaType.KE,
    KomaType.GI,
    KomaType.KI,
    KomaType.KA,
    KomaType.HI
)

sealed interface HandOperation {
    class SetAmount(val komaType: KomaType, val amount: Int) : HandOperation
    class Increment(val komaType: KomaType) : HandOperation
    class Decrement(val komaType: KomaType) : HandOperation
}

fun testHand(
    initialHandData: Map<KomaType, Int>,
    operations: List<HandOperation>,
    expectedHandData: Map<KomaType, Int>,
) {
    val initialHand = initialHandData.toList().let { HandImpl.fromList(it) }
    val finalHand = operations.fold(initialHand) { acc, op ->
        when (op) {
            is HandOperation.SetAmount -> acc.setAmount(
                op.komaType, op.amount
            )

            is HandOperation.Increment -> acc.increment(op.komaType)
            is HandOperation.Decrement -> acc.decrement(op.komaType)
        }
    }
    for (komaType in ALLOWED_HAND_PIECES) {
        val expectedAmount = expectedHandData[komaType] ?: 0
        val actualAmount = finalHand.getAmount(komaType)
        actualAmount shouldBe expectedAmount
    }
}

class HandImplTests : FunSpec({
    test("Empty hand should contain nothing") {
        val hand = HandImpl.empty()
        for (komaType in KomaType.entries) {
            hand.getAmount(komaType) shouldBe 0
        }
    }
    test("Setting amount from empty") {
        val input = listOf(KomaType.KE to 2, KomaType.HI to 3, KomaType.KA to 0)
        val initialHand = HandImpl.empty()
        val finalHand = initialHand.let {
            input.fold(it) { acc, (komaType, amount) ->
                acc.setAmount(komaType, amount)
            }
        }
        for ((komaType, amount) in input) {
            finalHand.getAmount(komaType) shouldBe amount
        }
    }
    context("Setting KomaType amounts") {
        withData(
            listOf(
                KomaType.FU,
                KomaType.KY,
                KomaType.KE,
                KomaType.GI,
                KomaType.KI,
                KomaType.KA,
                KomaType.HI
            )
        ) { komaType ->
            withData(listOf(0, 1, 2, 18)) { initial ->
                withData(listOf(0, 1, 2, 18)) { final ->
                    testHand(
                        mapOf(komaType to initial), listOf(
                            HandOperation.SetAmount(komaType, final)
                        ), mapOf(komaType to final)
                    )
                }
            }
        }
        test("Setting multiple values") {
            testHand(
                mapOf(KomaType.KA to 2, KomaType.HI to 3, KomaType.GI to 0),
                listOf(
                    HandOperation.SetAmount(KomaType.KA, 3),
                    HandOperation.SetAmount(KomaType.HI, 0),
                    HandOperation.SetAmount(KomaType.GI, 4)
                ),
                mapOf(KomaType.KA to 3, KomaType.HI to 0, KomaType.GI to 4)
            )
        }
    }
    test("Setting a negative amount should not change the hand") {
        testHand(
            mapOf(KomaType.KE to 2, KomaType.FU to 3),
            listOf(HandOperation.SetAmount(KomaType.FU, -1)),
            mapOf(KomaType.KE to 2, KomaType.FU to 3)
        )
    }
    context("Setting amount for an invalid KomaType should not change the hand") {
        withData(
            listOf(
                KomaType.OU to 2, KomaType.RY to 3, KomaType.UM to 1
            )
        ) { (komaType, amount) ->
            testHand(
                mapOf(),
                listOf(HandOperation.SetAmount(komaType, amount)),
                mapOf(),
            )
        }
    }
    test("Incrementing KomaType repeatedly") {
        testHand(
            mapOf(),
            List(4) { HandOperation.Increment(KomaType.FU) },
            mapOf(KomaType.FU to 4)
        )
    }
    test("Decrementing KomaType repeatedly") {
        testHand(
            mapOf(KomaType.FU to 4),
            List(3) { HandOperation.Decrement(KomaType.FU) },
            mapOf(KomaType.FU to 4 - 3)
        )
    }
    test("Should not be able to decrement below 0") {
        testHand(
            mapOf(KomaType.HI to 2, KomaType.KE to 0),
            listOf(HandOperation.Decrement(KomaType.KE)),
            mapOf(KomaType.HI to 2, KomaType.KE to 0)
        )
    }
    test("Get all KomaType amounts in hand") {
        val expected =
            mapOf(KomaType.FU to 3, KomaType.KI to 2, KomaType.HI to 1)
        val sut = expected.toList().let(HandImpl::fromList)
        val result = sut.getAmounts()
        expected shouldBe result
    }
})