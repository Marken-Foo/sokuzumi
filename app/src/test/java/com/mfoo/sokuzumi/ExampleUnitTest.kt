package com.mfoo.sokuzumi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

fun HandImpl.Companion.fromList(komaAmounts: List<Pair<KomaType, Int>>): Hand {
    var hand = empty()
    for ((komaType, amount) in komaAmounts) {
        hand = hand.setAmount(komaType, amount)
    }
    return hand
}

class KomaAmount(
    val komaType: KomaType, val initial: Int, val updated: Int? = null
)

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
    test("Setting amount from A to B") {
        val input = listOf(
            KomaAmount(KomaType.KA, 2, 3),
            KomaAmount(KomaType.HI, 3, 0),
            KomaAmount(KomaType.GI, 0, 4)
        )
        val initialHand = input.map { komaAmount ->
            with(komaAmount) {
                Pair(
                    komaType, initial
                )
            }
        }.let { HandImpl.fromList(it) }
        val finalHand = initialHand.let {
            input.fold(it) { acc, komaAmount ->
                with(komaAmount) {
                    when (updated == null) {
                        true -> acc
                        false -> acc.setAmount(komaType, updated)
                    }
                }
            }
        }
        for (komaAmount in input) {
            with(komaAmount) {
                finalHand.getAmount(komaType) shouldBe when {
                    updated == null -> initial
                    updated < 0 -> initial
                    else -> updated
                }
            }
        }
    }
    test("Setting a negative amount should not change the hand") {
        val input = listOf(
            KomaAmount(KomaType.KE, 2), KomaAmount(KomaType.FU, 3, -1)
        )
        val initialHand = input.map { komaAmount ->
            with(komaAmount) {
                Pair(
                    komaType, initial
                )
            }
        }.let { HandImpl.fromList(it) }
        val finalHand = initialHand.let {
            input.fold(it) { acc, komaAmount ->
                with(komaAmount) {
                    when (updated == null) {
                        true -> acc
                        false -> acc.setAmount(komaType, updated)
                    }
                }
            }
        }
        for (komaAmount in input) {
            with(komaAmount) {
                finalHand.getAmount(komaType) shouldBe when {
                    updated == null -> initial
                    updated < 0 -> initial
                    else -> updated
                }
            }
        }
    }
    test("Setting amount for an invalid KomaType should not change the hand") {
        val input = listOf(KomaType.OU to 2, KomaType.RY to 3, KomaType.UM to 0)
        val initialHand = HandImpl.empty()
        val finalHand = initialHand.let {
            input.fold(it) { acc, (komaType, amount) ->
                acc.setAmount(komaType, amount)
            }
        }
        for ((komaType, _) in input) {
            finalHand.getAmount(komaType) shouldBe 0
        }
    }
    test("Incrementing KomaType repeatedly") {
        var hand = HandImpl.empty()
        val amount = 4
        val komaType = KomaType.FU
        repeat(amount) { hand = hand.increment(komaType) }
        hand.getAmount(komaType) shouldBe amount
    }
    test("Decrementing KomaType repeatedly") {
        val initialAmount = 4
        val numDecrements = 2
        val komaType = KomaType.FU
        var hand = HandImpl.empty().setAmount(komaType, initialAmount)
        repeat(numDecrements) { hand = hand.decrement(komaType) }
        hand.getAmount(komaType) shouldBe initialAmount - numDecrements
    }
    test("Should not be able to decrement below 0") {
        val initialHand = HandImpl.empty()
        val komaType = KomaType.FU
        val finalHand = initialHand.decrement(komaType)
        finalHand.getAmount(komaType) shouldBe 0
    }
})