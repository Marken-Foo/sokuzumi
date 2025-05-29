package com.mfoo.shogi

import arrow.core.raise.either
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GameImplTests : FunSpec({
    context("Game tests") {
        test("Empty game is empty") {
            val sut = GameImpl.empty()
            val moveResult = sut.getMainlineMove()
            val posResult = sut.getPosition()
            moveResult shouldBe null
            posResult shouldBe PositionImpl.empty()
        }

        test("Create game from position") {
            val arbitrarySfen =
                "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b - 1"
            val arbitraryPosition = PositionImpl.fromSfen(arbitrarySfen)
                ?: fail("Unable to instantiate test position from SFEN")
            val sut = GameImpl.fromPos(arbitraryPosition)
            val moveResult = sut.getMainlineMove()
            val posResult = sut.getPosition()
            moveResult shouldBe null
            posResult shouldBe arbitraryPosition
        }

        test("Add move should advance game") {
            val startingSfen =
                "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b - 1"
            val startingPosition = PositionImpl.fromSfen(startingSfen)
                ?: fail("Unable to instantiate test position from SFEN")
            val move =
                Move.Regular(
                    Square(Col(7), Row(7)),
                    Square(Col(7), Row(6)),
                    false,
                    Side.SENTE,
                    KomaType.FU,
                    null
                )
            val sut = GameImpl.fromPos(startingPosition)
            val result = sut.addMove(move)
            val gameResult =
                result.getOrNull() ?: fail("Failed to add move to Game")
            val moveResult = gameResult.getMainlineMove()
            val posResult = gameResult.getPosition()
            val expectedPos =
                PositionImpl.fromSfen("lnsgkgsnl/1r5b1/ppppppppp/9/9/2P6/PP1PPPPPP/1B5R1/LNSGKGSNL w - 2")
            moveResult shouldBe null
            posResult shouldBe expectedPos
        }

        test("Adding multiple moves in a single line") {
            val startingSfen = "9/4kP3/9/3pK4/9/9/9/9/9 b Pp 1"
            val startingPosition = PositionImpl.fromSfen(startingSfen)
                ?: fail("Unable to instantiate test position from SFEN")
            val sut = GameImpl.fromPos(startingPosition)
            val move1 =
                Move.Drop(Square(Col(1), Row(4)), Side.SENTE, KomaType.FU)
            val move2 =
                Move.Drop(Square(Col(1), Row(2)), Side.GOTE, KomaType.FU)
            val move3 = Move.Regular(
                Square(Col(5), Row(4)),
                Square(Col(6), Row(4)),
                false,
                Side.SENTE,
                KomaType.OU,
                Koma(Side.GOTE, KomaType.FU)
            )
            val result = either {
                sut.addMove(move1).bind()
                    .addMove(move2).bind()
                    .addMove(move3).bind()
            }
            val gameResult =
                result.getOrNull() ?: fail("Failed to add move to Game")
            val moveResult = gameResult.getMainlineMove()
            val posResult = gameResult.getPosition()
            val expectedPos =
                PositionImpl.fromSfen("9/4kP2p/9/3K4P/9/9/9/9/9 w P 4")
            moveResult shouldBe null
            posResult shouldBe expectedPos
        }
    }
})
