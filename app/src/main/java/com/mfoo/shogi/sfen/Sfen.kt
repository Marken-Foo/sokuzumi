package com.mfoo.shogi.sfen

import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Side

private fun main() {
    val res = parseSfen("7r1/6B1p/6Bsk/9/7P1/9/9/5+P+p2/8N w 2Sr4gs3n4l14p 1")
    println(res)
}

fun parseSfen(input: String): SfenAst.ShogiPosition? {
    val parts = input.split("""[ \t]+""".toRegex())
    if (parts.size > 4) {
        // Too many parts in SFEN
        return null
    }
    if (parts.size < 4) {
        // Missing parts in SFEN
        return null
    }
    val (boardPart, turnPart, handsPart, moveNumPart) = parts

    val board = parseBoard(boardPart)
    val turn = when (turnPart) {
        "b" -> Side.SENTE
        "w" -> Side.GOTE
        else -> null
    }
    val hands = parseHands(handsPart)
    val moveNum = moveNumPart.toIntOrNull()

    return turn?.let {
        moveNum?.let {
            SfenAst.ShogiPosition(
                board,
                hands.senteHand,
                hands.goteHand,
                turn,
                moveNum
            )
        }
    }
}

private val boardPattern =
    mapOf(
        "Char" to """[a-zA-Z]""",
        "PlusChar" to """\+[a-zA-Z]""",
        "Integer" to """[1-9][0-9]*""",
        "Slash" to """/""",
        "Unknown" to """.""",
    )
        .map { (name, pattern) -> """(?<${name}>${pattern})""" }
        .joinToString("|")

private fun parseBoardItems(match: MatchResult): List<Koma?> {
    return if (match.groups["Char"] != null) {
        val c = match.groups["Char"]?.value?.get(0) ?: Char.MIN_VALUE
        listOf(toKoma(c))
    } else if (match.groups["PlusChar"] != null) {
        val c = match.groups["PlusChar"]?.value?.get(1) ?: Char.MIN_VALUE
        listOf(toPromotedKoma(c))
    } else if (match.groups["Integer"] != null) {
        val n = match.groups["Integer"]?.value?.toInt() ?: 0
        List(n) { null }
    } else {
        // Unknown item in board
        listOf()
    }
}

private fun parseRow(input: String): SfenAst.Row {
    return Regex(boardPattern)
        .findAll(input)
        .flatMap(::parseBoardItems)
        .let { SfenAst.Row(it.toList()) }
}

private fun parseBoard(input: String): SfenAst.Board {
    return input
        .split('/')
        .map(::parseRow)
        .let { SfenAst.Board(it) }
}

private const val handPattern = """(?<Amount>[1-9][0-9]*)?(?<Koma>[a-zA-Z])"""

private fun parseHandItem(match: MatchResult): Pair<Koma, Int>? {
    val amount = match.groups["Amount"]?.value?.toIntOrNull() ?: 1
    val koma = match.groups["Koma"]?.value?.firstOrNull()?.let(::toKoma)
    return koma?.let { Pair(it, amount) }
}

private fun parseHands(input: String): SfenAst.Hands {
    if (input == "-") {
        return SfenAst.Hands(
            senteHand = SfenAst.Hand(mapOf()),
            goteHand = SfenAst.Hand(mapOf())
        )
    }
    val (senteContents, goteContents) = input
        .let { Regex(handPattern).findAll(it).map(::parseHandItem) }
        .filterNotNull()
        .partition { (koma, _) -> koma.side.isSente() }
    val senteHand = senteContents
        .associate { (k, amount) -> k.komaType to amount }
        .let { SfenAst.Hand(it) }
    val goteHand = goteContents
        .associate { (k, amount) -> k.komaType to amount }
        .let { SfenAst.Hand(it) }
    return SfenAst.Hands(senteHand = senteHand, goteHand = goteHand)
}

private fun toKoma(char: Char): Koma? {
    val side =
        if (char.isLowerCase()) Side.GOTE else Side.SENTE
    val komaType = when (char.uppercaseChar()) {
        'P' -> KomaType.FU
        'L' -> KomaType.KY
        'N' -> KomaType.KE
        'S' -> KomaType.GI
        'G' -> KomaType.KI
        'B' -> KomaType.KA
        'R' -> KomaType.HI
        'K' -> KomaType.OU
        else -> null
    }
    return komaType?.let { Koma(side, it) }
}

private fun toPromotedKoma(char: Char): Koma? {
    val side =
        if (char.isLowerCase()) Side.GOTE else Side.SENTE
    val komaType = when (char.uppercaseChar()) {
        'P' -> KomaType.TO
        'L' -> KomaType.NY
        'N' -> KomaType.NK
        'S' -> KomaType.NG
        'B' -> KomaType.UM
        'R' -> KomaType.RY
        else -> null
    }
    return komaType?.let { Koma(side, it) }
}
