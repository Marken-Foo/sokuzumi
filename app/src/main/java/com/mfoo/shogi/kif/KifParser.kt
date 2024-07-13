package com.mfoo.shogi.kif

import com.mfoo.shogi.Position
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.Square
import com.mfoo.shogi.bod.parseBod

private data class ParseState(val input: List<Token>, val parsePos: Int = 0) {
    fun peek(): Token? {
        return input.getOrNull(parsePos)
    }

    fun advance(numTokens: Int = 1): ParseState {
        return this.copy(parsePos = this.parsePos + numTokens)
    }
}

private data class ParseResult<T>(val value: T, val state: ParseState)

// Store handicap positions as SFEN for now because it's easier to read the code,
// but KIF parsing becomes dependent on SFEN parsing.
private val SfenFromHandicap = mapOf(
    "平手" to "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b - 1",
    "香落ち" to "lnsgkgsn1/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "右香落ち" to "1nsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "角落ち" to "lnsgkgsnl/1r7/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "飛車落ち" to "lnsgkgsnl/7b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "飛香落ち" to "lnsgkgsn1/7b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "二枚落ち" to "lnsgkgsnl/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "三枚落ち" to "1nsgkgsnl/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "四枚落ち" to "1nsgkgsn1/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "五枚落ち" to "2sgkgsn1/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "左五枚落ち" to "1nsgkgs2/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "六枚落ち" to "2sgkgs2/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "八枚落ち" to "3gkg3/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "十枚落ち" to "4k4/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1"
)

private class GameInformation(
    val headers: List<KifAst.Header>,
    val startPosition: Position,
)

/**
 * Parse the headers/game information section of the KIF.
 */
private fun parseGameInformation(input: ParseState): ParseResult<GameInformation> {
    var state = input
    var token = state.peek()
    val headers: MutableList<KifAst.Header> = mutableListOf()
    var startPos: Position? = null
    while (token != null) {
        when (token) {
            is Token.Handicap -> startPos =
                SfenFromHandicap[token.name]?.let { PositionImpl.fromSfen(it) }

            is Token.Bod -> startPos =
                parseBod(token.lines)?.let { PositionImpl.fromBodAst(it) }

            is Token.Comment -> Unit
            is Token.Escape -> Unit
            is Token.HeaderKeyValuePair -> headers.add(
                KifAst.Header(
                    key = token.key,
                    value = token.value
                )
            )

            is Token.MoveLine -> break
            Token.MoveSectionDelineation -> break
            Token.Unknown -> Unit
            is Token.VariationStart -> break
        }
        state = state.advance()
        token = state.peek()
    }
    return ParseResult(
        GameInformation(
            headers,
            startPos ?: PositionImpl.empty()
        ), state
    )
}

private fun parseMainMoveSection(input: ParseState): ParseResult<List<KifAst.Move>> {
    var state = input
    if (state.peek() is Token.MoveSectionDelineation) {
        state = state.advance()
    }
    val (moveList, endState) = parseMoveSequence(state)
    return ParseResult(moveList, endState)
}

private fun parseMoveSequence(input: ParseState): ParseResult<List<KifAst.Move>> {
    var state = input
    var token = state.peek()

    val moveList = mutableListOf<KifAst.Move>()
    // The move currently under construction
    var currentMove: KifAst.Move? = null
    var previousSq: Square? = null

    while (token != null) {
        when (token) {
            is Token.Bod -> break // Unexpected
            is Token.Comment -> {
                currentMove = currentMove?.addCommentLine(token.line)
            }

            is Token.Escape -> Unit
            is Token.Handicap -> break // Unexpected
            is Token.HeaderKeyValuePair -> break // Unexpected
            is Token.MoveLine -> {
                val move = parseMove(token, previousSq)
                when (move) {
                    // TODO: handle error more gracefully
                    null -> return ParseResult(moveList, state)
                    is KifAst.Move.Drop -> previousSq = move.sq
                    is KifAst.Move.GameEnd -> Unit
                    is KifAst.Move.Regular -> previousSq = move.endSq
                }
                currentMove?.let { moveList.add(it) }
                currentMove = move
            }

            Token.MoveSectionDelineation -> break // Unexpected
            Token.Unknown -> break // Unexpected
            is Token.VariationStart -> break // signals start of variations section
        }
        state = state.advance()
        token = state.peek()
    }
    currentMove?.let { moveList.add(currentMove) }
    return ParseResult(moveList, state)
}

private class Variation(val moveNum: Int, val moveList: List<KifAst.Move>)

private fun parseVariationSection(input: ParseState): ParseResult<List<Variation>> {
    var state = input
    val variations = mutableListOf<Variation?>()
    while (state.peek() is Token.VariationStart) {
        val (variation, nextState) = parseVariation(state)
        variations.add(variation)
        state = nextState
    }
    return ParseResult(variations.filterNotNull(), state)
}

private fun parseVariation(input: ParseState): ParseResult<Variation?> {
    val token = input.peek()
    if (token !is Token.VariationStart) {
        return ParseResult(null, input)
    }
    val (moveList, state) = parseMoveSequence(input.advance())
    return ParseResult(Variation(token.moveNum, moveList), state)
}

fun main() {
    val buf = readFile("./sample_problems/variations.kif")
    val tokens = tokenise(buf)
    println(tokens)
    val (gameInfo, state1) = parseGameInformation(ParseState(tokens))
    println(gameInfo.startPosition)

    val (moveList, state2) = parseMainMoveSection(state1)
    println(moveList)

    val (variationList, state3) = parseVariationSection(state2)
    for (variation in variationList) {
        println("Move number: ${variation.moveNum}")
        println(variation.moveList)
    }
}
