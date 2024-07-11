package com.mfoo.shogi.kif

import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Position
import com.mfoo.shogi.Square
import kotlin.time.Duration

sealed interface KifAst {
    class Game(
        val startPos: Position,
        val moveList: List<Move>,
        val headers: List<Header>,
    ) : KifAst

    class Header(val key: String, val value: String) : KifAst
    class Move(
        val moveNum: Int,
        val startSq: Square,
        val endSq: Square,
        val isPromotion: Boolean,
        val komaType: KomaType,
        val captured: KomaType?,
        val moveTime: Duration?,
        val totalTime: Duration?,
    ) : KifAst
}
