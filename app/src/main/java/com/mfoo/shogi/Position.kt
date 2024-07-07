package com.mfoo.shogi

import arrow.core.Either
import com.mfoo.shogi.sfen.parseSfen

/**
 * Represents a shogi position state, equivalent to the information in an SFEN.
 */
interface Position {
    // Hand functions
    fun getHandOfSide(side: Side): Hand
    fun getHandAmount(side: Side, komaType: KomaType): Int
    fun setHandAmount(side: Side, komaType: KomaType, amount: Int): Position
    fun incrementHandAmount(side: Side, komaType: KomaType): Position
    fun decrementHandAmount(side: Side, komaType: KomaType): Position

    // Board functions
    fun getKoma(sq: Square): Either<Unit, Koma?>
    fun setKoma(sq: Square, koma: Koma): Position
    fun removeKoma(sq: Square): Position
    fun getAllKoma(): Map<Square, Koma>

    // Game state functions
    fun getSideToMove(): Side
    fun toggleSideToMove(): Position
}

interface PositionFactory {
    fun empty(): Position
    fun fromSfen(sfen: String): Position?
}

data class PositionImpl(
    private val senteHand: Hand,
    private val goteHand: Hand,
    private val board: Board,
    private val sideToMove: Side,
) : Position {
    override fun getHandOfSide(side: Side): Hand {
        return when (side) {
            Side.SENTE -> senteHand
            Side.GOTE -> goteHand
        }
    }

    override fun getHandAmount(side: Side, komaType: KomaType): Int {
        return getHandOfSide(side).getAmount(komaType)
    }

    override fun setHandAmount(
        side: Side, komaType: KomaType, amount: Int,
    ): Position {
        return when (side) {
            Side.SENTE -> copy(
                senteHand = this.getHandOfSide(side).setAmount(komaType, amount)
            )

            Side.GOTE -> copy(
                goteHand = this.getHandOfSide(side).setAmount(komaType, amount)
            )
        }
    }

    override fun incrementHandAmount(side: Side, komaType: KomaType): Position {
        return when (side) {
            Side.SENTE -> copy(
                senteHand = this.getHandOfSide(side).increment(komaType)
            )

            Side.GOTE -> copy(
                goteHand = this.getHandOfSide(side).increment(komaType)
            )
        }
    }

    override fun decrementHandAmount(side: Side, komaType: KomaType): Position {
        return when (side) {
            Side.SENTE -> copy(
                senteHand = this.getHandOfSide(side).decrement(komaType)
            )

            Side.GOTE -> copy(
                goteHand = this.getHandOfSide(side).decrement(komaType)
            )
        }
    }

    override fun getKoma(sq: Square): Either<Unit, Koma?> {
        return board.getKoma(sq)
    }

    override fun setKoma(sq: Square, koma: Koma): Position {
        return copy(board = board.setKoma(sq, koma))
    }

    override fun removeKoma(sq: Square): Position {
        return copy(board = board.removeKoma(sq))
    }

    override fun getAllKoma(): Map<Square, Koma> {
        return Square.all().mapNotNull { sq ->
            val koma = board.getKoma(sq).getOrNull()
            if (koma != null) Pair(sq, koma) else null
        }.toMap()
    }

    override fun getSideToMove(): Side {
        return sideToMove
    }

    override fun toggleSideToMove(): Position {
        return copy(sideToMove = sideToMove.switch())
    }

    override fun equals(other: Any?): Boolean {
        return if (other is PositionImpl) {
            this.senteHand == other.senteHand
                && this.goteHand == other.goteHand
                && this.board == other.board
                && this.sideToMove == other.sideToMove
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = senteHand.hashCode()
        result = 31 * result + goteHand.hashCode()
        result = 31 * result + board.hashCode()
        result = 31 * result + sideToMove.hashCode()
        return result
    }

    companion object : PositionFactory {
        override fun empty(): Position {
            return PositionImpl(
                senteHand = HandImpl.empty(),
                goteHand = HandImpl.empty(),
                board = MailboxBoard.empty(),
                sideToMove = Side.SENTE,
            )
        }

        override fun fromSfen(sfen: String): Position? {
            val sfenTree = parseSfen(sfen) ?: return null
            return empty()
                .let {
                    var pos = it
                    for ((rowIdx, row) in sfenTree.board.rows.withIndex()) {
                        for ((colIdx, koma) in row.komas.withIndex()) {
                            if (koma == null) continue;
                            val sq = Square(Col(9 - colIdx), Row(rowIdx + 1))
                            pos = pos.setKoma(sq, koma)
                        }
                    }
                    pos
                }
                .let {
                    var pos = it
                    for ((komaType, amount) in sfenTree.senteHand.contents) {
                        pos = pos.setHandAmount(Side.SENTE, komaType, amount)
                    }
                    for ((komaType, amount) in sfenTree.goteHand.contents) {
                        pos = pos.setHandAmount(Side.GOTE, komaType, amount)
                    }
                    pos
                }
                .let {
                    if (sfenTree.sideToMove.isSente()) {
                        it
                    } else
                        it.toggleSideToMove()
                }
        }
    }
}
