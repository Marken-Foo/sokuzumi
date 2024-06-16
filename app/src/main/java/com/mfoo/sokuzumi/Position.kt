package com.mfoo.sokuzumi

import arrow.core.Either

@JvmInline
value class Sfen(val sfen: String)

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
    fun toggleSideToMove(): Side

    // SFEN functions
    fun toSfen(): Sfen
}

interface PositionFactory {
    fun empty(): Position
    fun fromSfen(sfen: Sfen): Position
}

data class PositionImpl(
    private val senteHand: Hand,
    private val goteHand: Hand,
    private val board: Board,
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
        side: Side, komaType: KomaType, amount: Int
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
        TODO("Not yet implemented")
    }

    override fun toggleSideToMove(): Side {
        TODO("Not yet implemented")
    }

    override fun toSfen(): Sfen {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        return if (other is PositionImpl) {
            this.senteHand == other.senteHand && this.goteHand == other.goteHand && this.board == other.board
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = senteHand.hashCode()
        result = 31 * result + goteHand.hashCode()
        result = 31 * result + board.hashCode()
        return result
    }

    companion object : PositionFactory {
        override fun empty(): Position {
            return PositionImpl(
                senteHand = HandImpl.empty(),
                goteHand = HandImpl.empty(),
                board = MailboxBoard.empty(),
            )
        }

        override fun fromSfen(sfen: Sfen): Position {
            TODO("Not yet implemented")
        }
    }
}
