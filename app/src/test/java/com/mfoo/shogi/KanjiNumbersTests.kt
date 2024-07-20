package com.mfoo.shogi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val kanjiMap = mapOf(
    1 to "一",
    2 to "二",
    3 to "三",
    4 to "四",
    5 to "五",
    6 to "六",
    7 to "七",
    8 to "八",
    9 to "九",
    10 to "十"
)

class KanjiNumbersTests : FunSpec({
    context("Numbers from 1 to 10") {
        for ((expected, kanji) in kanjiMap) {
            test("Read ${kanji} as ${expected}") {
                val result = KanjiNumbers.read(kanji)
                result shouldBe expected
            }
        }
    }

    context("Numbers from 11 to 99") {
        for (expected in listOf(12, 18, 20, 37, 40, 55, 61, 79, 84, 93)) {
            val tensKanji = (expected / 10)
                .let { if (it == 1) "" else kanjiMap[it] }
            val onesKanji = (expected % 10)
                .let { if (it == 0) "" else kanjiMap[it] }
            val kanji = tensKanji + kanjiMap[10] + onesKanji
            test("Read ${kanji} as ${expected}") {
                val result = KanjiNumbers.read(kanji)
                result shouldBe expected
            }
        }
    }
})
