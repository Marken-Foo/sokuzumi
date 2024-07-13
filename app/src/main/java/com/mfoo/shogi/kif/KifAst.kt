package com.mfoo.shogi.kif

import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Position
import com.mfoo.shogi.Square
import kotlin.time.Duration

sealed interface KifAst {
    class Game(
        val startPos: Position,
        val rootMove: MoveNode,
        val headers: List<Header>,
    ) : KifAst

    class Header(val key: String, val value: String) : KifAst

    // Be permissive and allow moves after a game termination
    data class MoveNode(
        val children: List<MoveNode>,
        val move: Move,
    ): KifAst

    sealed interface Move : KifAst {
        val moveNum: Int
        val moveTime: Duration?
        val totalTime: Duration?
        val comment: String

        enum class GameEndType {
            ABORT, RESIGN, JISHOGI, SENNICHITE, FLAG, ILLEGAL_WIN, ILLEGAL_LOSS,
            NYUUGYOKU, NO_CONTEST_WIN, NO_CONTEST_LOSS, MATE, NO_MATE,
        }

        data class Regular(
            override val moveNum: Int,
            val startSq: Square,
            val endSq: Square,
            val isPromotion: Boolean,
            val komaType: KomaType,
            override val moveTime: Duration?,
            override val totalTime: Duration?,
            override val comment: String,
        ) : Move

        data class Drop(
            override val moveNum: Int,
            val sq: Square,
            val komaType: KomaType,
            override val moveTime: Duration?,
            override val totalTime: Duration?,
            override val comment: String
        ) : Move

        data class GameEnd(
            override val moveNum: Int,
            val endType: GameEndType,
            override val moveTime: Duration?,
            override val totalTime: Duration?,
            override val comment: String
        ) : Move
    }
}