package com.mfoo.shogi.sfen

import com.mfoo.shogi.sfen.SfenAst.Board
import com.mfoo.shogi.sfen.SfenAst.Hand
import com.mfoo.shogi.sfen.SfenAst.Koma
import com.mfoo.shogi.sfen.SfenAst.KomaType
import com.mfoo.shogi.sfen.SfenAst.Row
import com.mfoo.shogi.sfen.SfenAst.Side
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SfenTests : FunSpec({
    context("Basic SFEN tests") {
        test("Parse all allowed board pieces") {
            val input =
                "krbgsnlp1/1+r+b1+s+n+l+p1/9/9/9/9/9/1+P+L+N+S1+B+R1/1PLNSGBRK b - 1"
            val expected = Board(
                listOf(
                    Row(
                        listOf(
                            Koma(Side.GOTE, KomaType.OU),
                            Koma(Side.GOTE, KomaType.HI),
                            Koma(Side.GOTE, KomaType.KA),
                            Koma(Side.GOTE, KomaType.KI),
                            Koma(Side.GOTE, KomaType.GI),
                            Koma(Side.GOTE, KomaType.KE),
                            Koma(Side.GOTE, KomaType.KY),
                            Koma(Side.GOTE, KomaType.FU),
                            null,
                        )
                    ),
                    Row(
                        listOf(
                            null,
                            Koma(Side.GOTE, KomaType.RY),
                            Koma(Side.GOTE, KomaType.UM),
                            null,
                            Koma(Side.GOTE, KomaType.NG),
                            Koma(Side.GOTE, KomaType.NK),
                            Koma(Side.GOTE, KomaType.NY),
                            Koma(Side.GOTE, KomaType.TO),
                            null
                        )
                    ),
                    Row(List(9) { null }),
                    Row(List(9) { null }),
                    Row(List(9) { null }),
                    Row(List(9) { null }),
                    Row(List(9) { null }),
                    Row(
                        listOf(
                            null,
                            Koma(Side.SENTE, KomaType.TO),
                            Koma(Side.SENTE, KomaType.NY),
                            Koma(Side.SENTE, KomaType.NK),
                            Koma(Side.SENTE, KomaType.NG),
                            null,
                            Koma(Side.SENTE, KomaType.UM),
                            Koma(Side.SENTE, KomaType.RY),
                            null
                        )
                    ),
                    Row(
                        listOf(
                            null,
                            Koma(Side.SENTE, KomaType.FU),
                            Koma(Side.SENTE, KomaType.KY),
                            Koma(Side.SENTE, KomaType.KE),
                            Koma(Side.SENTE, KomaType.GI),
                            Koma(Side.SENTE, KomaType.KI),
                            Koma(Side.SENTE, KomaType.KA),
                            Koma(Side.SENTE, KomaType.HI),
                            Koma(Side.SENTE, KomaType.OU),
                        )
                    ),
                ),
            )
            val sut = parseSfen(input)
            sut shouldNotBe null
            val result = sut?.board
            result shouldBe expected
        }

        test("Parse sente turn to move") {
            val input = "9/9/9/9/9/9/9/9/9 b - 1"
            val sut = parseSfen(input)
            val expected = Side.SENTE
            val result = sut?.sideToMove
            result shouldBe expected
        }

        test("Parse gote turn to move") {
            val input = "9/9/9/9/9/9/9/9/9 w - 1"
            val sut = parseSfen(input)
            val expected = Side.GOTE
            val result = sut?.sideToMove
            result shouldBe expected
        }

        test("Parse empty hands") {
            val input = "9/9/9/9/9/9/9/9/9 b - 1"
            val sut = parseSfen(input)
            val expectedSente = Hand(mapOf())
            val expectedGote = Hand(mapOf())
            val resultSente = sut?.senteHand
            val resultGote = sut?.goteHand
            resultSente shouldBe expectedSente
            resultGote shouldBe expectedGote
        }

        test("Parse all allowed hand pieces") {
            val input = "9/9/9/9/9/9/9/9/9 b RB2G3S2NL12Prbgsnlp 1"
            val sut = parseSfen(input)
            val expectedSente = Hand(
                mapOf(
                    KomaType.HI to 1,
                    KomaType.KA to 1,
                    KomaType.KI to 2,
                    KomaType.GI to 3,
                    KomaType.KE to 2,
                    KomaType.KY to 1,
                    KomaType.FU to 12,
                )
            )
            val expectedGote = Hand(
                mapOf(
                    KomaType.HI to 1,
                    KomaType.KA to 1,
                    KomaType.KI to 1,
                    KomaType.GI to 1,
                    KomaType.KE to 1,
                    KomaType.KY to 1,
                    KomaType.FU to 1,
                )
            )
            val resultSente = sut?.senteHand
            val resultGote = sut?.goteHand
            resultSente shouldBe expectedSente
            resultGote shouldBe expectedGote
        }

        test("Parse move number") {
            val moveNum = 27
            val input = "9/9/9/9/9/9/9/9/9 b - ${moveNum}"
            val sut = parseSfen(input)
            val expected = moveNum
            val result = sut?.moveNumber
            result shouldBe expected
        }
    }
})