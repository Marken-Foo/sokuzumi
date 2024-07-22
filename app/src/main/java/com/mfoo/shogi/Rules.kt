package com.mfoo.shogi

import com.mfoo.shogi.MailboxCompanion.Direction

fun canBePromotion(move: Move.Regular): Boolean {
    return move.komaType.isPromotable() &&
        (move.startSq.isInPromotionZone(move.side)
            || move.endSq.isInPromotionZone(move.side))
}

private fun Square.isInPromotionZone(side: Side): Boolean {
    return when (side) {
        Side.SENTE -> this.row.int in 1..3
        Side.GOTE -> this.row.int in 7..9
    }
}

private fun Square.isLastRow(side: Side): Boolean {
    return when (side) {
        Side.SENTE -> this.row.int == 1
        Side.GOTE -> this.row.int == 9
    }
}

private fun Square.isLastTwoRows(side: Side): Boolean {
    return when (side) {
        Side.SENTE -> this.row.int in 1..2
        Side.GOTE -> this.row.int in 8..9
    }
}

fun isLegal(move: Move, pos: PositionImpl): Boolean {
    TODO("implement move legality checking")
}

fun isValid(move: Move, pos: Position): Boolean {
    if (!isMoveSideCorrect(move, pos)) {
        return false
    }
    return when (move) {
        is Move.Regular -> isRegularMoveValid(move, pos as PositionImpl)
        is Move.Drop -> isDropValid(move, pos)
        is Move.GameEnd -> true
    }
}

private fun isMoveSideCorrect(move: Move, pos: Position): Boolean {
    return when (move) {
        is Move.Regular -> move.side == pos.getSideToMove()
        is Move.Drop -> move.side == pos.getSideToMove()
        is Move.GameEnd -> true
    }
}

private fun isDropValid(move: Move.Drop, pos: Position): Boolean {
    if (isSquareOccupied(pos, move.sq)) {
        return false
    }
    if (pos.getHandAmount(move.side, move.komaType) < 1) {
        return false
    }
    if (isDeadKoma(move.komaType, move.sq, move.side)) {
        return false
    }
    if (isNifu(move, pos as PositionImpl)) {
        return false
    }
    return true
}

private fun isRegularMoveValid(move: Move.Regular, pos: PositionImpl): Boolean {
    val koma = pos
        .getKoma(move.startSq)
        .fold({ null }, { it })
        ?: return false
    if (koma.komaType != move.komaType) {
        return false
    }
    if (move.isPromotion && !canBePromotion(move)) {
        return false
    }
    if (isDeadKoma(move.komaType, move.endSq, move.side) && !move.isPromotion) {
        return false
    }

    val validDestinations = generateDestinations(pos.getMailbox(), move.startSq)
    return move.endSq in validDestinations
}

private fun forward(side: Side): Int {
    return when (side) {
        Side.SENTE -> Direction.N.t
        Side.GOTE -> Direction.S.t
    }
}

private fun generateDestinations(
    board: MailboxBoard,
    startSq: Square,
): List<Square> {
    val startIdx = MailboxBoardImpl.indexFromSq(startSq)
    return when (val k = board.mailbox[startIdx]) {
        MailboxContent.Invalid -> emptyList()
        MailboxContent.Empty -> emptyList()
        is MailboxContent.Koma -> generateDestinations(
            board,
            startIdx,
            k.value.side,
            k.value.komaType
        )
    }
}

