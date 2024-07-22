package com.mfoo.shogi


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

fun isValid(move: Move, pos: PositionImpl): Boolean {
    if (!isMoveSideCorrect(move, pos)) {
        return false
    }
    return when (move) {
        is Move.Regular -> isRegularMoveValid(move, pos)
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

private fun isDropValid(move: Move.Drop, pos: PositionImpl): Boolean {
    if (isSquareOccupied(pos, move.sq)) {
        return false
    }
    if (pos.getHandAmount(move.side, move.komaType) < 1) {
        return false
    }
    if (isDeadKoma(move.komaType, move.sq, move.side)) {
        return false
    }
    if (isNifu(move, pos)) {
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

    val validDestinations = getValidDestinations(pos.getMailbox(), move.startSq)
    return move.endSq in validDestinations
}

private fun isSquareOccupied(pos: Position, sq: Square): Boolean {
    return pos.getKoma(sq).fold({ false }, { it != null })
}

/**
 * Returns whether the koma on the square will forever have no valid moves.
 * Useful to determine illegal drop locations for FU, KY, and KE.
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
 * Returns the end squares (destinations) of valid moves on the board
 * that start from a given square.
 */
private fun getValidDestinations(
    board: MailboxBoard,
    startSq: Square,
): List<Square> {
    val startIdx = MailboxBoardImpl.indexFromSq(startSq)
    return when (val koma = board.mailbox[startIdx]) {
        MailboxContent.Invalid -> emptyList()
        MailboxContent.Empty -> emptyList()
        is MailboxContent.Koma -> {
            val (side, komaType) = koma.value
            return getKomaMovement(side, komaType)
                .getDestinations(board, startIdx, side)
                .toList()
        }
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
