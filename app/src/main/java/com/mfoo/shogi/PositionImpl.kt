package com.mfoo.shogi

import arrow.core.Either
import com.mfoo.shogi.bod.BodAst
import com.mfoo.shogi.sfen.SfenAst
import com.mfoo.shogi.sfen.parseSfen


interface MailboxPosition {
    fun getMailbox(): MailboxBoard
}

data class PositionImpl(
    private val senteHand: Hand,
    private val goteHand: Hand,
    private val board: Board,
    private val sideToMove: Side,
) : Position, MailboxPosition {

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
    ): PositionImpl {
        val newHand = getHandOfSide(side).setAmount(komaType, amount)
        return when (side) {
            Side.SENTE -> copy(senteHand = newHand)
            Side.GOTE -> copy(goteHand = newHand)
        }
    }

    override fun incrementHandAmount(
        side: Side,
        komaType: KomaType,
    ): PositionImpl {
        val newHand = getHandOfSide(side).increment(komaType)
        return when (side) {
            Side.SENTE -> copy(senteHand = newHand)
            Side.GOTE -> copy(goteHand = newHand)
        }
    }

    override fun decrementHandAmount(
        side: Side,
        komaType: KomaType,
    ): PositionImpl {
        val newHand = getHandOfSide(side).decrement(komaType)
        return when (side) {
            Side.SENTE -> copy(senteHand = newHand)
            Side.GOTE -> copy(goteHand = newHand)
        }
    }

    override fun getKoma(sq: Square): Either<Unit, Koma?> {
        return board.getKoma(sq)
    }

    override fun setKoma(sq: Square, koma: Koma): PositionImpl {
        return copy(board = board.setKoma(sq, koma))
    }

    override fun removeKoma(sq: Square): PositionImpl {
        return copy(board = board.removeKoma(sq))
    }

    override fun getAllKoma(): Map<Square, Koma> {
        return Square.all()
            .mapNotNull { sq ->
                val koma = board.getKoma(sq).getOrNull()
                if (koma != null) Pair(sq, koma) else null
            }
            .toMap()
    }

    override fun getSideToMove(): Side {
        return sideToMove
    }

    override fun setSideToMove(side: Side): PositionImpl {
        return copy(sideToMove = side)
    }

    override fun toggleSideToMove(): PositionImpl {
        return copy(sideToMove = sideToMove.switch())
    }

    override fun doMove(move: Move): PositionImpl {
        return when (move) {
            is Move.GameEnd -> this
            is Move.Regular -> {
                with(move) {
                    val capturedKoma = getKoma(endSq)
                    val finalKoma =
                        if (isPromotion && komaType.isPromotable()) {
                            Koma(side, komaType.promote())
                        } else {
                            Koma(side, komaType)
                        }
                    toggleSideToMove()
                        .removeKoma(startSq)
                        .setKoma(endSq, finalKoma)
                        .putCapturedKomaInHand(side, capturedKoma)
                }
            }

            is Move.Drop -> {
                with(move) {
                    toggleSideToMove()
                        .decrementHandAmount(side, komaType)
                        .setKoma(sq, Koma(side, komaType))
                }
            }
        }
    }

    fun undoMove(move: Move): PositionImpl {
        return when (move) {
            is Move.GameEnd -> this
            is Move.Regular -> {
                with(move) {
                    val initialKoma = if (isPromotion) {
                        Koma(side, komaType.demote())
                    } else {
                        Koma(side, komaType)
                    }
                    toggleSideToMove()
                        .setKoma(startSq, initialKoma)
                        .let {
                            if (capturedKoma == null) {
                                it
                            } else {
                                it.setKoma(endSq, capturedKoma)
                                    .decrementHandAmount(
                                        side,
                                        capturedKoma.komaType
                                    )
                            }
                        }
                }
            }

            is Move.Drop -> {
                with(move) {
                    toggleSideToMove()
                        .incrementHandAmount(side, komaType)
                        .removeKoma(sq)
                }
            }
        }
    }

    private fun putCapturedKomaInHand(
        side: Side,
        capturedKoma: Either<Unit, Koma?>,
    ): PositionImpl {
        return capturedKoma.fold(
            { this },
            { k ->
                if (k != null) {
                    this.incrementHandAmount(side, k.komaType.demote())
                } else {
                    this
                }
            }
        )
    }

    override fun getMailbox(): MailboxBoard {
        return this.board as MailboxBoard
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
        override fun empty(): PositionImpl {
            return PositionImpl(
                senteHand = HandImpl.empty(),
                goteHand = HandImpl.empty(),
                board = MailboxBoardImpl.empty(),
                sideToMove = Side.SENTE,
            )
        }

        override fun fromSfen(sfen: String): PositionImpl? {
            return parseSfen(sfen)?.let(::fromSfenAst) ?: return null
        }

        override fun fromSfenAst(sfenTree: SfenAst.ShogiPosition): PositionImpl {
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
                    } else {
                        it.toggleSideToMove()
                    }
                }
        }

        override fun fromBodAst(bodPosition: BodAst.Position): PositionImpl {
            return empty()
                .let {
                    var pos = it
                    for ((rowIdx, row) in bodPosition.board.rows.withIndex()) {
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
                    for ((komaType, amount) in bodPosition.senteHand.contents) {
                        pos = pos.setHandAmount(Side.SENTE, komaType, amount)
                    }
                    for ((komaType, amount) in bodPosition.goteHand.contents) {
                        pos = pos.setHandAmount(Side.GOTE, komaType, amount)
                    }
                    pos
                }
        }
    }
}
