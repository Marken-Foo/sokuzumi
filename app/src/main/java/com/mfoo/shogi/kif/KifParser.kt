package com.mfoo.shogi.kif

import com.mfoo.shogi.Col
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Position
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.Row
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

private data class GameInformation(
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

private fun parseMoveSection(input: ParseState): ParseResult<List<KifAst.Move>> {
    var state = input
    var token = state.peek()

    val moveList = mutableListOf<KifAst.Move>()
    // The move currently under construction
    var currentMove: KifAst.Move? = null
    var previousSq: Square? = null

    while (token != null) {
        when (token) {
            is Token.Bod -> Unit
            is Token.Comment -> {
                currentMove = currentMove?.addCommentLine(token.line)
            }

            is Token.Escape -> Unit
            is Token.Handicap -> Unit
            is Token.HeaderKeyValuePair -> Unit
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

            Token.MoveSectionDelineation -> Unit
            Token.Unknown -> Unit
            is Token.VariationStart -> break // signals start of variations section
        }
        state = state.advance()
        token = state.peek()
    }
    currentMove?.let { moveList.add(currentMove) }
    return ParseResult(moveList, state)
}

private fun KifAst.Move.addCommentLine(line: String): KifAst.Move {
    val newComment = if (this.comment == "") {
        line
    } else {
        "${this.comment}\n${line}"
    }
    return when (this) {
        is KifAst.Move.Regular -> this.copy(comment = newComment)
        is KifAst.Move.Drop -> this.copy(comment = newComment)
        is KifAst.Move.GameEnd -> this.copy(comment = newComment)
    }
}

private object KifMoveRegex {
    private val destSq = """(?<DestSq>同　|[１２３４５６７８９][一二三四五六七八九])"""
    private val komaType =
        """(?<KomaType>成[香桂銀]|[歩香桂銀金角飛玉と龍竜馬全圭杏])"""
    private val dropOrPromotion = """(?<DropOrPromotion>[打成])"""
    private val originSq = """(?<OriginSq>[1-9]{2})"""
    val move =
        """${destSq}${komaType}${dropOrPromotion}?(?:\(${originSq}\))?""".toRegex()
}

private fun parseMove(
    token: Token.MoveLine,
    prevSq: Square?,
): KifAst.Move? {
    parseGameEndType(token.moveStr)?.let {
        return KifAst.Move.GameEnd(
            moveNum = token.moveNum,
            endType = it,
            moveTime = token.moveTime,
            totalTime = token.totalTime,
            comment = "",
        )
    }
    val match = KifMoveRegex.move.matchAt(token.moveStr, 0) ?: return null
    val destSq =
        match.groups["DestSq"]?.value?.let { parseDestSq(it, prevSq) } ?: return null
    val komaType =
        match.groups["KomaType"]?.value?.let(::parseKomaType) ?: return null
    val (isDrop, isPromotion) = match.groups["DropOrPromotion"]?.value.let {
        Pair(it == "打", it == "成")
    }
    if (isDrop) {
        return KifAst.Move.Drop(
            moveNum = token.moveNum,
            sq = destSq,
            komaType = komaType,
            moveTime = token.moveTime,
            totalTime = token.totalTime,
            comment = "",
        )
    }

    val originSq = match.groups["OriginSq"]?.value?.let { str ->
        if (str.length != 2) {
            return null
        }
        Square(Col(str[0].digitToInt()), Row(str[1].digitToInt()))
    } ?: return null
    return KifAst.Move.Regular(
        moveNum = token.moveNum,
        startSq = originSq,
        endSq = destSq,
        isPromotion = isPromotion,
        komaType = komaType,
        moveTime = token.moveTime,
        totalTime = token.totalTime,
        comment = "",
    )
}

private fun parseDestSq(input: String, prevSq: Square?): Square? {
    if (input == "同　") {
        return prevSq
    }
    if (input.length != 2) {
        return null
    }
    val col = when (input[0]) {
        '１' -> 1
        '２' -> 2
        '３' -> 3
        '４' -> 4
        '５' -> 5
        '６' -> 6
        '７' -> 7
        '８' -> 8
        '９' -> 9
        else -> null
    }
    val row = when (input[1]) {
        '一' -> 1
        '二' -> 2
        '三' -> 3
        '四' -> 4
        '五' -> 5
        '六' -> 6
        '七' -> 7
        '八' -> 8
        '九' -> 9
        else -> null
    }
    return col?.let { row?.let { Square(Col(col), Row(row)) } }
}

private fun parseKomaType(input: String): KomaType? {
    return when (input) {
        "玉" -> KomaType.OU
        "飛" -> KomaType.HI
        "龍" -> KomaType.RY
        "竜" -> KomaType.RY
        "角" -> KomaType.KA
        "馬" -> KomaType.UM
        "金" -> KomaType.KI
        "銀" -> KomaType.GI
        "全" -> KomaType.NG
        "桂" -> KomaType.KE
        "圭" -> KomaType.NK
        "香" -> KomaType.KY
        "杏" -> KomaType.NY
        "歩" -> KomaType.FU
        "と" -> KomaType.TO
        "成香" -> KomaType.NY
        "成桂" -> KomaType.NK
        "成銀" -> KomaType.NG
        else -> null
    }
}

private fun parseGameEndType(input: String): KifAst.Move.GameEndType? {
    return when (input) {
        "中断" -> KifAst.Move.GameEndType.ABORT
        "投了" -> KifAst.Move.GameEndType.RESIGN
        "持将棋" -> KifAst.Move.GameEndType.JISHOGI
        "千日手" -> KifAst.Move.GameEndType.SENNICHITE
        "切れ負け" -> KifAst.Move.GameEndType.FLAG
        "反則勝ち" -> KifAst.Move.GameEndType.ILLEGAL_WIN
        "反則負け" -> KifAst.Move.GameEndType.ILLEGAL_LOSS
        "入玉勝ち" -> KifAst.Move.GameEndType.NYUUGYOKU
        "不戦勝" -> KifAst.Move.GameEndType.NO_CONTEST_WIN
        "不戦敗" -> KifAst.Move.GameEndType.NO_CONTEST_LOSS
        "詰み" -> KifAst.Move.GameEndType.MATE
        "不詰" -> KifAst.Move.GameEndType.NO_MATE
        else -> null
    }
}

fun main() {
    val buf = readFile("./sample_problems/3te/10.kif")
    val tokens = tokenise(buf)
    println(tokens)
    val (gameInfo, state1) = parseGameInformation(ParseState(tokens))
    println(gameInfo.startPosition)
    val (moveList, state2) = parseMoveSection(state1)
    println(moveList)
}