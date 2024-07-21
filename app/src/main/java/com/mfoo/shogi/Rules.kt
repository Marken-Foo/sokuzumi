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
        Side.GOTE -> this.row.int == 2
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
    if (isAllyOnSquare(pos, move.sq, move.side)) {
        return false
    }
    if (pos.getHandAmount(move.side, move.komaType) < 1) {
        return false
    }
    if (isDeadDrop(move.komaType, move.sq, move.side)) {
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

    val validDestinations = generateDestinations(pos.getMailbox(), move.startSq)
    return move.endSq in validDestinations
}

private fun forward(side: Side): Int {
    return when (side) {
        Side.SENTE -> MailboxCompanion.Direction.N.t
        Side.GOTE -> MailboxCompanion.Direction.S.t
    }
}

private fun generateDestinations(
    board: MailboxBoard,
    startSq: Square,
): List<Square> {
    val startIdx = MailboxBoardImpl.indexFromSq(startSq)
    when (val k = board.mailbox[startIdx]) {
        MailboxContent.Invalid -> return emptyList()
        MailboxContent.Empty -> return emptyList()
        is MailboxContent.Koma -> {
            val side = k.value.side
            if (k.value.komaType == KomaType.FU) {
                return listOf(startIdx + forward(side))
                    .filterNot { isAllyAtIndex(board, it, side) }
                    .map(MailboxBoardImpl::sqFromIndex)
            } else if (k.value.komaType == KomaType.KI) {
                val forward = forward(side)
                return listOf(
                    MailboxCompanion.Direction.N.t,
                    MailboxCompanion.Direction.S.t,
                    MailboxCompanion.Direction.E.t,
                    MailboxCompanion.Direction.W.t,
                    forward + MailboxCompanion.Direction.E.t,
                    forward + MailboxCompanion.Direction.W.t,
                )
                    .map { dir -> dir + startIdx }
                    .filterNot { isAllyAtIndex(board, it, side) }
                    .map(MailboxBoardImpl::sqFromIndex)
            } else {
                return emptyList()
            }
        }
    }
}

private fun isAllyOnSquare(pos: Position, sq: Square, side: Side): Boolean {
    return pos.getKoma(sq).fold({ false }, { it?.side == side })
}

private fun isAllyAtIndex(board: MailboxBoard, idx: Int, side: Side): Boolean {
    val content = board.mailbox[idx]
    return content is MailboxContent.Koma && content.value.side == side
}

/**
 * Returns whether the koma on the square will forever have no valid moves.
 */
private fun isDeadDrop(komaType: KomaType, sq: Square, side: Side): Boolean {
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
