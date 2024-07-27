package com.mfoo.shogi.rules

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Side
import com.mfoo.shogi.Square
import com.mfoo.shogi.isInCheck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe


class CheckTests : FunSpec({
    // Creates a test with 2 komas on the board, a specified koma on startSq
    // and the enemy king (OU) on endSq. The enemy is expected to be in check.
    fun testGiveCheckTo(
        side: Side,
        komaType: KomaType,
        startSq: Square,
        endSq: Square,
    ) {
        test("${side.switch()} in check by ${komaType}") {
            val pos = Pos.fromMap(
                mapOf(
                    startSq to Koma(side, komaType),
                    endSq to Koma(side.switch(), KomaType.OU),
                )
            )
            isInCheck(side.switch(), pos) shouldBe true
        }
    }

    testGiveCheckTo(Side.SENTE, KomaType.FU, sq(5, 5), sq(5, 4))
    testGiveCheckTo(Side.GOTE, KomaType.FU, sq(5, 5), sq(5, 6))
    testGiveCheckTo(Side.SENTE, KomaType.KY, sq(3, 7), sq(3, 1))
    testGiveCheckTo(Side.GOTE, KomaType.KY, sq(6, 2), sq(6, 8))
    testGiveCheckTo(Side.SENTE, KomaType.KE, sq(7, 9), sq(8, 7))
    testGiveCheckTo(Side.GOTE, KomaType.KE, sq(4, 6), sq(3, 8))
    testGiveCheckTo(Side.SENTE, KomaType.GI, sq(2, 4), sq(2, 3))
    testGiveCheckTo(Side.GOTE, KomaType.GI, sq(8, 1), sq(9, 2))

    for (komaType in setOf(
        KomaType.KI,
        KomaType.TO,
        KomaType.NY,
        KomaType.NK,
        KomaType.NG
    )) {
        testGiveCheckTo(Side.SENTE, komaType, sq(3, 2), sq(3, 1))
        testGiveCheckTo(Side.GOTE, komaType, sq(4, 2), sq(5, 3))
    }

    testGiveCheckTo(Side.SENTE, KomaType.KA, sq(1, 2), sq(8, 9))
    testGiveCheckTo(Side.GOTE, KomaType.KA, sq(1, 2), sq(8, 9))
    testGiveCheckTo(Side.SENTE, KomaType.HI, sq(4, 6), sq(9, 6))
    testGiveCheckTo(Side.GOTE, KomaType.HI, sq(4, 6), sq(4, 1))

    testGiveCheckTo(Side.SENTE, KomaType.UM, sq(3, 8), sq(9, 2))
    testGiveCheckTo(Side.GOTE, KomaType.UM, sq(3, 8), sq(1, 6))
    testGiveCheckTo(Side.SENTE, KomaType.UM, sq(4, 7), sq(4, 6))
    testGiveCheckTo(Side.GOTE, KomaType.UM, sq(4, 7), sq(5, 7))

    testGiveCheckTo(Side.SENTE, KomaType.RY, sq(3, 8), sq(3, 1))
    testGiveCheckTo(Side.GOTE, KomaType.RY, sq(3, 8), sq(1, 8))
    testGiveCheckTo(Side.SENTE, KomaType.RY, sq(4, 7), sq(3, 6))
    testGiveCheckTo(Side.GOTE, KomaType.RY, sq(4, 7), sq(5, 8))
})
