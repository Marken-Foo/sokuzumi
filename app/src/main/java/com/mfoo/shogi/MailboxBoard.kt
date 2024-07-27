package com.mfoo.shogi

import arrow.core.Either

interface MailboxBoard {
    val mailbox: List<MailboxContent>
    val senteOu: Int?
    val goteOu: Int?

    /**
     * Returns the content of the column. Useful for nifu detection.
     */
    fun getColumn(col: Col): List<MailboxContent>
}

sealed interface MailboxCompanion {
    enum class Direction(val t: Int) {
        N(-11),
        NE(-10),
        E(1),
        SE(12),
        S(11),
        SW(10),
        W(-1),
        NW(-12)
    }

    fun indexFromSq(sq: Square): Int
    fun sqFromIndex(idx: Int): Square
}

sealed interface MailboxContent {
    @JvmInline
    value class Koma(val t: com.mfoo.shogi.Koma) : MailboxContent
    data object Empty : MailboxContent
    data object Invalid : MailboxContent
}

/**
 * `MailboxBoard` uses a mailbox representation of the shogi board to simplify
 * move generation for standard shogi.
 *
 * The mailbox is a 1-D array that can be interpreted as 13 rows * 11 columns,
 * padding the 9 * 9 shogi board with one column on either side, and two rows
 * each above and below. The padded indexes contain `Invalid` sentinel values.
 *
 * The indexes are enumerated from top to bottom and left to right, such that
 * e.g. the shogi 11 square has index 31, the shogi 12 square has index 42, etc.
 */
class MailboxBoardImpl private constructor(
    override val mailbox: List<MailboxContent>,
    override val senteOu: Int?,
    override val goteOu: Int?,
) : Board, MailboxBoard {

    override fun getKoma(sq: Square): Either<Unit, Koma?> {
        return when (val content = mailbox[indexFromSq(sq)]) {
            is MailboxContent.Koma -> Either.Right(content.t)
            is MailboxContent.Empty -> Either.Right(null)
            is MailboxContent.Invalid -> Either.Left(Unit)
        }
    }

    override fun setKoma(sq: Square, koma: Koma): Board {
        val newMailbox = this.mailbox.toMutableList()
        val idx = indexFromSq(sq)
        newMailbox[idx] = MailboxContent.Koma(koma)
        return MailboxBoardImpl(
            mailbox = newMailbox.toList(),
            senteOu = if (koma == Koma(Side.SENTE, KomaType.OU)) {
                idx
            } else {
                senteOu
            },
            goteOu = if (koma == Koma(Side.GOTE, KomaType.OU)) {
                idx
            } else {
                goteOu
            },
        )
    }

    override fun removeKoma(sq: Square): Board {
        val newMailbox = this.mailbox.toMutableList()
        newMailbox[indexFromSq(sq)] = MailboxContent.Empty
        return MailboxBoardImpl(
            mailbox = newMailbox.toList(),
            senteOu = senteOu,
            goteOu = goteOu,
        )
    }

    override fun getColumn(col: Col): List<MailboxContent> {
        return (1..9)
            .map { NUM_COLS * (it + 1) + (NUM_COLS - 1 - col.t) }
            .map { mailbox[it] }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MailboxBoardImpl) {
            this.mailbox == other.mailbox
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return this.mailbox.hashCode()
    }

    override fun toString(): String {
        val boardSquares = (1..9).map { y ->
            (9 downTo 1).map { x -> Square(Col(x), Row(y)) }
        }
        return "\n" + boardSquares.joinToString("\n") { row ->
            row
                .map(Companion::indexFromSq)
                .map { idx -> mailbox[idx] }
                .joinToString(",") { content ->
                    when (content) {
                        is MailboxContent.Invalid -> ""
                        is MailboxContent.Empty -> " . "
                        is MailboxContent.Koma -> content.t.toCsa()
                    }
                }
        }
    }

    companion object : BoardFactory, MailboxCompanion {
        private const val NUM_ROWS = 13
        private const val NUM_COLS = 11
        override fun empty(): Board {
            val mailbox =
                MutableList<MailboxContent>(NUM_COLS * NUM_ROWS) { MailboxContent.Invalid }
            for (col in 1..9) {
                for (row in 1..9) {
                    Square(Col(col), Row(row))
                        .let(::indexFromSq)
                        .let { mailbox.set(it, MailboxContent.Empty) }
                }
            }
            return MailboxBoardImpl(mailbox, null, null)
        }

        private fun colFromIndex(idx: Int): Col =
            Col(NUM_COLS - 1 - idx % NUM_COLS)

        private fun rowFromIndex(idx: Int): Row = Row(idx / NUM_COLS - 1)
        override fun indexFromSq(sq: Square): Int =
            NUM_COLS * (sq.row.t + 1) + (NUM_COLS - 1 - sq.col.t)

        override fun sqFromIndex(idx: Int): Square {
            return Square(colFromIndex(idx), rowFromIndex(idx))
        }
    }
}
