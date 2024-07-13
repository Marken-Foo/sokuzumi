package com.mfoo.shogi.kif

import java.io.BufferedReader
import java.io.File
import java.nio.charset.Charset
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface Token {
    data class Handicap(val name: String) : Token
    data class Bod(val lines: List<String>) : Token
    data object MoveSectionDelineation : Token
    data class MoveLine(
        val moveNum: Int,
        val moveStr: String,
        val moveTime: Duration?,
        val totalTime: Duration?,
    ) : Token

    data class Comment(val line: String) : Token
    data class Escape(val line: String) : Token
    data class VariationStart(val moveNum: Int) : Token
    data class HeaderKeyValuePair(val key: String, val value: String) : Token
    data object Unknown : Token
}

private object KifRegex {
    val handicap = """^[ \t]*手合割：(?<Handicap>.*)""".toRegex()
    val bodStart = """^[ \t]*後手の持駒：""".toRegex()
    val bodEnd = """^[ \t]*先手の持駒：""".toRegex()
    val moveSectionDelineation = """^[ \t]*手数--""".toRegex()
    val comment = """^\*(?<Comment>.*)""".toRegex()
    val escape = """^#(?<Escape>.*)""".toRegex()
    val variation = """^[ \t]*変化：(?<MoveNum>\d+)手""".toRegex()
    val headerKeyValuePair = """^[ \t]*(?<Key>.+)：(?<Value>.*)""".toRegex()
    private const val moveNum = """(?<MoveNum>[0-9]+)"""
    private const val moveBody = """(?<MoveBody>[　\S]+)"""
    private const val moveTime = """(?<MoveMinutes>\d+):(?<MoveSeconds>\d{2})"""
    private const val totalTime =
        """(?<TotalHours>\d+):(?<TotalMinutes>\d{2}):(?<TotalSeconds>\d{2})"""
    private const val moveTimes =
        """\(\s*(?<MoveTime>$moveTime)\s*/\s*(?<TotalTime>$totalTime)\s*\)"""
    private const val moveLineStr =
        "^[ \\t]*$moveNum[ \\t]*$moveBody[ \\t]*(?:$moveTimes)?[ \\t]*$"
    val moveLine = moveLineStr.toRegex()
}


fun readFile(filename: String): BufferedReader {
    return File(filename).bufferedReader(Charset.forName("SHIFT-JIS"))
}

fun tokenise(input: BufferedReader): List<Token> {
    val tokens: MutableList<Token> = mutableListOf()
    var line: String? = input.readLine() ?: return tokens

    while (line != null) {
        // BOD is a multiline token
        if (KifRegex.bodStart.matchAt(line, 0) != null) {
            val bodLines: MutableList<String> = mutableListOf()
            while (KifRegex.bodEnd.matchAt(line!!, 0) == null) {
                bodLines.add(line)
                line = input.readLine() ?: break
            }
            bodLines.add(line)
            tokens.add(Token.Bod(bodLines))
            line = input.readLine()
        } else {
            tokens.add(tokeniseLine(line))
            line = input.readLine()
        }
    }
    return tokens
}

private fun getMoveDuration(match: MatchResult): Duration? {
    if (match.groups["MoveTime"] == null) {
        return null
    }
    val minutes = match.groups["MoveMinutes"]?.value?.toInt() ?: 0
    val seconds = match.groups["MoveSeconds"]?.value?.toInt() ?: 0
    return (minutes * 60 + seconds).seconds
}

private fun getTotalDuration(match: MatchResult): Duration? {
    if (match.groups["TotalTime"] == null) {
        return null
    }
    val hours = match.groups["TotalHours"]?.value?.toInt() ?: 0
    val minutes = match.groups["TotalMinutes"]?.value?.toInt() ?: 0
    val seconds = match.groups["TotalSeconds"]?.value?.toInt() ?: 0
    return (hours * 3600 + minutes * 60 + seconds).seconds
}

fun tokeniseLine(input: String): Token {
    KifRegex.moveLine
        .matchAt(input, 0)
        ?.let {
            val moveNum = it.groups["MoveNum"]?.value?.toInt() ?: 0
            val moveBody = it.groups["MoveBody"]?.value ?: ""
            val moveTime = getMoveDuration(it)
            val totalTime = getTotalDuration(it)
            return Token.MoveLine(
                moveNum = moveNum,
                moveStr = moveBody,
                moveTime = moveTime,
                totalTime = totalTime
            )
        }
    KifRegex.escape
        .matchAt(input, 0)
        ?.let { return Token.Escape(it.groups["Escape"]?.value ?: "") }
    KifRegex.comment
        .matchAt(input, 0)
        ?.let { return Token.Comment(it.groups["Comment"]?.value ?: "") }
    KifRegex.handicap
        .matchAt(input, 0)
        ?.let { return Token.Handicap(it.groups["Handicap"]?.value ?: "") }
    KifRegex.moveSectionDelineation
        .matchAt(input, 0)
        ?.let { return Token.MoveSectionDelineation }
    KifRegex.variation
        .matchAt(input, 0)
        ?.let {
            return Token.VariationStart(
                it.groups["MoveNum"]?.value?.toIntOrNull() ?: Int.MAX_VALUE
            )
        }
    KifRegex.headerKeyValuePair
        .matchAt(input, 0)
        ?.let {
            val key = it.groups["Key"]?.value ?: ""
            val value = it.groups["Value"]?.value ?: ""
            return Token.HeaderKeyValuePair(key, value)
        }
    return Token.Unknown
}

fun main() {
    val buf = readFile("./sample_problems/3te/10.kif")
    println(tokenise(buf))
}