private fun generateDestinations(
    board: MailboxBoard,
    startIdx: Int,
    side: Side,
    komaType: KomaType,
): List<Square> {
    return when (komaType) {
        KomaType.FU -> {
            listOf(startIdx + forward(side))
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
        }

        KomaType.KY -> {
            val forward = when (side) {
                Side.SENTE -> Direction.N
                Side.GOTE -> Direction.S
            }
            return getSquaresInRay(board, side, startIdx, forward).toList()
        }

        KomaType.KE -> {
            val forward = when (side) {
                Side.SENTE -> Direction.N
                Side.GOTE -> Direction.S
            }
            listOf(
                forward.t + forward.t + Direction.E.t,
                forward.t + forward.t + Direction.W.t,
            )
                .map { dir -> dir + startIdx }
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
        }

        KomaType.GI -> {
            val forward = forward(side)
            listOf(
                Direction.NE.t,
                Direction.SE.t,
                Direction.SW.t,
                Direction.NW.t,
                forward,
            )
                .map { dir -> dir + startIdx }
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
        }

        KomaType.KI, KomaType.TO, KomaType.NY, KomaType.NK, KomaType.NG -> {
            val forward = forward(side)
            listOf(
                Direction.N.t,
                Direction.S.t,
                Direction.E.t,
                Direction.W.t,
                forward + Direction.E.t,
                forward + Direction.W.t,
            )
                .map { dir -> dir + startIdx }
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
        }

        KomaType.KA -> {
            getSquaresInRay(board, side, startIdx, Direction.NW)
                .plus(getSquaresInRay(board, side, startIdx, Direction.NE))
                .plus(getSquaresInRay(board, side, startIdx, Direction.SE))
                .plus(getSquaresInRay(board, side, startIdx, Direction.SW))
                .toList()
        }

        KomaType.HI -> {
            getSquaresInRay(board, side, startIdx, Direction.N)
                .plus(getSquaresInRay(board, side, startIdx, Direction.S))
                .plus(getSquaresInRay(board, side, startIdx, Direction.E))
                .plus(getSquaresInRay(board, side, startIdx, Direction.W))
                .toList()
        }

        KomaType.OU -> TODO()
        KomaType.UM -> TODO()
        KomaType.RY -> TODO()
    }
}

private fun isSquareOccupied(pos: Position, sq: Square): Boolean {
    return pos.getKoma(sq).fold({ false }, { it != null })
}

private fun isAllyAtIndex(board: MailboxBoard, idx: Int, side: Side): Boolean {
    val content = board.mailbox[idx]
    return content is MailboxContent.Koma && content.value.side == side
}

/**
 * Returns whether the koma on the square will forever have no valid moves.
 */
private fun isDeadKoma(komaType: KomaType, sq: Square, side: Side): Boolean {
    return when (komaType) {
        KomaType.FU, KomaType.KY -> sq.isLastRow(side)
        KomaType.KE -> sq.isLastTwoRows(side)

        KomaType.GI, KomaType.KI, KomaType.KA, KomaType.HI, KomaType.OU,
        KomaType.TO, KomaType.NY, KomaType.NK, KomaType.NG, KomaType.UM,
        KomaType.RY,
        -> false
    }
}

/**
 * Returns whether the drop move is nifu, i.e. if there is already an allied
 * unpromoted FU on the same file.
 */
private fun isNifu(move: Move.Drop, pos: PositionImpl): Boolean {
    if (move.komaType != KomaType.FU) {
        return false
    }
    return pos.getMailbox()
        .getColumn(move.sq.col)
        .any {
            it is MailboxContent.Koma
                && it.value.komaType == KomaType.FU
                && it.value.side == move.side
        }
}

/**
 * Returns the squares in a ray that extends until it hits an allied unit.
 */
fun getSquaresInRay(
    board: MailboxBoard,
    side: Side,
    startIdx: Int,
    direction: Direction,
): Iterable<Square> {
    val res = mutableListOf<Square>()

    for (stepNum in generateSequence(1) { 1 + it }) {
        val idx = startIdx + direction.t * stepNum
        when (val content = board.mailbox[idx]) {
            MailboxContent.Empty -> {
                res.add(MailboxBoardImpl.sqFromIndex(idx))
            }

            MailboxContent.Invalid -> break
            is MailboxContent.Koma -> {
                if (content.value.side != side) {
                    res.add(MailboxBoardImpl.sqFromIndex(idx))
                    break
                }
                break
            }
        }
    }
    return res
}
