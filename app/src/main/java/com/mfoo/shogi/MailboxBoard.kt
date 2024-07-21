package com.mfoo.shogi

import arrow.core.Either

interface MailboxBoard {
    val mailbox: List<MailboxContent>

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
    value class Koma(val value: com.mfoo.shogi.Koma) : MailboxContent
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
) : Board, MailboxBoard {
    override fun getKoma(sq: Square): Either<Unit, Koma?> {
        return when (val content = mailbox[indexFromSq(sq)]) {
            is MailboxContent.Koma -> Either.Right(content.value)
            is MailboxContent.Empty -> Either.Right(null)
            is MailboxContent.Invalid -> Either.Left(Unit)
        }
    }

    override fun setKoma(sq: Square, koma: Koma): Board {
        val newMailbox = this.mailbox.toMutableList()
        newMailbox[indexFromSq(sq)] = MailboxContent.Koma(koma)
        return MailboxBoardImpl(mailbox = newMailbox.toList())
    }

    override fun removeKoma(sq: Square): Board {
        val newMailbox = this.mailbox.toMutableList()
        newMailbox[indexFromSq(sq)] = MailboxContent.Empty
        return MailboxBoardImpl(mailbox = newMailbox.toList())
    }

    override fun getColumn(col: Col): List<MailboxContent> {
        return (1..9)
            .map { NUM_COLS * (it + 1) + (NUM_COLS - 1 - col.int) }
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
                        is MailboxContent.Koma -> content.value.toCsa()
                    }
                }
        }
    }

    companion object : BoardFactory, MailboxCompanion {
        private const val NUM_ROWS = 13
        private const val NUM_COLS = 11
        override fun empty(): Board {
            val mailbox =
                MutableList(NUM_COLS * NUM_ROWS) { MailboxContent.Empty }
            (1..9).zip(1..9).map { (x, y) -> Square(Col(x), Row(y)) }
                .map(Companion::indexFromSq)
                .forEach { idx -> mailbox.set(idx, MailboxContent.Empty) }
            return MailboxBoardImpl(mailbox)
        }

        private fun colFromIndex(idx: Int): Col =
            Col(NUM_COLS - 1 - idx % NUM_COLS)

        private fun rowFromIndex(idx: Int): Row = Row(idx / NUM_COLS - 1)
        override fun indexFromSq(sq: Square): Int =
            NUM_COLS * (sq.row.int + 1) + (NUM_COLS - 1 - sq.col.int)

        override fun sqFromIndex(idx: Int): Square {
            return Square(colFromIndex(idx), rowFromIndex(idx))
        }
    }
}