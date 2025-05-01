package com.mfoo.shogi.kif

import com.mfoo.shogi.Col
import com.mfoo.shogi.KanjiNumbers
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Row
import com.mfoo.shogi.Square
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private data class MoveInfo(
    val startCoords: Int = 11,
    val endCoords: Int = 99,
    val komaType: KomaType,
    val isPromotion: Boolean = false,
) {
    fun toRegularMoveString(): String {
        val endCol = endCoords / 10
        val endRow = endCoords % 10
        return (toFullWidth(endCol)
            + KanjiNumbers.fromDigit(endRow)
            + komaTypeToKanji(komaType)
            + if (isPromotion) "成" else ""
            + "(${startCoords})"
            )
    }

    fun startSq(): Square {
        return Square(Col(startCoords / 10), Row(startCoords % 10))
    }

    fun endSq(): Square {
        return Square(Col(endCoords / 10), Row(endCoords % 10))
    }
}

private fun testRegularMove(moveInfo: MoveInfo, moveString: String? = null) {
    val input = Token.MoveLine(
        1,
        moveString ?: moveInfo.toRegularMoveString(),
        null,
        null
    )
    val expected = KifAst.Move.Regular(
        moveNum = 1,
        startSq = moveInfo.startSq(),
        endSq = moveInfo.endSq(),
        isPromotion = moveInfo.isPromotion,
        komaType = moveInfo.komaType,
        moveTime = null,
        totalTime = null,
        comment = ""
    )
    val result = parseMove(input, null)
    result shouldBe expected
}


class KifMoveTests : FunSpec({
    context("Regular moves") {
        val testInfo = listOf(
            MoveInfo(11, 12, KomaType.FU),
            MoveInfo(91, 94, KomaType.KY),
            MoveInfo(37, 45, KomaType.KE),
            MoveInfo(54, 55, KomaType.GI),
            MoveInfo(64, 65, KomaType.KI),
            MoveInfo(11, 99, KomaType.KA),
            MoveInfo(34, 84, KomaType.HI),
            MoveInfo(55, 44, KomaType.OU),
            MoveInfo(22, 31, KomaType.TO),
            MoveInfo(43, 33, KomaType.NY),
            MoveInfo(76, 77, KomaType.NK),
            MoveInfo(89, 88, KomaType.NG),
            MoveInfo(26, 27, KomaType.UM),
            MoveInfo(55, 66, KomaType.RY),
        )

        for (moveInfo in testInfo) {
            val moveString = moveInfo.toRegularMoveString()
            test("Read ${moveInfo.komaType} move ${moveString}") {
                testRegularMove(moveInfo)
            }
        }

        val ryMoveInfo = MoveInfo(55, 66, KomaType.RY)
        val ryMoveString = "６六竜(55)"
        test("Read simpler kanji RY move ${ryMoveString}") {
            testRegularMove(ryMoveInfo, ryMoveString)
        }

        val nyMoveInfo = MoveInfo(43, 33, KomaType.NY)
        val nyMoveString = "３三成香(43)"
        test("Read two-kanji NY move ${nyMoveString}") {
            testRegularMove(nyMoveInfo, nyMoveString)
        }

        val nkMoveInfo = MoveInfo(76, 77, KomaType.NK)
        val nkMoveString = "７七成桂(76)"
        test("Read two-kanji NK move ${nkMoveString}") {
            testRegularMove(nkMoveInfo, nkMoveString)
        }

        val ngMoveInfo = MoveInfo(89, 88, KomaType.NG)
        val ngMoveString = "８八成銀(89)"
        test("Read two-kanji NG move ${ngMoveString}") {
            testRegularMove(ngMoveInfo, ngMoveString)
        }
    }

    context("Test drop move") {
        test("Read knight drop") {
            val input = Token.MoveLine(
                1,
                "２二桂打",
                null,
                null
            )
            val expected = KifAst.Move.Drop(
                moveNum = 1,
                sq = Square(Col(2), Row(2)),
                komaType = KomaType.KE,
                moveTime = null,
                totalTime = null,
                comment = ""
            )
            val result = parseMove(input, null)
            result shouldBe expected
        }
    }

    context("Test promotion move") {
        val moveInfo = MoveInfo(11, 99, KomaType.KA)
        val moveString = moveInfo.toRegularMoveString()
        test("Read ${moveInfo.komaType} promotion ${moveString}") {
            testRegularMove(moveInfo)
        }
    }
})

private fun komaTypeToKanji(komaType: KomaType): String {
    return when (komaType) {
        KomaType.OU -> "玉"
        KomaType.HI -> "飛"
        KomaType.RY -> "龍"
        KomaType.KA -> "角"
        KomaType.UM -> "馬"
        KomaType.KI -> "金"
        KomaType.GI -> "銀"
        KomaType.NG -> "全"
        KomaType.KE -> "桂"
        KomaType.NK -> "圭"
        KomaType.KY -> "香"
        KomaType.NY -> "杏"
        KomaType.FU -> "歩"
        KomaType.TO -> "と"
    }
}

private fun toFullWidth(digit: Int): String {
    return when (digit) {
        0 -> "０"
        1 -> "１"
        2 -> "２"
        3 -> "３"
        4 -> "４"
        5 -> "５"
        6 -> "６"
        7 -> "７"
        8 -> "８"
        9 -> "９"
        else -> ""
    }
}