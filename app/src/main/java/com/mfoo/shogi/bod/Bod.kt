package com.mfoo.shogi.bod

import com.mfoo.shogi.KanjiNumbers
import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Side

private object BodRegex {
    val handKoma =
        """(?<KomaType>[飛角金銀桂香歩])(?<Amount>[二三四五六七八九]|十[一二三四五六七八九]?)?　?""".toRegex()
    val goteHand =
        """^[ \t]*後手の持駒：(?<GoteHand>なし|.*)$""".toRegex()
    val senteHand =
        """^[ \t]*先手の持駒：(?<SenteHand>なし|.*)$""".toRegex()
    val boardSquare =
        """ ・|[ v][玉飛龍角馬金銀全桂圭香杏歩と]""".toRegex()
    val boardRow = """^[ \t]*\|.+\|""".toRegex()
}

/**
 * Parse a list of strings as a BOD format.
 * BOD format: https://github.com/Marken-Foo/shogi-translations/blob/main/BOD-features.md
 *
 * NOTE: Currently assumes the BOD is 14 lines long, and with 1-indexed line numbers:
 * - Begins with gote's hand (line 1)
 * - Contains board rows at lines 4-12
 * - Ends with sente's hand (line 14)
 */
fun parseBod(bodLines: List<String>): BodAst.Position? {
    if (bodLines.size < 14) {
        return null
    }
    val goteHand = parseGoteHand(bodLines[0])
    val board = BodAst.Board(bodLines.slice(3..11).map(::parseBoardRow))
    val senteHand = parseSenteHand(bodLines[13])
    return goteHand?.let {
        senteHand?.let {
            BodAst.Position(
                senteHand = senteHand,
                goteHand = goteHand,
                board = board
            )
        }
    }
}

private fun parseBoardRow(input: String): BodAst.Row {
    return BodRegex.boardSquare
        .findAll(input)
        .map(::parseBodSquare)
        .toList()
        .let { BodAst.Row(it) }
}

private fun parseBodSquare(match: MatchResult): Koma? {
    if (match.value == " ・") {
        return null
    }
    val side = when (match.value[0]) {
        ' ' -> Side.SENTE
        'v' -> Side.GOTE
        else -> null
    }
    val komaType = when (match.value[1]) {
        '玉' -> KomaType.OU
        '飛' -> KomaType.HI
        '龍' -> KomaType.RY
        '角' -> KomaType.KA
        '馬' -> KomaType.UM
        '金' -> KomaType.KI
        '銀' -> KomaType.GI
        '全' -> KomaType.NG
        '桂' -> KomaType.KE
        '圭' -> KomaType.NK
        '香' -> KomaType.KY
        '杏' -> KomaType.NY
        '歩' -> KomaType.FU
        'と' -> KomaType.TO
        else -> null
    }
    return side?.let { komaType?.let { Koma(side, komaType) } }
}

private fun parseGoteHand(line: String): BodAst.Hand? {
    return BodRegex.goteHand
        .matchAt(line, 0)
        ?.let {
            (it.groups["GoteHand"]?.value ?: "").let(::parseHandContents)
        }
}

private fun parseSenteHand(line: String): BodAst.Hand? {
    return BodRegex.senteHand
        .matchAt(line, 0)
        ?.let {
            (it.groups["SenteHand"]?.value ?: "").let(::parseHandContents)
        }
}

private fun parseHandContents(input: String): BodAst.Hand {
    if (input == "なし") {
        return BodAst.Hand(mapOf())
    }
    // Always returns all the komatype-amount pairs it can read, ignoring errors
    return BodRegex.handKoma
        .findAll(input)
        .map(::parseHandPair)
        .filterNotNull()
        .toMap()
        .let { BodAst.Hand(it) }
}

private fun parseHandPair(match: MatchResult): Pair<KomaType, Int>? {
    val amountInKanji = match.groups["Amount"]?.value
    val amount = if (amountInKanji == null) {
        1
    } else {
        KanjiNumbers.read(amountInKanji)
    }
    val komaType = match.groups["KomaType"]?.value?.let(::toKomaType)
    return komaType?.let { amount?.let { komaType to amount } }
}

private fun toKomaType(string: String): KomaType? {
    return when (string) {
        "歩" -> KomaType.FU
        "香" -> KomaType.KY
        "桂" -> KomaType.KE
        "銀" -> KomaType.GI
        "金" -> KomaType.KI
        "角" -> KomaType.KA
        "飛" -> KomaType.HI
        else -> null
    }
}