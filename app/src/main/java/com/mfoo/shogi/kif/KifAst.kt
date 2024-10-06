package com.mfoo.shogi.kif

import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Position
import com.mfoo.shogi.Square
import com.mfoo.shogi.Tree
import kotlin.time.Duration

sealed interface KifAst {
    class Game<T>(
        val startPos: Position,
        val rootNode: Tree.RootNode<T>,
        val headers: List<Header>,
    ) : KifAst {
        override fun toString(): String {
            return "Headers: [${headers.joinToString(", ")}]\n${startPos}\n${rootNode}"
        }
    }

    class Header(val key: String, val value: String) : KifAst {
        override fun toString(): String {
            return "Header[${key}: ${value}]"
        }
    }

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
        ) : Move {
            override fun toString(): String {
                return "${moveNum}: ${komaType}-${startSq}-${endSq}${if (isPromotion) "+" else ""}"
            }
        }

        data class Drop(
            override val moveNum: Int,
            val sq: Square,
            val komaType: KomaType,
            override val moveTime: Duration?,
            override val totalTime: Duration?,
            override val comment: String,
        ) : Move {
            override fun toString(): String {
                return "${moveNum}: ${komaType}*${sq}"
            }
        }

        data class GameEnd(
            override val moveNum: Int,
            val endType: GameEndType,
            override val moveTime: Duration?,
            override val totalTime: Duration?,
            override val comment: String,
        ) : Move {
            override fun toString(): String {
                return "${moveNum}: ${endType}"
            }
        }
    }
}
