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

fun isLegal(move: Move, pos: PositionImpl): Boolean {
    TODO("implement move legality checking")
}

fun isValid(move: Move, pos: PositionImpl): Boolean {
    if (!isMoveSideCorrect(move, pos)) {
        return false
    }
    return when (move) {
        is Move.Regular -> isValid(move, pos)
        is Move.Drop -> TODO()
        is Move.GameEnd -> true
    }
}

private fun isMoveSideCorrect(move: Move, pos: PositionImpl): Boolean {
    return when (move) {
        is Move.Regular -> move.side == pos.getSideToMove()
        is Move.Drop -> move.side == pos.getSideToMove()
        is Move.GameEnd -> true
    }
}

private fun isValid(move: Move.Regular, pos: PositionImpl): Boolean {
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

    if (koma.komaType == KomaType.FU) {
        println(pos.getMailbox())
    }
    return false
}
