package com.mfoo.shogi


fun canBePromotion(move: Move.Regular): Boolean {
    return with(move) {
        komaType.isPromotable()
            && (startSq.isInPromotionZone(side) || endSq.isInPromotionZone(side))
    }
}

private fun Square.isInPromotionZone(side: Side): Boolean {
    return when (side) {
        Side.SENTE -> this.row.t in 1..3
        Side.GOTE -> this.row.t in 7..9
    }
}

private fun Square.isLastRow(side: Side): Boolean {
    return when (side) {
        Side.SENTE -> this.row.t == 1
        Side.GOTE -> this.row.t == 9
    }
}

private fun Square.isLastTwoRows(side: Side): Boolean {
    return when (side) {
        Side.SENTE -> this.row.t in 1..2
        Side.GOTE -> this.row.t in 8..9
    }
}

fun isLegal(move: Move, pos: PositionImpl): Boolean {
    return isValid(move, pos) && when (move) {
        is Move.GameEnd -> true
        is Move.Regular -> !isInCheck(move.side, pos.doMove(move))
        is Move.Drop -> !isInCheck(move.side, pos.doMove(move))
    }
}

/**
 * Returns whether a side is in check. Assumes that the king (OU) is the royal
 * koma for each side.
 */
fun isInCheck(side: Side, pos: PositionImpl): Boolean {
    // Implementation scans from the OU location to see which enemy units
    // could attack it, assuming standard shogi koma types.
    // May not work with non-standard-shogi komaTypes or movements in play.
    val mailbox = pos.getMailbox()
    val ouIdx = when (side) {
        Side.SENTE -> mailbox.senteOu
        Side.GOTE -> mailbox.goteOu
    }
    if (ouIdx == null) return false

    val stepperMovements = listOf(
        KomaType.FU to listOf(KomaType.FU),
        KomaType.KE to listOf(KomaType.KE),
        KomaType.GI to listOf(KomaType.GI, KomaType.RY),
        KomaType.KI to listOf(
            KomaType.KI,
            KomaType.TO,
            KomaType.NY,
            KomaType.NK,
            KomaType.NG,
            KomaType.UM
        ),
        KomaType.OU to listOf(KomaType.OU),
    )
    // Here we use the "reversibility" of the standard shogi moves:
    // e.g. The squares that a sente KI can reach are the squares
    // from which a gote KI (or TO/NY/NK/NG) would be attacking it.
    val isInCheckByStepper = stepperMovements
        .map { (movementType, attackers) ->
            getKomaMovement(side, movementType)
                .getKomasInStepRange(mailbox, ouIdx)
                .any { k -> k.side == side.switch() && k.komaType in attackers }
        }
        .any { it }

    val riderMovements = listOf(
        KomaType.KY to listOf(KomaType.KY),
        KomaType.KA to listOf(KomaType.KA, KomaType.UM),
        KomaType.HI to listOf(KomaType.HI, KomaType.RY),
    )
    // e.g. The squares that a sente KA can reach are the squares
    // from which a gote KA (or UM) would be attacking it.
    val isInCheckByRider = riderMovements
        .map { (movementType, attackers) ->
            getKomaMovement(side, movementType)
                .getKomasBlockingLines(mailbox, ouIdx)
                .any { k -> k.side == side.switch() && k.komaType in attackers }
        }
        .any { it }
    return isInCheckByStepper || isInCheckByRider
}

// TODO: Redefine "isValid" and split validity/legality checks appropriately
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
    return isSquareEmpty(pos, move.sq)
        && hasKomaToDrop(move, pos)
        && !isDeadKoma(move.komaType, move.sq, move.side)
        && !isNifu(move, pos)
}

private fun isRegularMoveValid(move: Move.Regular, pos: PositionImpl): Boolean {
    val koma = pos
        .getKoma(move.startSq)
        .fold({ null }, { it })
        ?: return false
    return with(move) {
        koma.komaType == komaType
            && !(isPromotion && !canBePromotion(move))
            && !(isDeadKoma(komaType, endSq, side) && !isPromotion)
            && endSq in getValidDestinations(pos.getMailbox(), startSq)
    }
}

private fun isSquareEmpty(pos: Position, sq: Square): Boolean {
    return pos.getKoma(sq).fold({ false }, { it == null })
}

private fun hasKomaToDrop(move: Move.Drop, pos: Position): Boolean {
    return pos.getHandAmount(move.side, move.komaType) >= 1
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
                && it.t.komaType == KomaType.FU
                && it.t.side == move.side
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
            val (side, komaType) = koma.t
            return getKomaMovement(side, komaType)
                .getDestinations(board, startIdx, side)
                .toList()
        }
    }
}
