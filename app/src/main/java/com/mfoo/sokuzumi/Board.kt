package com.mfoo.sokuzumi

import arrow.core.Either

/**
 * Represents just the shogi board, without considering pieces in hand.
 */
interface Board {
    fun getKoma(sq: Square): Either<Unit, Koma?>
    fun setKoma(sq: Square, koma: Koma): Board
    fun removeKoma(sq: Square): Board
}

interface BoardFactory {
    fun empty(): Board
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
class MailboxBoard private constructor(
    val mailbox: List<MailboxContent>
) : Board {
    sealed interface MailboxContent {
        @JvmInline
        value class Koma(val value: com.mfoo.sokuzumi.Koma) : MailboxContent
        data object Empty : MailboxContent
        data object Invalid : MailboxContent
    }


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
        return MailboxBoard(mailbox = newMailbox.toList())
    }

    override fun removeKoma(sq: Square): Board {
        val newMailbox = this.mailbox.toMutableList()
        newMailbox[indexFromSq(sq)] = MailboxContent.Empty
        return MailboxBoard(mailbox = newMailbox.toList())
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MailboxBoard) {
            this.mailbox == other.mailbox
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return this.mailbox.hashCode()
    }

    companion object : BoardFactory {
        private const val NUM_ROWS = 13
        private const val NUM_COLS = 11
        override fun empty(): Board {
            val mailbox =
                MutableList(NUM_COLS * NUM_ROWS) { MailboxContent.Empty }
            (1..9).zip(1..9).map { (x, y) -> Square(Col(x), Row(y)) }
                .map(::indexFromSq)
                .forEach { idx -> mailbox.set(idx, MailboxContent.Empty) }
            return MailboxBoard(mailbox)
        }

        private fun colFromIndex(idx: Int): Col =
            Col(NUM_COLS - 1 - idx % NUM_COLS)

        private fun rowFromIndex(idx: Int): Row = Row(idx / NUM_COLS - 1)
        private fun indexFromSq(sq: Square): Int =
            NUM_COLS * (sq.row.int + 1) + (NUM_COLS - 1 - sq.col.int)
    }
}