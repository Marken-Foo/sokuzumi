package com.mfoo.shogi

object KanjiNumbers {
    private fun kanjiToInt(string: String): Int? {
        return when (string) {
            "一" -> 1
            "二" -> 2
            "三" -> 3
            "四" -> 4
            "五" -> 5
            "六" -> 6
            "七" -> 7
            "八" -> 8
            "九" -> 9
            "十" -> 10
            else -> null
        }
    }

    private val kanjiNumRegex =
        Regex("""(?<TensGroup>(?<Tens>[二三四五六七八九])?十)?(?<Units>[一二三四五六七八九])?""")

    /**
     * Returns the value of an integer in kanji between 1 and 99 inclusive.
     */
    fun read(string: String): Int? {
        val match = kanjiNumRegex.matchEntire(string)
        val unitsValue = match?.groups?.get("Units")?.value?.let(::kanjiToInt)
        val hasTens = match?.groups?.get("TensGroup") != null
        val tens = match?.groups?.get("Tens")?.value?.let(::kanjiToInt) ?: 1
        return if (hasTens) {
            10 * tens + (unitsValue ?: 0)
        } else {
            unitsValue
        }
    }

    /**
     * Returns a kanji corresponding to a digit.
     */
    fun fromDigit(digit: Int): String? {
        return when (digit) {
            0 -> "零"
            1 -> "一"
            2 -> "二"
            3 -> "三"
            4 -> "四"
            5 -> "五"
            6 -> "六"
            7 -> "七"
            8 -> "八"
            9 -> "九"
            else -> null
        }
    }
}