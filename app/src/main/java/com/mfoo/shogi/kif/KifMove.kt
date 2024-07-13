package com.mfoo.shogi.kif

import com.mfoo.shogi.Col
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Row
import com.mfoo.shogi.Square


internal fun KifAst.Move.addCommentLine(line: String): KifAst.Move {
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

internal fun parseMove(
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
        match.groups["DestSq"]?.value?.let { parseDestSq(it, prevSq) }
            ?: return null
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
